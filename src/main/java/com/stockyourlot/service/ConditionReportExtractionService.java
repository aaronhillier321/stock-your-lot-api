package com.stockyourlot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockyourlot.dto.ConditionReportExtractionResponse;
import com.stockyourlot.entity.FileMetadata;
import com.stockyourlot.entity.FileStatus;
import com.stockyourlot.entity.FileType;
import com.stockyourlot.repository.FileMetadataRepository;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

/**
 * Extracts text from a condition report PDF and uses OpenAI to return structured fields
 * (VIN, make, model, trim, color, auction, vehicleYear, miles). No purchase price or sale date.
 * No GCS or DB; result is returned to the UI for form prefilling.
 */
@Service
public class ConditionReportExtractionService {

    private static final Logger log = LoggerFactory.getLogger(ConditionReportExtractionService.class);
    private static final String PDF_CONTENT_TYPE = "application/pdf";
    private static final long MAX_PDF_BYTES = 10 * 1024 * 1024; // 10 MB
    private static final int MAX_TEXT_CHARS = 30_000; // trim for LLM context
    private static final int OPENAI_CONNECT_TIMEOUT_MS = 30_000;
    private static final int OPENAI_READ_TIMEOUT_MS = 60_000;
    private static final String PENDING_CONDITION_REPORT_FILE = "condition-report.pdf";

    private final RestTemplate restTemplate = createRestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final GcsFileStorageService gcsFileStorageService;
    private final FileMetadataRepository fileMetadataRepository;

    @Value("${openai.api.key:}")
    private String openaiApiKey;

    @Value("${openai.api.model:gpt-4o-mini}")
    private String openaiModel;

    private static RestTemplate createRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(OPENAI_CONNECT_TIMEOUT_MS);
        factory.setReadTimeout(OPENAI_READ_TIMEOUT_MS);
        return new RestTemplate(factory);
    }

    public ConditionReportExtractionService(GcsFileStorageService gcsFileStorageService,
                                           FileMetadataRepository fileMetadataRepository) {
        this.gcsFileStorageService = gcsFileStorageService;
        this.fileMetadataRepository = fileMetadataRepository;
    }

    public ConditionReportExtractionResponse extractFromPdf(MultipartFile file, UUID uploadToken) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is required");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equalsIgnoreCase(PDF_CONTENT_TYPE)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only PDF files are allowed");
        }
        if (file.getSize() > MAX_PDF_BYTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File size must not exceed 10 MB");
        }
        if (openaiApiKey == null || openaiApiKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "OpenAI API key is not configured (OPENAI_API_KEY)");
        }

        String text;
        try {
            byte[] bytes = file.getBytes();
            try (PDDocument doc = Loader.loadPDF(bytes)) {
                PDFTextStripper stripper = new PDFTextStripper();
                text = stripper.getText(doc);
            }
        } catch (Exception e) {
            log.warn("Failed to extract text from PDF", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Could not extract text from PDF. The file may be scanned/image-only or corrupted.");
        }

        if (text == null || text.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No text could be extracted from the PDF (e.g. image-only/scanned document).");
        }

        text = text.trim();
        if (text.length() > MAX_TEXT_CHARS) {
            text = text.substring(0, MAX_TEXT_CHARS) + "... [truncated]";
        }

        String json = callOpenAi(text);
        ConditionReportExtractionResponse response = parseExtractionResponse(json);
        String tokenStr = uploadToken != null ? uploadToken.toString() : null;
        if (tokenStr != null && gcsFileStorageService.isBucketConfigured()) {
            try {
                byte[] bytes = file.getBytes();
                gcsFileStorageService.uploadPending(tokenStr, PENDING_CONDITION_REPORT_FILE, bytes);
                String pendingPath = GcsFileStorageService.pendingPath(tokenStr, PENDING_CONDITION_REPORT_FILE);
                FileMetadata meta = new FileMetadata();
                meta.setPurchase(null);
                meta.setDealership(null);
                meta.setStatus(FileStatus.PENDING);
                meta.setUploadToken(tokenStr);
                meta.setFileName(file.getOriginalFilename() != null ? file.getOriginalFilename() : PENDING_CONDITION_REPORT_FILE);
                meta.setBucket(gcsFileStorageService.getBucketName());
                meta.setObjectPath(pendingPath);
                meta.setContentType(PDF_CONTENT_TYPE);
                meta.setFileType(FileType.CONDITION_REPORT);
                meta.setSizeBytes(file.getSize());
                fileMetadataRepository.save(meta);
            } catch (Exception e) {
                log.warn("Failed to save condition report to GCS/metadata", e);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Extraction succeeded but failed to save file");
            }
        }
        return new ConditionReportExtractionResponse(
                response.vin(), response.make(), response.model(), response.trim(), response.color(),
                response.auction(), response.vehicleYear(), response.miles(),
                response.isValidConditionReport(), tokenStr);
    }

    private String callOpenAi(String documentText) {
        String prompt = """
            The following text was extracted from a vehicle condition report. Extract the following fields and return ONLY a valid JSON object with exactly these keys (use null for any value not found): vin, make, model, trim, color, auction, vehicleYear, miles, isValidConditionReport.
            Do NOT extract purchase price or sale date.
            For vehicleYear use a 4-digit number (e.g. 2023).
            For miles use a number (odometer/mileage, no commas).
            auction is the auction house or auction name if present.
            Set isValidConditionReport to true only if the document has a realistic format for a vehicle condition report (e.g. inspection checklist, condition grades, damage notes, vehicle details); otherwise false.
            Return nothing else except the JSON object.

            Text:
            %s
            """.formatted(documentText);

        Map<String, Object> requestBody = Map.of(
                "model", openaiModel,
                "messages", List.of(
                        Map.of("role", "system", "content", "You extract structured data from vehicle condition report documents. Reply only with valid JSON. Do not include purchase price or sale date."),
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.1
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openaiApiKey.trim());

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    "https://api.openai.com/v1/chat/completions",
                    new HttpEntity<>(requestBody, headers),
                    String.class
            );
            String body = response.getBody();
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.warn("OpenAI API non-2xx: status={} body={}", response.getStatusCode(), body);
                String msg = "OpenAI API error: " + (body != null && body.length() <= 500 ? body : response.getStatusCode());
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, msg);
            }
            if (body == null || body.isBlank()) {
                log.warn("OpenAI API returned empty body");
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "OpenAI API returned empty response");
            }
            JsonNode root = objectMapper.readTree(body);
            JsonNode error = root.path("error");
            if (!error.isMissingNode()) {
                String errMsg = error.path("message").asText("Unknown OpenAI error");
                log.warn("OpenAI API error payload: {}", errMsg);
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "OpenAI: " + errMsg);
            }
            JsonNode choices = root.path("choices");
            if (choices.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "OpenAI returned no completion");
            }
            String content = choices.get(0).path("message").path("content").asText(null);
            if (content == null || content.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "OpenAI returned empty content");
            }
            JsonNode usage = root.path("usage");
            if (!usage.isMissingNode()) {
                log.info("OpenAI tokens: prompt_tokens={}, completion_tokens={}, total_tokens={}",
                        usage.path("prompt_tokens").asInt(0),
                        usage.path("completion_tokens").asInt(0),
                        usage.path("total_tokens").asInt(0));
            }
            return content.trim();
        } catch (ResponseStatusException e) {
            throw e;
        } catch (HttpStatusCodeException e) {
            String body = e.getResponseBodyAsString();
            log.warn("OpenAI API error: status={} body={}", e.getStatusCode(), body);
            String msg = parseOpenAiErrorBody(body, e.getStatusCode());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, msg);
        } catch (Exception e) {
            log.error("OpenAI API error", e);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to call OpenAI: " + e.getMessage());
        }
    }

    private ConditionReportExtractionResponse parseExtractionResponse(String json) {
        try {
            if (json.startsWith("```")) {
                int start = json.indexOf('{');
                int end = json.lastIndexOf('}') + 1;
                if (start >= 0 && end > start) {
                    json = json.substring(start, end);
                }
            }
            JsonNode node = objectMapper.readTree(json);
            String vin = textOrNull(node, "vin");
            String make = textOrNull(node, "make");
            String model = textOrNull(node, "model");
            String trim = textOrNull(node, "trim");
            String color = textOrNull(node, "color");
            String auction = textOrNull(node, "auction");
            Integer vehicleYear = intOrNull(node, "vehicleYear");
            Integer miles = intOrNull(node, "miles");
            Boolean isValidConditionReport = booleanOrNull(node, "isValidConditionReport");
            return new ConditionReportExtractionResponse(vin, make, model, trim, color, auction, vehicleYear, miles, isValidConditionReport, null);
        } catch (Exception e) {
            log.warn("Failed to parse OpenAI JSON: {}", json, e);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Could not parse extraction result");
        }
    }

    private static String textOrNull(JsonNode node, String key) {
        if (!node.has(key)) return null;
        JsonNode n = node.get(key);
        if (n == null || n.isNull()) return null;
        String s = n.asText();
        return (s != null && !s.isBlank()) ? s.trim() : null;
    }

    private static Integer intOrNull(JsonNode node, String key) {
        if (!node.has(key)) return null;
        JsonNode n = node.get(key);
        if (n == null || n.isNull()) return null;
        if (n.isNumber()) return n.intValue();
        if (n.isTextual()) {
            try {
                return Integer.parseInt(n.asText().replaceAll("[^0-9]", ""));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private static Boolean booleanOrNull(JsonNode node, String key) {
        if (!node.has(key)) return null;
        JsonNode n = node.get(key);
        if (n == null || n.isNull()) return null;
        if (n.isBoolean()) return n.booleanValue();
        if (n.isTextual()) return Boolean.parseBoolean(n.asText().trim());
        return null;
    }

    private String parseOpenAiErrorBody(String body, org.springframework.http.HttpStatusCode status) {
        if (body == null || body.isBlank()) {
            return "OpenAI: " + status;
        }
        try {
            JsonNode root = objectMapper.readTree(body);
            JsonNode error = root.path("error");
            if (error.isMissingNode()) {
                return "OpenAI: " + status;
            }
            String code = error.path("code").asText(null);
            String message = error.path("message").asText("").trim();
            if ("insufficient_quota".equals(code) || (status.value() == 429 && message.toLowerCase().contains("quota"))) {
                return "OpenAI quota exceeded. Check your plan and billing: https://platform.openai.com/account/billing";
            }
            return message.length() <= 300 ? message : message.substring(0, 297) + "...";
        } catch (Exception e) {
            return body.length() <= 300 ? body.replaceAll("\\s+", " ").trim() : "OpenAI: " + status;
        }
    }
}

# Kubernetes (GKE) deploy

- **Deployment** – app, env from Secret, health probes, resources.
- **Service** – `LoadBalancer` so the API is reachable from outside the cluster.
- **Secret** – create from your secrets (e.g. in CI); see `secret.example.yaml`.

## Deploy on merge (CI)

1. Build and push image (e.g. to GCR or Artifact Registry):
   ```bash
   docker build -t gcr.io/PROJECT_ID/stock-your-lot-api:IMAGE_TAG .
   docker push gcr.io/PROJECT_ID/stock-your-lot-api:IMAGE_TAG
   ```

2. Create the DB secret (once per env, from your secrets store):
   ```bash
   kubectl create secret generic stock-your-lot-db-secret \
     --from-literal=POSTGRES_HOST=... \
     --from-literal=POSTGRES_PORT=5432 \
     --from-literal=POSTGRES_DB=... \
     --from-literal=POSTGRES_USER=... \
     --from-literal=POSTGRES_PASSWORD=...
   ```

   Create the mail secret (for invite emails). To **pull the value from GCP Secret Manager** (recommended):
   ```bash
   # Replace PROJECT_ID with your GCP project ID. The secret name in Secret Manager must match (e.g. GMAIL_APP_PASSWORD).
   kubectl create secret generic stock-your-lot-mail-secret \
     --from-literal=GMAIL_APP_PASSWORD="$(gcloud secrets versions access latest --secret=GMAIL_APP_PASSWORD --project=PROJECT_ID)"
   ```
   Or create it with a literal value (e.g. in CI from a pipeline secret):
   ```bash
   kubectl create secret generic stock-your-lot-mail-secret \
     --from-literal=GMAIL_APP_PASSWORD=your-app-password
   ```

3. Replace image in `deployment.yaml` and apply:
   ```bash
   sed -i "s|IMAGE_PLACEHOLDER|gcr.io/PROJECT_ID/stock-your-lot-api:IMAGE_TAG|g" k8s/deployment.yaml
   kubectl apply -f k8s/deployment.yaml -f k8s/service.yaml
   ```

Or use `envsubst` / a templating step in your pipeline to set the image.

## Local apply (for testing)

```bash
# Replace IMAGE_PLACEHOLDER with a real image first
kubectl apply -f k8s/
```

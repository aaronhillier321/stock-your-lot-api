# Kubernetes (GKE) deploy

- **Deployment** – app, env from Secret, health probes, resources.
- **Service** – `LoadBalancer` so the API is reachable from outside the cluster.
- **Secret** – create from your secrets (e.g. in CI); see `secret.example.yaml`.

## Using a GCP service account key file (secret mount)

Store the service account JSON key in a Kubernetes secret and mount it so the app uses it via `GOOGLE_APPLICATION_CREDENTIALS`.

1. **Create the Kubernetes secret** (one of these):

   **From a local key file** (e.g. after downloading from GCP Console → IAM → Service Accounts → Keys):
   ```bash
   kubectl create secret generic stock-your-lot-gcp-sa-secret \
     --from-file=credentials.json=/path/to/your-service-account-key.json
   ```

   **From GCP Secret Manager** (store the key JSON in a secret named `GCP_SA_KEY_JSON`, then in CI or manually):
   ```bash
   gcloud secrets versions access latest --secret=GCP_SA_KEY_JSON --project=PROJECT_ID > /tmp/gcp-sa.json
   kubectl create secret generic stock-your-lot-gcp-sa-secret \
     --from-file=credentials.json=/tmp/gcp-sa.json
   rm /tmp/gcp-sa.json
   ```
   The deploy workflow will create/update `stock-your-lot-gcp-sa-secret` from Secret Manager if `GCP_SA_KEY_JSON` exists.

2. The deployment mounts this secret at `/var/secrets/gcp/credentials.json` and sets `GOOGLE_APPLICATION_CREDENTIALS` to that path. The GCP client library (Storage, etc.) uses it automatically—no application.properties or code changes.

3. If the secret is not created, the volume is optional so the pod still starts; GCS calls will use the node's default identity (or fail if that identity has no access). Create the secret and restart the deployment when you need GCS with your SA:
   ```bash
   kubectl rollout restart deployment/stock-your-lot-api
   ```

4. **Security**: Store the key in Secret Manager and rotate it periodically; avoid committing it to git.

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

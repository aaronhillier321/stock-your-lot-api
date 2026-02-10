# Deploy to GKE – setup

The **Build and Deploy to GKE** workflow runs on every push to `main`. It builds the image, pushes to Artifact Registry, syncs DB secrets from Secret Manager into the cluster, and deploys.

## 1. GCP setup

- **Artifact Registry:** The workflow creates the repository automatically if it doesn’t exist (using the same name as the variable `ARTIFACT_REGISTRY_REPO` or default `stock-your-lot`, in `GKE_REGION`). If you see **"Repository not found"**, ensure `GKE_REGION` and `GCP_PROJECT_ID` are correct and that the service account has **Artifact Registry Admin** (or **Artifact Registry Repository Administrator**) so it can create the repo.
- **Secret Manager:** Create these secrets with your DB values (as used for Cloud SQL):
  - `POSTGRES_HOST`
  - `POSTGRES_PORT`
  - `POSTGRES_DB`
  - `POSTGRES_USER`
  - `POSTGRES_PASSWORD`
- **GKE:** Have a cluster and ensure the service account below can deploy to it (e.g. `container.developer` or equivalent).
- **Service account for GitHub Actions:** Create a GCP service account used only for CI, with:
  - **Artifact Registry:** write access to the repo (e.g. Artifact Registry Writer).
  - **Secret Manager:** Secret Manager Secret Accessor (so the workflow can read the 5 secrets).
  - **GKE:** permission to get cluster credentials and update the workload (e.g. Kubernetes Engine Developer or a custom role with `container.*` as needed).
  - Create a JSON key for this SA and store it in GitHub (see below).

## 2. GitHub secrets

In the repo: **Settings → Secrets and variables → Actions**, add:

| Secret            | Description |
|-------------------|-------------|
| `GCP_PROJECT_ID`  | Your GCP project ID. |
| `GCP_SA_KEY`      | Full JSON key of the CI service account (copy the entire JSON). |
| `GKE_CLUSTER_NAME`| Name of the GKE cluster to deploy to. |
| `GKE_REGION`      | Region of the cluster (e.g. `us-central1`). |

Optional: add a **variable** (not secret) **`ARTIFACT_REGISTRY_REPO`** if your Artifact Registry repo name is not `stock-your-lot`.

## 3. First run

After saving the secrets, push (or merge) to `main`. The workflow will:

1. Build the image and push to `us-docker.pkg.dev/<GCP_PROJECT_ID>/<ARTIFACT_REGISTRY_REPO>/stock-your-lot-api:<git-sha>`.
2. Create/update the Kubernetes Secret `stock-your-lot-db-secret` from Secret Manager.
3. Deploy/update the Deployment and Service in the cluster.

If anything fails, check the Actions log and that the service account has the permissions above and that the 5 Secret Manager secrets exist and are accessible.

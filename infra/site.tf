terraform {
  required_version = "~> 1.2.0"

  required_providers {
    google = {
      source  = "hashicorp/google"
      version = "~> 4.51.0"
    }
  }

  backend "gcs" {
    bucket = "110416_tfstate_storage"
    prefix = "tfstate/website"
  }
}

provider "google" {
  project = var.site_project_id
}


resource "google_service_account" "deploy_agent" {
  account_id   = "website-deploy-agent"
  display_name = "${var.service_name}_deploy_agent"
}
resource "google_service_account" "site_run_agent" {
  account_id   = "website-run-agent"
  display_name = "${var.service_name}_run_agent"
}




resource "google_project_iam_member" "deploy_gcr_rw" {
  project = var.site_project_id
  role    = "roles/storage.admin"
  member  = "serviceAccount:${google_service_account.deploy_agent.email}"
}
resource "google_project_iam_member" "run_agant" {
  project = var.site_project_id
  role    = "roles/iam.serviceAccountUser"
  member  = "serviceAccount:${google_service_account.deploy_agent.email}"
}

resource "google_project_iam_member" "deploy_run" {
  project = var.site_project_id
  role    = "roles/run.developer"
  member  = "serviceAccount:${google_service_account.deploy_agent.email}"
}


data "google_iam_policy" "public_access" {
  binding {
    role = "roles/run.invoker"
    members = [
      "allUsers",
    ]
  }
}

resource "google_cloud_run_service_iam_policy" "noauth" {
  location = google_cloud_run_service.website.location
  project  = google_cloud_run_service.website.project
  service  = google_cloud_run_service.website.name

  policy_data = data.google_iam_policy.public_access.policy_data
}


resource "google_cloud_run_domain_mapping" "website" {
  location = var.site_project_loc
  name     = var.site_domain

  metadata {
    namespace = var.site_project_id

    labels = {
      scope = "website"
    }
  }

  spec {
    route_name = google_cloud_run_service.website.name
  }
}
resource "google_cloud_run_service" "website" {
  name     = var.service_name
  location = var.site_project_loc

  metadata {
    namespace = var.site_project_id
    labels = {
      scope = "website"
    }
  }
  traffic {
    percent         = 100
    latest_revision = true
  }


  template {
    spec {
      service_account_name = var.run_agent
      containers {
        image = "asia.gcr.io/${var.site_project_id}/cloudrun/${var.service_name}:latest"
      }
    }
  }
}

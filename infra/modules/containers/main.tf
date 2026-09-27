locals {
  pull_secret_enabled = var.ocir_pull_secret_id != ""
}

resource "oci_container_instances_container_instance" "backend" {
  compartment_id                       = var.compartment_id
  availability_domain                  = var.availability_domain
  display_name                         = "news-platform-${var.environment}-backend"
  shape                                = var.container_shape
  container_restart_policy             = "ALWAYS"
  graceful_shutdown_timeout_in_seconds = 30
  shape_config {
    ocpus         = var.backend_cpu
    memory_in_gbs = var.backend_memory_gbs
  }
  vnics {
    subnet_id             = var.app_subnet_id
    nsg_ids               = [var.app_nsg_id]
    is_public_ip_assigned = false
  }
  dynamic "image_pull_secrets" {
    for_each = local.pull_secret_enabled ? [1] : []
    content {
      registry_endpoint = var.ocir_registry
      secret_type       = "VAULT"
      secret_id         = var.ocir_pull_secret_id
    }
  }
  containers {
    display_name                   = "backend"
    image_url                      = var.backend_image
    environment_variables          = var.backend_environment
    is_resource_principal_disabled = false
    resource_config {
      vcpus_limit         = var.backend_cpu
      memory_limit_in_gbs = var.backend_memory_gbs
    }
    health_checks {
      name                     = "backend-readiness"
      health_check_type        = "HTTP"
      path                     = "/actuator/health/readiness"
      port                     = 8080
      initial_delay_in_seconds = 30
      interval_in_seconds      = 10
      timeout_in_seconds       = 5
      failure_threshold        = 6
      success_threshold        = 1
    }
  }
}

resource "oci_container_instances_container_instance" "web" {
  compartment_id                       = var.compartment_id
  availability_domain                  = var.availability_domain
  display_name                         = "news-platform-${var.environment}-web"
  shape                                = var.container_shape
  container_restart_policy             = "ALWAYS"
  graceful_shutdown_timeout_in_seconds = 30
  shape_config {
    ocpus         = var.web_cpu
    memory_in_gbs = var.web_memory_gbs
  }
  vnics {
    subnet_id             = var.app_subnet_id
    nsg_ids               = [var.app_nsg_id]
    is_public_ip_assigned = false
  }
  dynamic "image_pull_secrets" {
    for_each = local.pull_secret_enabled ? [1] : []
    content {
      registry_endpoint = var.ocir_registry
      secret_type       = "VAULT"
      secret_id         = var.ocir_pull_secret_id
    }
  }
  containers {
    display_name          = "web"
    image_url             = var.web_image
    environment_variables = var.web_environment
    resource_config {
      vcpus_limit         = var.web_cpu
      memory_limit_in_gbs = var.web_memory_gbs
    }
    health_checks {
      name                     = "web-homepage"
      health_check_type        = "HTTP"
      path                     = "/"
      port                     = 3000
      initial_delay_in_seconds = 20
      interval_in_seconds      = 10
      timeout_in_seconds       = 5
      failure_threshold        = 6
      success_threshold        = 1
    }
  }
}

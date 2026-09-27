output "backend_repository" { value = "${var.namespace}/${var.backend_image_name}" }
output "web_repository" { value = "${var.namespace}/${var.web_image_name}" }
output "backend_repository_id" { value = oci_artifacts_container_repository.backend.id }
output "web_repository_id" { value = oci_artifacts_container_repository.web.id }

resource "oci_artifacts_container_repository" "backend" {
  compartment_id = var.compartment_id
  display_name   = var.backend_image_name
  is_immutable   = true
  is_public      = false
}

resource "oci_artifacts_container_repository" "web" {
  compartment_id = var.compartment_id
  display_name   = var.web_image_name
  is_immutable   = true
  is_public      = false
}

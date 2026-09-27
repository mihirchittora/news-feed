resource "oci_objectstorage_bucket" "media" {
  compartment_id = var.compartment_id
  namespace      = var.namespace
  name           = var.bucket_name
  access_type    = "NoPublicAccess"
  storage_tier   = "Standard"
  versioning     = "Enabled"
  kms_key_id     = var.kms_key_id == "" ? null : var.kms_key_id
  freeform_tags = {
    environment = var.environment
    service     = "news-platform"
  }
}

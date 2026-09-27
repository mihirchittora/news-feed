resource "oci_kms_vault" "this" {
  compartment_id = var.compartment_id
  display_name   = "news-platform-${var.environment}-vault"
  vault_type     = "DEFAULT"
}

resource "oci_kms_key" "this" {
  compartment_id           = var.compartment_id
  display_name             = "news-platform-${var.environment}-data-key"
  management_endpoint      = oci_kms_vault.this.management_endpoint
  protection_mode          = "SOFTWARE"
  is_auto_rotation_enabled = false
  key_shape {
    algorithm = "AES"
    length    = 32
  }
}

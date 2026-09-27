output "vault_id" { value = oci_kms_vault.this.id }
output "vault_management_endpoint" { value = oci_kms_vault.this.management_endpoint }
output "kms_key_id" { value = oci_kms_key.this.id }

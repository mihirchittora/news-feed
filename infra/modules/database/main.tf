resource "oci_psql_db_system" "this" {
  compartment_id              = var.compartment_id
  db_version                  = var.db_version
  display_name                = "news-platform-${var.environment}-postgres"
  shape                       = var.shape
  instance_count              = 1
  instance_ocpu_count         = var.instance_ocpu_count
  instance_memory_size_in_gbs = var.instance_memory_size_in_gbs

  credentials {
    username = var.database_username
    password_details {
      password_type  = "VAULT_SECRET"
      secret_id      = var.database_password_secret_id
      secret_version = var.database_password_secret_version
    }
  }

  network_details {
    subnet_id = var.subnet_id
    nsg_ids   = [var.nsg_id]
  }

  storage_details {
    is_regionally_durable = true
    system_type           = "OCI_OPTIMIZED_STORAGE"
  }

  management_policy {
    backup_policy {
      kind           = "DAILY"
      retention_days = var.backup_retention_days
      backup_start   = "02:00"
    }
  }
}

data "oci_psql_db_system_connection_detail" "this" {
  db_system_id = oci_psql_db_system.this.id
}

output "db_system_id" { value = oci_psql_db_system.this.id }
output "private_endpoint" { value = data.oci_psql_db_system_connection_detail.this.primary_db_endpoint[0].fqdn }
output "private_port" { value = data.oci_psql_db_system_connection_detail.this.primary_db_endpoint[0].port }
output "jdbc_url" { value = "jdbc:postgresql://${data.oci_psql_db_system_connection_detail.this.primary_db_endpoint[0].fqdn}:${data.oci_psql_db_system_connection_detail.this.primary_db_endpoint[0].port}/news_platform" }
output "database_username" { value = var.database_username }

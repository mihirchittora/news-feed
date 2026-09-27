output "vcn_id" { value = oci_core_vcn.this.id }
output "public_subnet_id" { value = oci_core_subnet.public.id }
output "app_subnet_id" { value = oci_core_subnet.app.id }
output "database_subnet_id" { value = oci_core_subnet.database.id }
output "load_balancer_nsg_id" { value = oci_core_network_security_group.load_balancer.id }
output "app_nsg_id" { value = oci_core_network_security_group.app.id }
output "database_nsg_id" { value = oci_core_network_security_group.database.id }

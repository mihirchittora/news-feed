output "backend_id" { value = oci_container_instances_container_instance.backend.id }
output "web_id" { value = oci_container_instances_container_instance.web.id }
output "backend_vnic_id" { value = oci_container_instances_container_instance.backend.vnics[0].vnic_id }
output "web_vnic_id" { value = oci_container_instances_container_instance.web.vnics[0].vnic_id }

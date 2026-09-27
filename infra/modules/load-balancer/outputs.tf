output "load_balancer_id" { value = oci_load_balancer_load_balancer.this.id }
output "public_ip" { value = try(oci_load_balancer_load_balancer.this.ip_address_details[0].ip_address, null) }
output "endpoint" { value = var.frontend_domain == "" ? null : "${var.certificate_id == "" ? "http" : "https"}://${var.frontend_domain}" }

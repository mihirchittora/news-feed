data "oci_core_private_ips" "backend" {
  vnic_id = var.backend_vnic_id
}

data "oci_core_private_ips" "web" {
  vnic_id = var.web_vnic_id
}

resource "oci_load_balancer_load_balancer" "this" {
  compartment_id             = var.compartment_id
  display_name               = "news-platform-${var.environment}-lb"
  is_private                 = false
  is_request_id_enabled      = true
  network_security_group_ids = [var.load_balancer_nsg_id]
  shape                      = "flexible"
  shape_details {
    minimum_bandwidth_in_mbps = 10
    maximum_bandwidth_in_mbps = 10
  }
  subnet_ids = [var.public_subnet_id]
}

resource "oci_load_balancer_backend_set" "backend" {
  load_balancer_id = oci_load_balancer_load_balancer.this.id
  name             = "backend"
  policy           = "LEAST_CONNECTIONS"
  health_checker {
    protocol          = "HTTP"
    port              = var.backend_port
    url_path          = "/actuator/health/readiness"
    return_code       = 200
    interval_ms       = 10000
    timeout_in_millis = 5000
    retries           = 3
  }
}

resource "oci_load_balancer_backend_set" "web" {
  load_balancer_id = oci_load_balancer_load_balancer.this.id
  name             = "web"
  policy           = "LEAST_CONNECTIONS"
  health_checker {
    protocol          = "HTTP"
    port              = var.web_port
    url_path          = "/"
    return_code       = 200
    interval_ms       = 10000
    timeout_in_millis = 5000
    retries           = 3
  }
}

resource "oci_load_balancer_backend" "backend" {
  load_balancer_id = oci_load_balancer_load_balancer.this.id
  backendset_name  = oci_load_balancer_backend_set.backend.name
  ip_address       = data.oci_core_private_ips.backend.private_ips[0].ip_address
  port             = var.backend_port
  weight           = 1
}

resource "oci_load_balancer_backend" "web" {
  load_balancer_id = oci_load_balancer_load_balancer.this.id
  backendset_name  = oci_load_balancer_backend_set.web.name
  ip_address       = data.oci_core_private_ips.web.private_ips[0].ip_address
  port             = var.web_port
  weight           = 1
}

resource "oci_load_balancer_load_balancer_routing_policy" "this" {
  load_balancer_id           = oci_load_balancer_load_balancer.this.id
  name                       = "api-path-routing"
  condition_language_version = "V1"
  rules {
    name      = "api-paths"
    condition = "any(http.request.url.path sw '/api', http.request.url.path sw '/actuator', http.request.url.path sw '/v3')"
    actions {
      name             = "FORWARD_TO_BACKENDSET"
      backend_set_name = oci_load_balancer_backend_set.backend.name
    }
  }
}

resource "oci_load_balancer_rule_set" "http_to_https" {
  count            = var.certificate_id == "" ? 0 : 1
  load_balancer_id = oci_load_balancer_load_balancer.this.id
  name             = "http-to-https"
  items {
    action        = "REDIRECT"
    response_code = 301
    redirect_uri {
      protocol = "HTTPS"
      host     = "{host}"
      path     = "{path}"
      query    = "{query}"
    }
  }
}

resource "oci_load_balancer_listener" "http" {
  load_balancer_id         = oci_load_balancer_load_balancer.this.id
  name                     = "http"
  default_backend_set_name = oci_load_balancer_backend_set.web.name
  port                     = 80
  protocol                 = "HTTP"
  routing_policy_name      = oci_load_balancer_load_balancer_routing_policy.this.name
  rule_set_names           = var.certificate_id == "" ? [] : [oci_load_balancer_rule_set.http_to_https[0].name]
}

resource "oci_load_balancer_listener" "https" {
  count                    = var.certificate_id == "" ? 0 : 1
  load_balancer_id         = oci_load_balancer_load_balancer.this.id
  name                     = "https"
  default_backend_set_name = oci_load_balancer_backend_set.web.name
  port                     = 443
  protocol                 = "HTTPS"
  routing_policy_name      = oci_load_balancer_load_balancer_routing_policy.this.name
  ssl_configuration {
    certificate_ids = [var.certificate_id]
    protocols       = ["TLSv1.2", "TLSv1.3"]
  }
}

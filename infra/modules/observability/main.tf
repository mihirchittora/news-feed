resource "oci_logging_log_group" "this" {
  compartment_id = var.compartment_id
  display_name   = "news-platform-${var.environment}-logs"
  description    = "Application and OCI service logs for the ${var.environment} news platform environment"
}

resource "oci_monitoring_alarm" "this" {
  for_each = var.notification_topic_id == "" ? {} : var.alarms

  compartment_id        = var.compartment_id
  destinations          = [var.notification_topic_id]
  display_name          = "news-platform-${var.environment}-${each.key}"
  is_enabled            = true
  metric_compartment_id = var.compartment_id
  namespace             = each.value.namespace
  query                 = each.value.query
  severity              = each.value.severity
  pending_duration      = "PT5M"
  body                  = "Review the deployment runbook and OCI service health for the ${var.environment} environment."
}

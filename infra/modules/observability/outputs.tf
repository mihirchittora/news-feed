output "log_group_id" { value = oci_logging_log_group.this.id }
output "alarm_ids" { value = { for name, alarm in oci_monitoring_alarm.this : name => alarm.id } }

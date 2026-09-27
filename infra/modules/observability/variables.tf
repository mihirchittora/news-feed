variable "compartment_id" { type = string }
variable "environment" { type = string }
variable "notification_topic_id" {
  type    = string
  default = ""
}
variable "alarms" {
  type = map(object({
    namespace = string
    query     = string
    severity  = string
  }))
  default = {}
}

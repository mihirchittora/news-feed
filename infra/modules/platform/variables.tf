variable "compartment_id" { type = string }
variable "environment" { type = string }
variable "region" { type = string }
variable "availability_domain" { type = string }
variable "tenancy_namespace" { type = string }
variable "vcn_cidr" { type = string }
variable "public_subnet_cidr" { type = string }
variable "app_subnet_cidr" { type = string }
variable "database_subnet_cidr" { type = string }
variable "vcn_dns_label" { type = string }
variable "bucket_name" { type = string }
variable "certificate_id" {
  type    = string
  default = ""
}
variable "frontend_url" { type = string }
variable "backend_url" { type = string }
variable "frontend_domain" {
  type    = string
  default = ""
}
variable "ocir_registry" { type = string }
variable "backend_image_name" {
  type    = string
  default = "news-platform-backend"
}
variable "web_image_name" {
  type    = string
  default = "news-platform-web"
}
variable "backend_image" { type = string }
variable "web_image" { type = string }
variable "ocir_pull_secret_id" { type = string }
variable "database_password_secret_id" { type = string }
variable "database_password_secret_version" { type = string }
variable "jwt_secret_id" { type = string }
variable "initial_admin_password_secret_id" {
  type    = string
  default = ""
}
variable "database_username" {
  type    = string
  default = "news_platform"
}
variable "database_pool_max_size" {
  type    = number
  default = 10
}
variable "database_pool_min_idle" {
  type    = number
  default = 2
}
variable "app_timezone" {
  type    = string
  default = "Asia/Kolkata"
}
variable "notification_topic_id" {
  type    = string
  default = ""
}
variable "artifact_tag" { type = string }
variable "monitoring_alarms" {
  type = map(object({
    namespace = string
    query     = string
    severity  = string
  }))
  default = {}
}

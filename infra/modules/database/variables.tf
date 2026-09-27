variable "compartment_id" { type = string }
variable "environment" { type = string }
variable "subnet_id" { type = string }
variable "nsg_id" { type = string }
variable "availability_domain" {
  type    = string
  default = null
}
variable "db_version" {
  type    = string
  default = "16"
}
variable "shape" {
  type    = string
  default = "VM.Standard.E4.Flex"
}
variable "instance_ocpu_count" {
  type    = number
  default = 2
}
variable "instance_memory_size_in_gbs" {
  type    = number
  default = 8
}
variable "database_username" {
  type    = string
  default = "news_platform"
}
variable "database_password_secret_id" { type = string }
variable "database_password_secret_version" { type = string }
variable "backup_retention_days" {
  type    = number
  default = 35
}

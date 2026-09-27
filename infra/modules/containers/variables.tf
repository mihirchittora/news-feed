variable "compartment_id" { type = string }
variable "environment" { type = string }
variable "availability_domain" { type = string }
variable "app_subnet_id" { type = string }
variable "app_nsg_id" { type = string }
variable "backend_image" { type = string }
variable "web_image" { type = string }
variable "ocir_registry" { type = string }
variable "ocir_pull_secret_id" { type = string }
variable "backend_environment" {
  type      = map(string)
  sensitive = true
}
variable "web_environment" { type = map(string) }
variable "backend_cpu" {
  type    = number
  default = 1
}
variable "backend_memory_gbs" {
  type    = number
  default = 4
}
variable "web_cpu" {
  type    = number
  default = 1
}
variable "web_memory_gbs" {
  type    = number
  default = 2
}
variable "container_shape" {
  type    = string
  default = "CI.Standard.E4.Flex"
}

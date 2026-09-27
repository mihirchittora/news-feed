variable "compartment_id" { type = string }
variable "environment" { type = string }
variable "public_subnet_id" { type = string }
variable "load_balancer_nsg_id" { type = string }
variable "backend_vnic_id" { type = string }
variable "web_vnic_id" { type = string }
variable "certificate_id" {
  type    = string
  default = ""
}
variable "frontend_domain" {
  type    = string
  default = ""
}
variable "backend_port" {
  type    = number
  default = 8080
}
variable "web_port" {
  type    = number
  default = 3000
}

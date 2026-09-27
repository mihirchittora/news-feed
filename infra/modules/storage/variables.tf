variable "compartment_id" { type = string }
variable "namespace" { type = string }
variable "bucket_name" { type = string }
variable "kms_key_id" {
  type    = string
  default = ""
}
variable "environment" { type = string }

module "secrets" {
  source         = "../secrets"
  compartment_id = var.compartment_id
  environment    = var.environment
}

module "network" {
  source               = "../network"
  compartment_id       = var.compartment_id
  environment          = var.environment
  vcn_cidr             = var.vcn_cidr
  public_subnet_cidr   = var.public_subnet_cidr
  app_subnet_cidr      = var.app_subnet_cidr
  database_subnet_cidr = var.database_subnet_cidr
  vcn_dns_label        = var.vcn_dns_label
}

module "registry" {
  source             = "../registry"
  compartment_id     = var.compartment_id
  namespace          = var.tenancy_namespace
  environment        = var.environment
  backend_image_name = var.backend_image_name
  web_image_name     = var.web_image_name
}

module "storage" {
  source         = "../storage"
  compartment_id = var.compartment_id
  namespace      = var.tenancy_namespace
  bucket_name    = var.bucket_name
  environment    = var.environment
  kms_key_id     = module.secrets.kms_key_id
}

module "database" {
  source                           = "../database"
  compartment_id                   = var.compartment_id
  environment                      = var.environment
  subnet_id                        = module.network.database_subnet_id
  nsg_id                           = module.network.database_nsg_id
  availability_domain              = var.availability_domain
  database_username                = var.database_username
  database_password_secret_id      = var.database_password_secret_id
  database_password_secret_version = var.database_password_secret_version
}

module "containers" {
  source              = "../containers"
  compartment_id      = var.compartment_id
  environment         = var.environment
  availability_domain = var.availability_domain
  app_subnet_id       = module.network.app_subnet_id
  app_nsg_id          = module.network.app_nsg_id
  backend_image       = var.backend_image
  web_image           = var.web_image
  ocir_registry       = var.ocir_registry
  ocir_pull_secret_id = var.ocir_pull_secret_id
  backend_environment = {
    DATABASE_URL                                 = module.database.jdbc_url
    DATABASE_USERNAME                            = module.database.database_username
    SPRING_PROFILES_ACTIVE                       = "prod"
    APP_ENVIRONMENT                              = var.environment
    APP_TIMEZONE                                 = var.app_timezone
    FRONTEND_URL                                 = var.frontend_url
    MEDIA_STORAGE_TYPE                           = "oci"
    MEDIA_PUBLIC_URL                             = var.backend_url
    OCI_REGION                                   = var.region
    OCI_OBJECT_STORAGE_NAMESPACE                 = var.tenancy_namespace
    OCI_OBJECT_STORAGE_BUCKET                    = module.storage.bucket_name
    OCI_VAULT_SECRET_DATABASE_PASSWORD_OCID      = var.database_password_secret_id
    OCI_VAULT_SECRET_JWT_SECRET_OCID             = var.jwt_secret_id
    OCI_VAULT_SECRET_INITIAL_ADMIN_PASSWORD_OCID = var.initial_admin_password_secret_id
    SECURITY_HSTS_ENABLED                        = "true"
    FLYWAY_ENABLED                               = "true"
    DATABASE_POOL_MAX_SIZE                       = tostring(var.database_pool_max_size)
    DATABASE_POOL_MIN_IDLE                       = tostring(var.database_pool_min_idle)
    APP_VERSION                                  = var.artifact_tag
    COMMIT_SHA                                   = var.artifact_tag
  }
  web_environment = {
    NEXT_PUBLIC_API_URL          = var.backend_url
    INTERNAL_API_URL             = var.backend_url
    SITE_URL                     = var.frontend_url
    NEXT_PUBLIC_SITE_URL         = var.frontend_url
    NEXT_PUBLIC_FEED_AD_INTERVAL = "5"
  }
}

module "load_balancer" {
  source               = "../load-balancer"
  compartment_id       = var.compartment_id
  environment          = var.environment
  public_subnet_id     = module.network.public_subnet_id
  load_balancer_nsg_id = module.network.load_balancer_nsg_id
  backend_vnic_id      = module.containers.backend_vnic_id
  web_vnic_id          = module.containers.web_vnic_id
  certificate_id       = var.certificate_id
  frontend_domain      = var.frontend_domain
}

module "observability" {
  source                = "../observability"
  compartment_id        = var.compartment_id
  environment           = var.environment
  notification_topic_id = var.notification_topic_id
  alarms                = var.monitoring_alarms
}

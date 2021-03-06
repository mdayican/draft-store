terraform {
  backend "azurerm" {}

  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "2.31"
    }
    random = {
      source = "hashicorp/random"
    }
  }
}

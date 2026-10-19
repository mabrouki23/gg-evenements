#!/bin/bash
# ==============================================================================
# Script de déploiement d'infrastructure Azure pour GG'Événements
# Nom de l'application utilisé pour toutes les ressources : gg-evenements
# ==============================================================================

set -e

APP_NAME="gg-evenements"
RESOURCE_GROUP="rg-${APP_NAME}"
LOCATION="canadacentral"

echo "=== 1. Vérification de la connexion Azure CLI ==="
az account show > /dev/null || (echo "Veuillez vous connecter à Azure avec 'az login'" && exit 1)

echo "=== 2. Création du Groupe de Ressources: $RESOURCE_GROUP ==="
az group create --name $RESOURCE_GROUP --location $LOCATION

echo "=== 3. Déploiement du modèle Bicep (App Service + Azure SQL) ==="
az deployment group create \
  --resource-group $RESOURCE_GROUP \
  --template-file ./infra/main.bicep \
  --parameters appName=$APP_NAME

echo "=== 4. Récupération du Profil de Publication pour GitHub Actions ==="
az webapp deployment list-publishing-profiles --name $APP_NAME --resource-group $RESOURCE_GROUP --xml > publish-profile-prod.xml
echo "Enregistré dans publish-profile-prod.xml (À ajouter dans le secret GitHub : AZURE_WEBAPP_PUBLISH_PROFILE)"

echo "=============================================================================="
echo "Déploiement de l'infrastructure terminé avec succès !"
echo "Application disponible : https://${APP_NAME}.azurewebsites.net"
echo "=============================================================================="

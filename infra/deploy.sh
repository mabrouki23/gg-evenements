#!/bin/bash
# ==============================================================================
# Script de déploiement d'infrastructure Azure pour GG'Événements
# Nom de l'application utilisé pour toutes les ressources : gg-evenements
# ==============================================================================

set -e

APP_NAME="gg-evenements"
RESOURCE_GROUP="rg-${APP_NAME}"
LOCATION="eastus" # Vous pouvez changer la région (ex: canadacentral, westeurope)

echo "=== 1. Vérification de la connexion Azure CLI ==="
az account show > /dev/null || (echo "Veuillez vous connecter à Azure avec 'az login'" && exit 1)

echo "=== 2. Création du Groupe de Ressources: $RESOURCE_GROUP ==="
az group create --name $RESOURCE_GROUP --location $LOCATION

echo "=== 3. Demande du mot de passe administrateur pour Azure MySQL ==="
read -s -p "Entrez le mot de passe administrateur MySQL (minimum 8 caractères, majuscule, chiffre, symbole) : " MYSQL_PASS
echo ""

echo "=== 4. Déploiement du modèle Bicep (App Service Prod + Dev + MySQL Azure) ==="
az deployment group create \
  --resource-group $RESOURCE_GROUP \
  --template-file ./infra/main.bicep \
  --parameters appName=$APP_NAME mysqlAdminPassword=$MYSQL_PASS

echo "=== 5. Récupération des Profils de Publication pour GitHub Actions ==="
echo "--- Profil Production ---"
az webapp deployment list-publishing-profiles --name $APP_NAME --resource-group $RESOURCE_GROUP --xml > publish-profile-prod.xml
echo "Enregistré dans publish-profile-prod.xml (À ajouter dans le secret GitHub : AZURE_WEBAPP_PUBLISH_PROFILE)"

echo "--- Profil Développement (Dev) ---"
az webapp deployment list-publishing-profiles --name "${APP_NAME}-dev" --resource-group $RESOURCE_GROUP --xml > publish-profile-dev.xml
echo "Enregistré dans publish-profile-dev.xml (À ajouter dans le secret GitHub : AZURE_WEBAPP_PUBLISH_PROFILE_DEV)"

echo "=============================================================================="
echo "Déploiement de l'infrastructure terminé avec succès !"
echo "Applications disponibles :"
echo " - Production : https://${APP_NAME}.azurewebsites.net"
echo " - Dev        : https://${APP_NAME}-dev.azurewebsites.net"
echo "=============================================================================="

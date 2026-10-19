@description('Nom de l\'application (utilisé pour nommer toutes les ressources Azure)')
param appName string = 'gg-evenements-cgodin'

@description('Région Azure pour le déploiement')
param location string = resourceGroup().location

// --- 1. Plan App Service Basic B1 (Linux, Couvert par Azure for Students) ---
resource appServicePlan 'Microsoft.Web/serverfarms@2022-09-01' = {
  name: 'asp-${appName}'
  location: location
  kind: 'linux'
  sku: {
    name: 'B1'
    tier: 'Basic'
  }
  properties: {
    reserved: true // Requis pour Linux
  }
}

// --- 2. App Service Web App - Environnement PRODUCTION ---
resource webAppProd 'Microsoft.Web/sites@2022-09-01' = {
  name: appName
  location: location
  properties: {
    serverFarmId: appServicePlan.id
    siteConfig: {
      linuxFxVersion: 'JAVA|17-java17'
      appSettings: [
        {
          name: 'SPRING_PROFILES_ACTIVE'
          value: 'azure'
        }
        {
          name: 'SPRING_DATASOURCE_URL'
          value: 'jdbc:h2:file:/home/site/wwwroot/db/ggevenements;MODE=MySQL;AUTO_SERVER=TRUE;DB_CLOSE_ON_EXIT=FALSE'
        }
        {
          name: 'SPRING_DATASOURCE_DRIVER_CLASS_NAME'
          value: 'org.h2.Driver'
        }
        {
          name: 'SPRING_DATASOURCE_USERNAME'
          value: 'sa'
        }
        {
          name: 'SPRING_DATASOURCE_PASSWORD'
          value: ''
        }
        {
          name: 'SPRING_JPA_DATABASE_PLATFORM'
          value: 'org.hibernate.dialect.H2Dialect'
        }
      ]
    }
  }
}

// --- 3. App Service Web App - Environnement DEVELOPPEMENT (DEV) ---
resource webAppDev 'Microsoft.Web/sites@2022-09-01' = {
  name: '${appName}-dev'
  location: location
  properties: {
    serverFarmId: appServicePlan.id
    siteConfig: {
      linuxFxVersion: 'JAVA|17-java17'
      appSettings: [
        {
          name: 'SPRING_PROFILES_ACTIVE'
          value: 'dev'
        }
        {
          name: 'SPRING_DATASOURCE_URL'
          value: 'jdbc:h2:mem:ggevenementsdev;DB_CLOSE_DELAY=-1;MODE=MySQL'
        }
      ]
    }
  }
}

output webAppProdUrl string = 'https://${webAppProd.properties.defaultHostName}'
output webAppDevUrl string = 'https://${webAppDev.properties.defaultHostName}'

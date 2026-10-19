@description('Nom de l\'application (utilisé pour nommer toutes les ressources Azure)')
param appName string = 'gg-evenements'

@description('Région Azure pour le déploiement')
param location string = resourceGroup().location

// --- 1. Unique Plan App Service Linux B1 ---
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

// --- 2. Unique App Service Web App (Spring Boot + React) ---
resource webApp 'Microsoft.Web/sites@2022-09-01' = {
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

output webAppUrl string = 'https://${webApp.properties.defaultHostName}'

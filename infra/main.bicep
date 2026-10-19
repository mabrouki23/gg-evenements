@description('Nom de l\'application (utilisé pour nommer toutes les ressources Azure)')
param appName string = 'gg-evenements'

@description('Région Azure pour le déploiement')
param location string = resourceGroup().location

@description('Nom d\'utilisateur administrateur MySQL Azure')
param mysqlAdminUser string = 'ggadmin'

@description('Mot de passe administrateur MySQL Azure')
@secure()
param mysqlAdminPassword string

@description('Rôle/SKU de la base de données MySQL Flexible Server')
param mysqlSkuName string = 'Standard_B1ms'

// --- 1. Plan App Service (Linux, Java 17) ---
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

// --- 2. Azure Database for MySQL Flexible Server ---
resource mysqlServer 'Microsoft.DBforMySQL/flexibleServers@2023-06-30' = {
  name: 'mysql-${appName}'
  location: location
  sku: {
    name: mysqlSkuName
    tier: 'Burstable'
  }
  properties: {
    administratorLogin: mysqlAdminUser
    administratorLoginPassword: mysqlAdminPassword
    version: '8.0.21'
    storage: {
      storageSizeGB: 20
      autoGrow: 'Enabled'
    }
    backup: {
      backupRetentionDays: 7
      geoRedundantBackup: 'Disabled'
    }
  }
}

// Base de données MySQL pour l'application
resource mysqlDatabase 'Microsoft.DBforMySQL/flexibleServers/databases@2023-06-30' = {
  parent: mysqlServer
  name: 'ggevenements'
  properties: {
    charset: 'utf8mb4'
    collation: 'utf8mb4_unicode_ci'
  }
}

// Règle de pare-feu : Autoriser les services Azure (App Service)
resource mysqlFirewallAzure 'Microsoft.DBforMySQL/flexibleServers/firewallRules@2023-06-30' = {
  parent: mysqlServer
  name: 'AllowAzureServices'
  properties: {
    startIpAddress: '0.0.0.0'
    endIpAddress: '0.0.0.0'
  }
}

// --- 3. App Service Web App - Environnement PRODUCTION ---
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
          value: 'jdbc:mysql://${mysqlServer.name}.mysql.database.azure.com:3306/ggevenements?useSSL=true&requireSSL=true&serverTimezone=UTC'
        }
        {
          name: 'SPRING_DATASOURCE_USERNAME'
          value: mysqlAdminUser
        }
        {
          name: 'SPRING_DATASOURCE_PASSWORD'
          value: mysqlAdminPassword
        }
      ]
    }
  }
}

// --- 4. App Service Web App - Environnement DEVELOPPEMENT (DEV) ---
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
output mysqlServerFqdn string = '${mysqlServer.name}.mysql.database.azure.com'

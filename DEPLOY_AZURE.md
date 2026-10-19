# Guide de Déploiement Azure & GitHub Actions - GG'Événements

Ce guide explique comment déployer l'application **GG'Événements** (Spring Boot + React) sur **Azure App Service** avec une base de données **Azure Database for MySQL Flexible Server** et un pipeline de CI/CD **GitHub Actions** pour les environnements `dev` et `production`.

---

## 🏛️ Architecture & Noms des Ressources Azure

Toutes les ressources utilisent le nom de l'application : **`gg-evenements`**

| Ressource Azure | Nom de la ressource | Description |
|---|---|---|
| **Groupe de ressources** | `rg-gg-evenements` | Contient toutes les ressources Azure du projet |
| **App Service Plan** | `asp-gg-evenements` | Plan Linux (Java 17, Niveau B1) |
| **App Service (Production)** | `gg-evenements` | Application web de prod (`https://gg-evenements.azurewebsites.net`) |
| **App Service (Dev)** | `gg-evenements-dev` | Application web de dev (`https://gg-evenements-dev.azurewebsites.net`) |
| **MySQL Flexible Server** | `mysql-gg-evenements` | Serveur MySQL 8 Azure (`mysql-gg-evenements.mysql.database.azure.com`) |
| **Base de données MySQL** | `ggevenements` | Base de données principale |

---

## ⚙️ Environnements disponibles dans l'Application

1. **Environnement Local / Dev (H2 en mémoire ou MySQL)** :
   * Activé via le profil Spring : `spring.profiles.active=dev`
   * Inclus la console H2 interactive : `http://localhost:8080/h2-console`
2. **Environnement Azure (Production / Staging)** :
   * Activé via le profil Spring : `spring.profiles.active=azure`
   * Utilise la base **Azure Database for MySQL Flexible Server** avec connexion sécurisée SSL.

---

## 🚀 Étape 1 : Créer les ressources Azure (Automatisé avec Bicep)

Exécutez le script d'infrastructure fourni dans le dossier `infra/` :

```bash
# Se connecter à Azure CLI
az login

# Lancer le déploiement de l'infrastructure
bash infra/deploy.sh
```

Le script crée automatiquement le groupe de ressources, le plan App Service Linux, le serveur MySQL Azure, et les deux applications Web (Prod et Dev).

---

## 🔐 Étape 2 : Configuration du Dépôt GitHub

### 1. Créer le dépôt sur GitHub et pousser le code
```bash
# Ajouter le remote GitHub
git remote add origin https://github.com/VOTRE_COMPTE/gg-evenements.git

# Pousser la branche principale (main)
git push -u origin main

# Pousser la branche de développement (dev)
git push -u origin dev
```

### 2. Ajouter les Secrets et Variables dans GitHub

Allez dans votre dépôt GitHub : **Settings > Secrets and variables > Actions**

#### 🔑 Secrets GitHub (`New repository secret`) :

| Nom du Secret | Valeur |
|---|---|
| `AZURE_WEBAPP_PUBLISH_PROFILE` | Contenu du fichier `publish-profile-prod.xml` généré par le script |
| `AZURE_WEBAPP_PUBLISH_PROFILE_DEV` | Contenu du fichier `publish-profile-dev.xml` généré par le script |

#### 🌐 Variables GitHub (`New repository variable`) :

| Nom de la Variable | Valeur |
|---|---|
| `AZURE_WEBAPP_NAME` | `gg-evenements` |
| `AZURE_WEBAPP_NAME_DEV` | `gg-evenements-dev` |

---

## 🔄 Étape 3 : Fonctionnement des Pipelines GitHub Actions

Deux workflows automatiques sont configurés dans `.github/workflows/` :

### 1. Workflow de Déploiement Production (`.github/workflows/deploy-azure-prod.yml`)
* **Déclenchement** : Automatique lors d'un `push` sur la branche `main`.
* **Actions** :
  1. Compiles le frontend React (Vite) et le backend Java (Spring Boot) en un seul fichier JAR.
  2. Exécute la suite de tests automatisés Maven.
  3. Déploie le package sur l'App Service Azure **`gg-evenements`**.

### 2. Workflow d'Intégration Dev (`.github/workflows/ci-dev.yml`)
* **Déclenchement** : Automatique lors d'un `push` sur la branche `dev` ou lors d'une `Pull Request`.
* **Actions** :
  1. Compile et exécute les tests avec le profil **`dev`**.
  2. Déploie automatiquement sur l'environnement de staging/dev **`gg-evenements-dev`**.

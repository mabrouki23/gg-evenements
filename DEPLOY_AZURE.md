# Guide de Déploiement Azure & GitHub Actions - GG'Événements

Ce guide explique le fonctionnement du déploiement de l'application **GG'Événements** (Spring Boot + React) sur **Azure App Service** avec la base de données **Azure SQL Database** et le pipeline de CI/CD **GitHub Actions** pour les branches `main` et `dev`.

---

## 🏛️ Architecture & Noms des Ressources Azure

Toutes les ressources utilisent le nom de l'application : **`gg-evenements`**

| Ressource Azure | Nom de la ressource | Description |
|---|---|---|
| **Groupe de ressources** | `rg-gg-evenements` | Contient toutes les ressources Azure du projet |
| **App Service Plan** | `asp-gg-evenements` | Plan Linux (Java 17, Niveau B1) |
| **App Service (Production)** | `gg-evenements` | Application web (`https://gg-evenements.azurewebsites.net`) |
| **Serveur Azure SQL** | `sql-gg-evenements` | Serveur de base de données (`sql-gg-evenements.database.windows.net`) |
| **Base de données SQL** | `ggevenements` | Base de données principale |

---

## ⚙️ Environnements disponibles dans l'Application

1. **Environnement Local / Dev** :
   * Activé via le profil Spring : `spring.profiles.active=dev`
   * Console H2 interactive : `http://localhost:8080/h2-console`
2. **Environnement Azure** :
   * Activé via le profil Spring : `spring.profiles.active=azure`
   * Connecté à la base de données **Azure SQL Database**.

---

## 🚀 Étape 1 : Créer les ressources Azure (Automatisé avec Bicep / CLI)

Exécutez le script d'infrastructure fourni dans le dossier `infra/` :

```bash
# Se connecter à Azure CLI
az login

# Lancer le déploiement de l'infrastructure
bash infra/deploy.sh
```

---

## 🔐 Étape 2 : Configuration du Dépôt GitHub

### 1. Dépôt GitHub et branches
```bash
# Ajouter le remote GitHub
git remote add origin https://github.com/mabrouki23/gg-evenements.git

# Pousser les branches main et dev
git push -u origin main
git push -u origin dev
```

### 2. Secrets et Variables GitHub Actions

Dans GitHub : **Settings > Secrets and variables > Actions**

#### 🔑 Secrets GitHub :
* `AZURE_WEBAPP_PUBLISH_PROFILE` : Profil de publication pour l'application Web.

#### 🌐 Variables GitHub :
* `AZURE_WEBAPP_NAME` : `gg-evenements`

---

## 🔄 Étape 3 : Fonctionnement des Pipelines GitHub Actions

Les workflows automatiques sont configurés dans `.github/workflows/` :

1. **`deploy-azure-prod.yml`** (Branche `main`) :
   * Compiles le frontend React (Vite) et le backend Java (Spring Boot) en un seul fichier JAR.
   * Exécute les tests automatisés Maven.
   * Déploie l'application sur Azure App Service **`gg-evenements`**.

2. **`ci-dev.yml`** (Branche `dev`) :
   * Valide la compilation et exécute la suite de tests en mode dev.

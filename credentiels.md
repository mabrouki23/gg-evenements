# Identifiants et Accès - Application GG'Événements

Ce fichier récapitule l'ensemble des identifiants et accès pour l'application **GG'Événements** (Local, GitHub, Azure App Service et Azure SQL Database).

---

## 🌐 1. Liens de l'Application et Dépôt Code Source

* **Application Web Azure (Production) :**
  👉 [https://gg-evenements.azurewebsites.net](https://gg-evenements.azurewebsites.net)
* **Dépôt GitHub (CI/CD) :**
  👉 [https://github.com/mabrouki23/gg-evenements.git](https://github.com/mabrouki23/gg-evenements.git)

---

## 🔑 2. Comptes de Test de l'Application (Se connecter sur l'interface)

Ces comptes de démonstration sont pré-chargés au premier démarrage de l'application :

| Profil / Rôle | Courriel | Mot de passe |
|---|---|---|
| **Administrateur** | `admin@ggevenements.ca` | `Admin123!` |
| **Organisateur** *(Club d'échecs, Asso. info)* | `organisateur@ggevenements.ca` | `Organisateur123!` |
| **Organisateur** *(Club de plein air)* | `organisateur2@ggevenements.ca` | `Organisateur123!` |
| **Étudiant / Public** | `etudiant1@ggevenements.ca` | `Etudiant123!` |
| **Étudiant / Public (2)** | `etudiant2@ggevenements.ca` | `Etudiant123!` |
| **Étudiant / Public (3)** | `etudiant3@ggevenements.ca` | `Etudiant123!` |
| **Compte désactivé** *(Test message)* | `inactif@ggevenements.ca` | `Etudiant123!` |

---

## 🗄️ 3. Base de Données Azure SQL (Production Cloud)

* **Hôte du serveur Azure SQL :** `sql-gg-evenements.database.windows.net`
* **Port :** `1433`
* **Nom de la base de données :** `ggevenements`
* **Nom d'utilisateur administrateur :** `gguser`
* **Mot de passe administrateur :** `Cgodin2026!SecuredPass`
* **Groupe de ressources Azure :** `rg-gg-evenements`
* **Région Azure :** `canadacentral` (Canada)

---

## 💻 4. Environnement de Développement Local (DEV)

* **URL de l'application locale :** `http://localhost:8080`
* **Console H2 interactive :** `http://localhost:8080/h2-console`
  * **JDBC URL :** `jdbc:h2:mem:ggevenementsdev;DB_CLOSE_DELAY=-1;MODE=MySQL`
  * **Utilisateur :** `sa`
  * **Mot de passe :** *(laisser vide)*

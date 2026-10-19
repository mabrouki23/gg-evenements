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

## 📌 Règles de gestion : Création d'événements et Rôles

Dans GG’Événements, seul le rôle Organisateur peut créer un événement, et il doit en plus être responsable d’au moins un club actif. L’étudiant (rôle Public) n’a pas accès à la création, et l’administrateur non plus, car l’énoncé précise qu’il supervise la plateforme sans créer d’événements à la place des organisateurs ; c’est pourquoi le lien de création n’apparaît pas avec le compte administrateur. Pour créer un événement, il faut se connecter avec un compte organisateur, par exemple organisateur@ggevenements.ca (mot de passe Organisateur123!), cliquer sur « Mes événements à organiser » dans la barre de navigation, puis sur « Créer un événement ». Ce compte dirige deux clubs, donc il doit d’abord choisir le club actif dans la liste, puis cliquer sur « Choisir ce club » ; avec un compte qui n’a qu’un seul club, comme organisateur2@ggevenements.ca, le club est choisi automatiquement. Le club n’est jamais un champ du formulaire : le serveur le déduit du compte connecté et refuse tout club qui n’appartient pas à l’organisateur. Pour donner cette compétence à une autre personne, l’administrateur doit aller dans Administration, puis Clubs, et créer ou modifier un club en désignant cette personne comme responsable : elle reçoit alors automatiquement le rôle Organisateur et peut créer des événements pour ce club.

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

# Documentation des API REST - GG'Événements

Ce document décrit l'ensemble des points de terminaison (*endpoints*) de l'API REST Spring Boot de l'application **GG'Événements**, leur rôle applicatif et les raisons d'affaires de leur existence.

---

## 🔒 1. Authentification, Session et Sécurité

| Méthode | Route | Accès | Rôle | Pourquoi / Explication d'affaires |
|---|---|---|---|---|
| `GET` | `/api/csrf` | Public | Générer et récupérer le jeton CSRF. | Protège l'application contre les attaques de type *Cross-Site Request Forgery*. La méthode initialise le cookie `XSRF-TOKEN` que l'application React lit et renvoie automatiquement dans l'en-tête HTTP `X-XSRF-TOKEN` lors des requêtes `POST`, `PUT` et `DELETE`. |
| `POST` | `/api/login` | Public | Authentifier un utilisateur. | Traité par Spring Security. Vérifie l'adresse courriel et le mot de passe encodé en BCrypt. En cas de succès, crée la session HTTP (`JSESSIONID`) et renvoie le profil utilisateur (HTTP 200). En cas d'échec ou si le compte est désactivé, renvoie une réponse JSON d'erreur explicite (HTTP 401). |
| `POST` | `/api/logout` | Connecté | Déconnecter l'utilisateur. | Traité par Spring Security. Invalide la session HTTP courante et supprime le cookie de session `JSESSIONID` côté client (HTTP 204). |
| `GET` | `/api/me` | Connecté | Consulter l'utilisateur connecté. | Permet au frontend React de vérifier si une session est active lors du chargement de la page et de récupérer le nom complet et les rôles de l'utilisateur pour adapter le menu d'en-tête. |

---

## 📚 2. Consultation Publique et Référentiels (Élèves & Tous)

| Méthode | Route | Accès | Rôle | Pourquoi / Explication d'affaires |
|---|---|---|---|---|
| `GET` | `/api/reference` | Connecté | Charger les catégories, lieux et clubs actifs. | Optimisation : permet au frontend de charger en une seule requête HTTP combinée les listes déroulantes nécessaires à la recherche et à l'affichage des filtres. |
| `GET` | `/api/evenements` | Connecté | Lister et rechercher les événements à venir (paginé). | Sert à la fois pour la liste principale et la recherche avancée. Accepte les paramètres facultatifs (`q` pour la recherche textuelle, `categorie`, `club`, `du`, `au` pour les dates, `tri` et `page`). Renvoie 9 événements par page. |
| `GET` | `/api/evenements/{id}` | Connecté | Obtenir les détails complets d'un événement. | Affiche la fiche détaillée d'un événement (description, affiche, lieu, organisateur, club, capacité, places restantes, statut de l'inscription de l'utilisateur et avis des participants). |
| `GET` | `/api/clubs/{id}` | Connecté | Consulter la fiche publique d'un club. | Permet d'afficher les informations d'un club, son responsable et la liste de tous ses événements à venir. |

---

## ✍️ 3. Inscriptions, Désinscriptions et Avis

| Méthode | Route | Accès | Rôle | Pourquoi / Explication d'affaires |
|---|---|---|---|---|
| `POST` | `/api/evenements/{id}/inscription` | Connecté | S'inscrire à un événement. | Gère la règle de gestion de capacité : si l'événement a des places disponibles, le statut passe à `CONFIRMEE` (HTTP 201) ; si l'événement est complet, l'utilisateur est automatiquement placé sur la liste d'attente (`EN_ATTENTE`). |
| `DELETE` | `/api/evenements/{id}/inscription` | Connecté | Se désinscrire d'un événement. | Annule l'inscription de l'utilisateur (HTTP 204). Si une place se libère, une transaction atomique promeut immédiatement la plus ancienne personne sur la liste d'attente au statut `CONFIRMEE`. |
| `GET` | `/api/mes-evenements` | Connecté | Lister ses propres événements. | Permet à l'étudiant de consulter ses inscriptions futures et passées. |
| `POST` | `/api/evenements/{id}/avis` | Connecté | Laisser une note et un commentaire. | Permet à un participant d'évaluer un événement auquel il a assisté (la règle d'affaires valide que l'événement est passé et que l'utilisateur y a participé). |

---

## 🎯 4. Espace Organisateur (`/api/organisateur`)

| Méthode | Route | Accès | Rôle | Pourquoi / Explication d'affaires |
|---|---|---|---|---|
| `GET` | `/api/organisateur/clubs` | `ORGANISATEUR` | Obtenir les clubs gérés par l'organisateur. | Supporte les organisateurs responsables de plusieurs clubs. Retourne la liste de ses clubs et indique le club actuellement sélectionné pour la création. |
| `POST` | `/api/organisateur/club-actif` | `ORGANISATEUR` | Sélectionner le club actif en session. | Permet à un organisateur gérant plusieurs clubs de choisir au nom de quel club il travaille. Le serveur vérifie systématiquement qu'il en est bien le responsable (HTTP 403 sinon). |
| `GET` | `/api/organisateur/evenements` | `ORGANISATEUR` | Lister les événements de ses clubs. | Affiche le tableau de bord de l'organisateur avec l'ensemble des événements créés sous sa responsabilité. |
| `POST` | `/api/organisateur/evenements` | `ORGANISATEUR` | Créer un nouvel événement (`multipart`). | Seul le rôle Organisateur peut créer un événement. Le serveur déduit automatiquement le club à partir de la session organisateur (le club n'est jamais un champ du formulaire pour éviter toute falsification). |
| `GET` | `/api/organisateur/evenements/{id}/formulaire` | `ORGANISATEUR` | Charger les données d'édition d'un événement. | Prépare le formulaire de modification. Vérifie strictement l'appartenance du club (HTTP 403 si un organisateur tente d'éditer l'événement d'un autre club). |
| `POST` | `/api/organisateur/evenements/{id}/modifier` | `ORGANISATEUR` | Modifier un événement et son affiche (`multipart`). | Permet la mise à jour des détails d'un événement. |
| `POST` | `/api/organisateur/evenements/{id}/annuler` | `ORGANISATEUR` | Annuler un événement. | Modifie le statut de l'événement à `ANNULE` (HTTP 204). L'événement n'est pas supprimé de la BDD afin de conserver l'historique et d'avertir les inscrits. |
| `GET` | `/api/organisateur/evenements/{id}/inscriptions` | `ORGANISATEUR` | Lister les personnes inscrites à un événement. | Fournit à l'organisateur la liste d'émargement (confirmés et liste d'attente) pour contrôler les présences le jour de l'événement. |
| `POST` | `/api/organisateur/inscriptions/{id}/presence` | `ORGANISATEUR` | Pointer la présence d'un participant. | Permet d'enregistrer si la personne inscrite s'est présentee ou non à l'activité. |

---

## 🛠️ 5. Espace Administration (`/api/admin`)

| Méthode | Route | Accès | Rôle | Pourquoi / Explication d'affaires |
|---|---|---|---|---|
| `GET` | `/api/admin/utilisateurs` | `ADMIN` | Lister tous les utilisateurs. | Permet à l'administration de superviser l'ensemble des comptes de la plateforme. |
| `POST` | `/api/admin/utilisateurs/{id}/actif` | `ADMIN` | Activer ou désactiver un compte. | Permet de bloquer temporairement ou définitivement un compte utilisateur. |
| `POST` | `/api/admin/utilisateurs/{id}/organisateur` | `ADMIN` | Assigner/Révoquer le rôle Organisateur. | Permet la gestion directe des droits d'organisation. |
| `GET` | `/api/admin/clubs` | `ADMIN` | Lister tous les clubs (actifs et inactifs). | Supervision et gestion du répertoire des clubs de l'établissement. |
| `POST` | `/api/admin/clubs` | `ADMIN` | Créer un nouveau club. | L'administrateur désigne le responsable du club. La personne désignée reçoit automatiquement le rôle `ORGANISATEUR`. |
| `PUT` | `/api/admin/clubs/{id}` | `ADMIN` | Modifier un club ou changer son responsable. | Mise à jour des informations d'un club ou transfert de responsabilité à un autre utilisateur. |
| `POST` | `/api/admin/clubs/{id}/actif` | `ADMIN` | Activer/Désactiver un club. | Permet d'archiver un club inactif sans supprimer son historique d'événements. |
| `GET` | `/api/admin/categories` | `ADMIN` | Lister les catégories d'événements. | Gestion du référentiel des thématiques (Ex: Sport, Culture, Jeux). |
| `POST` | `/api/admin/categories` | `ADMIN` | Créer une catégorie. | Ajout d'un nouveau type d'activité. |
| `PUT` | `/api/admin/categories/{id}` | `ADMIN` | Modifier une catégorie. | Renommage ou ajustement de la description d'une catégorie. |
| `DELETE` | `/api/admin/categories/{id}` | `ADMIN` | Supprimer une catégorie. | La suppression est refusée avec une erreur claire (HTTP 422) si au moins un événement est associé à cette catégorie. |
| `GET` | `/api/admin/lieux` | `ADMIN` | Lister les lieux disponibles. | Référentiel des locaux, salles et espaces extérieurs. |
| `POST` | `/api/admin/lieux` | `ADMIN` | Créer un lieu. | Ajout d'une nouvelle salle ou espace disponible pour les activités. |
| `PUT` | `/api/admin/lieux/{id}` | `ADMIN` | Modifier un lieu. | Ajustement du nom ou de l'adresse d'un emplacement. |
| `DELETE` | `/api/admin/lieux/{id}` | `ADMIN` | Supprimer un lieu. | Retrait d'un lieu du référentiel. |
| `GET` | `/api/admin/stats` | `ADMIN` | Consulter le tableau de bord statistique. | Génère les indicateurs clés de la vie étudiante (nombre total d'événements, taux d'occupation, volume d'inscriptions, moyenne des avis, clubs les plus actifs). |

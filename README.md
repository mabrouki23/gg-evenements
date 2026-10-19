# GG'Événements — Spring Boot + React dans un seul projet

Un seul projet Maven : l'**API REST Spring Boot** et l'**interface React** (Vite). Maven construit tout, y compris React, et Spring Boot sert l'interface compilée : une seule application sur `http://localhost:8080`.

```
gg-evenements/
├── pom.xml                      Maven : Spring Boot + frontend-maven-plugin (construit React)
├── frontend/                    Application React (Vite) — code source
│   ├── package.json, vite.config.js, index.html
│   ├── src/                     pages, composants, client API
│   ├── public/                  fichiers copiés tels quels (images/defaut.svg)
│   └── tests/                   tests d'interface (vitest, facultatifs)
├── src/main/java/…              Spring Boot : api/ (REST), service/, repository/, model/, security/…
├── src/main/resources/          application.properties, messages.properties
├── src/test/java/…              tests Java
├── database/schema.sql          schéma MySQL officiel
├── scripts/                     tests de fumée de l'API (curl)
└── target/classes/static/       ← GÉNÉRÉ par Maven : l'interface React compilée (jamais dans src/)
```

## Où vont les fichiers de React ?

* **Source** : `frontend/` (c'est là qu'on modifie React).
* **Résultat de la compilation** : `target/classes/static/` (`index.html` + `assets/*.js|css` + `images/`). Cet emplacement est le dossier statique de Spring Boot : il est servi par `spring-boot:run` et copié dans le `.jar` final (`BOOT-INF/classes/static`). Rien de généré n'est écrit dans `src/`, donc rien à commiter ni à nettoyer.
* **Comment** : dans le `pom.xml`, `frontend-maven-plugin` exécute, avant la compilation Java (phase `generate-resources`) : télécharger Node.js dans `.node/` → `npm install` → `npm run build`. `vite.config.js` fixe `outDir` à `../target/classes/static`.

## Démarrage

Prérequis : **Java 17+, Maven, MySQL 8** (Node.js n'est *pas* requis : Maven le télécharge ; premier build un peu plus long).

```bash
mysql -u root -p < database/schema.sql          # base et tables (une fois)
# adapter spring.datasource.username/password dans src/main/resources/application.properties
mvn spring-boot:run                              # construit React + démarre : http://localhost:8080
mvn clean package                                # jar complet : target/gg-evenements-1.0.0.jar (java -jar …)
```

### Développement du frontend (rechargement à chaud)

```bash
mvn spring-boot:run -P sans-frontend             # API seule (ignore l'étape npm)
cd frontend && npm install && npm run dev        # http://localhost:5173 (relaie /api et /uploads vers 8080)
```

`-P sans-frontend` ignore Node/npm pour un démarrage Maven plus rapide quand on ne touche pas à React (l'interface compilée précédente reste dans `target/classes/static` tant qu'on ne fait pas `mvn clean`).

## Ce qui a changé par rapport à la version Thymeleaf

Contrôleurs MVC → contrôleurs REST (`src/main/java/.../api`, JSON) ; gabarits Thymeleaf → pages React ; jeton CSRF caché dans les formulaires → cookie `XSRF-TOKEN` renvoyé en-tête `X-XSRF-TOKEN` ; redirections et pages d'erreur HTML → réponses JSON `401` / `403` / `404` / `422`. **Inchangés** : services, repositories, entités, schéma MySQL, règles d'affaires (verrou, promotion de la liste d'attente), BCrypt, rôles, contrôle d'appartenance au club (403) dans les services. La sécurité reste côté serveur : React masque seulement les liens non permis.

## Comptes de test

| Profil | Courriel | Mot de passe |
|---|---|---|
| Administrateur | admin@ggevenements.ca | Admin123! |
| Organisateur (2 clubs : Club d'échecs, Association étudiante en informatique) | organisateur@ggevenements.ca | Organisateur123! |
| Organisateur (Club de plein air) | organisateur2@ggevenements.ca | Organisateur123! |
| Public | etudiant1@ggevenements.ca … etudiant5@ggevenements.ca | Etudiant123! |
| Compte désactivé (pour voir le message) | inactif@ggevenements.ca | Etudiant123! |

## Règles de gestion : Création d'événements et Rôles

Dans GG’Événements, seul le rôle Organisateur peut créer un événement, et il doit en plus être responsable d’au moins un club actif. L’étudiant (rôle Public) n’a pas accès à la création, et l’administrateur non plus, car l’énoncé précise qu’il supervise la plateforme sans créer d’événements à la place des organisateurs ; c’est pourquoi le lien de création n’apparaît pas avec le compte administrateur. Pour créer un événement, il faut se connecter avec un compte organisateur, par exemple organisateur@ggevenements.ca (mot de passe Organisateur123!), cliquer sur « Mes événements à organiser » dans la barre de navigation, puis sur « Créer un événement ». Ce compte dirige deux clubs, donc il doit d’abord choisir le club actif dans la liste, puis cliquer sur « Choisir ce club » ; avec un compte qui n’a qu’un seul club, comme organisateur2@ggevenements.ca, le club est choisi automatiquement. Le club n’est jamais un champ du formulaire : le serveur le déduit du compte connecté et refuse tout club qui n’appartient pas à l’organisateur. Pour donner cette compétence à une autre personne, l’administrateur doit aller dans Administration, puis Clubs, et créer ou modifier un club en désignant cette personne comme responsable : elle reçoit alors automatiquement le rôle Organisateur et peut créer des événements pour ce club.

Scénario de démonstration : « Soirée jeux de société » est complète (2/2) avec 2 personnes en attente. Avec `etudiant1`, se désinscrire : `etudiant3` (la plus ancienne inscription en attente) passe à « Confirmé ». Avec `etudiant3` : *Mes événements › Passés › Laisser un avis*.

## Écrans (React Router)

| Route | Écran | Accès |
|---|---|---|
| `/login` | Connexion | public |
| `/evenements` | Liste des événements à venir (9 par page, tri, filtres) | connecté |
| `/recherche` | Recherche avancée (titre, catégorie, club, dates) | connecté |
| `/evenements/:id` | Détail, S'inscrire / Se désinscrire | connecté |
| `/mes-evenements`, `/evenements/:id/avis` | Mes événements, laisser un avis | connecté |
| `/clubs/:id` | Fiche publique d'un club | connecté |
| `/organisateur/evenements`, `/nouveau`, `/:id/modifier`, `/:id/inscriptions` | Espace organisateur | ORGANISATEUR |
| `/admin/tableau-de-bord`, `utilisateurs`, `clubs`, `categories`, `lieux` | Espace administrateur | ADMIN |

## API REST (résumé)

Authentification : session (cookie `JSESSIONID`, non lisible par JavaScript) + CSRF (cookie `XSRF-TOKEN` → en-tête `X-XSRF-TOKEN` sur tout POST/PUT/DELETE).

| Méthode et route | Rôle | Description |
|---|---|---|
| `GET /api/csrf`, `GET /api/me` | public | jeton CSRF ; utilisateur connecté (401 sinon) |
| `POST /api/login` (courriel, motDePasse), `POST /api/logout` | public | 200 + utilisateur / 401 `{code: INVALID \| DISABLED}` ; 204 |
| `GET /api/reference` | connecté | catégories, clubs actifs, lieux |
| `GET /api/evenements?q&categorie&club&du&au&tri&page` | connecté | liste paginée (liste + recherche avancée) |
| `GET /api/evenements/{id}` | connecté | détail + situation de l'utilisateur + avis |
| `POST` / `DELETE /api/evenements/{id}/inscription` | connecté | s'inscrire (201 `{statut}`) / se désinscrire (204) |
| `GET /api/mes-evenements`, `POST /api/evenements/{id}/avis` | connecté | mes événements ; laisser un avis |
| `GET /api/clubs/{id}` | connecté | fiche d'un club + événements à venir |
| `/api/organisateur/**` | ORGANISATEUR | clubs et club actif, événements (création/modification en multipart), annulation, inscriptions, présence |
| `/api/admin/**` | ADMIN | utilisateurs, clubs, catégories, lieux, statistiques |

Codes d'erreur : `400` validation (`errors` par champ), `401` non connecté, `403` accès refusé (rôle ou club), `404` introuvable, `422` règle d'affaires violée (message clair, champ concerné si applicable).

## Tests

* **Backend** : `cd mvn test` (H2 en mémoire) — `ApiSecuriteTest` (401/403 JSON, connexion, CSRF, rôles), `RegleAffairesTest` (capacité, doublon, promotion, 403 inter-clubs, concurrence…), chargement du contexte.
* **Interface (bout en bout)** : démarrer le backend avec une base de démonstration NEUVE, puis `cd frontend && npm install && npm run test:e2e` — 12 scénarios (connexion, liste/pagination/filtres, recherche, inscription/désinscription, avis, accès refusé, déconnexion, tableau de bord, CRUD, création d'événement) exécutés dans jsdom contre le vrai backend.
* **API (curl)** : `scripts/smoke-1-public.sh` puis `scripts/smoke-2-roles.sh` (backend neuf ; argument facultatif : URL, 8080 par défaut).

## Ce qui a été vérifié, et comment

L'API a été démarrée pour de vrai (Spring Boot + base H2 en mémoire) et exercée avec les deux scripts curl (82 vérifications). Dans la disposition **à projet unique**, l'interface a été compilée par Vite vers `target/classes/static`, servie par Spring Boot (routes React, fichiers statiques, `/api`), et les 12 scénarios d'interface passent contre ce backend.

**Pas vérifié ici :**
* l'exécution de Maven lui-même : l'environnement de préparation n'avait pas accès à Maven Central, donc la configuration de `frontend-maven-plugin` dans le `pom.xml` (téléchargement de Node, `npm install`, `npm run build`) n'a pas pu être lancée — seules les commandes équivalentes (`npm install`, `npm run build`) l'ont été ;
* l'exécution sur MySQL (la version Thymeleaf d'origine y tournait ; les parties communes sont inchangées) ;
* les tests Java (`mvn test`).

## Dépannage

| Symptôme | Cause probable |
|---|---|
| Page blanche sur `localhost:8080` | l'interface n'est pas compilée : relancer `mvn spring-boot:run` (sans `-P sans-frontend`) ou `cd frontend && npm run build` |
| `403` sur les actions (inscription, etc.) | cookie `XSRF-TOKEN` absent : recharger la page (il est créé au chargement via `/api/me`) |
| Affiches introuvables (404 sur `/uploads/...`) | lancer Maven depuis la racine du projet (le dossier `uploads/` est relatif) |
| `401` partout après un moment | session expirée : se reconnecter |

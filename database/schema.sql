-- ============================================================================
-- GG'Événements — Schéma de la base de données (9 tables · MySQL 8)
-- Conforme au document « Schéma de la base de données » du projet final.
-- Exécution :  mysql -u root -p < database/schema.sql
-- ============================================================================
CREATE DATABASE IF NOT EXISTS ggevenements
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
USE ggevenements;

-- 1. role -------------------------------------------------------------------
CREATE TABLE role (
  id_role      INT          NOT NULL AUTO_INCREMENT,
  nom_role     VARCHAR(30)  NOT NULL,            -- PUBLIC, ORGANISATEUR ou ADMIN
  description  VARCHAR(150) NULL,
  PRIMARY KEY (id_role)
) ENGINE=InnoDB;

-- 2. utilisateur ------------------------------------------------------------
CREATE TABLE utilisateur (
  id_utilisateur   INT          NOT NULL AUTO_INCREMENT,
  prenom           VARCHAR(50)  NOT NULL,
  nom              VARCHAR(50)  NOT NULL,
  courriel         VARCHAR(100) NOT NULL,        -- unique, identifiant de connexion
  mot_passe        VARCHAR(60)  NOT NULL,        -- hachage BCrypt (60 caractères)
  actif            TINYINT(1)   NOT NULL DEFAULT 1,
  date_inscription DATETIME     NOT NULL,
  PRIMARY KEY (id_utilisateur),
  UNIQUE KEY uk_utilisateur_courriel (courriel)
) ENGINE=InnoDB;

-- 3. utilisateur_role (table associative, clé primaire composite) -----------
CREATE TABLE utilisateur_role (
  id_utilisateur INT NOT NULL,
  id_role        INT NOT NULL,
  PRIMARY KEY (id_utilisateur, id_role),
  CONSTRAINT fk_ur_utilisateur FOREIGN KEY (id_utilisateur) REFERENCES utilisateur (id_utilisateur),
  CONSTRAINT fk_ur_role        FOREIGN KEY (id_role)        REFERENCES role (id_role)
) ENGINE=InnoDB;

-- 4. lieu -------------------------------------------------------------------
CREATE TABLE lieu (
  id_lieu      INT          NOT NULL AUTO_INCREMENT,
  nom          VARCHAR(80)  NOT NULL,
  campus       VARCHAR(50)  NOT NULL,
  adresse      VARCHAR(150) NOT NULL,
  capacite_max INT          NOT NULL,
  PRIMARY KEY (id_lieu)
) ENGINE=InnoDB;

-- 5. categorie --------------------------------------------------------------
CREATE TABLE categorie (
  id_categorie INT          NOT NULL AUTO_INCREMENT,
  nom          VARCHAR(50)  NOT NULL,
  description  VARCHAR(150) NULL,
  couleur      VARCHAR(7)   NOT NULL,            -- hexadécimal, ex. #1F3864
  PRIMARY KEY (id_categorie)
) ENGINE=InnoDB;

-- 6. club -------------------------------------------------------------------
CREATE TABLE club (
  id_club          INT          NOT NULL AUTO_INCREMENT,
  nom              VARCHAR(80)  NOT NULL,
  description      VARCHAR(300) NULL,
  courriel_contact VARCHAR(100) NOT NULL,
  logo             VARCHAR(150) NULL,
  id_responsable   INT          NOT NULL,
  actif            TINYINT(1)   NOT NULL DEFAULT 1,
  PRIMARY KEY (id_club),
  CONSTRAINT fk_club_responsable FOREIGN KEY (id_responsable) REFERENCES utilisateur (id_utilisateur)
) ENGINE=InnoDB;

-- 7. evenement --------------------------------------------------------------
CREATE TABLE evenement (
  id_evenement  INT           NOT NULL AUTO_INCREMENT,
  titre         VARCHAR(120)  NOT NULL,
  description   VARCHAR(1000) NOT NULL,
  id_club       INT           NOT NULL,
  id_categorie  INT           NOT NULL,
  id_lieu       INT           NOT NULL,
  id_createur   INT           NOT NULL,
  date_debut    DATETIME      NOT NULL,
  date_fin      DATETIME      NOT NULL,
  capacite_max  INT           NOT NULL,
  statut        VARCHAR(20)   NOT NULL,          -- PLANIFIE, ANNULE ou TERMINE
  image_affiche VARCHAR(150)  NULL,
  date_creation DATETIME      NOT NULL,
  PRIMARY KEY (id_evenement),
  CONSTRAINT fk_evt_club      FOREIGN KEY (id_club)      REFERENCES club (id_club),
  CONSTRAINT fk_evt_categorie FOREIGN KEY (id_categorie) REFERENCES categorie (id_categorie),
  CONSTRAINT fk_evt_lieu      FOREIGN KEY (id_lieu)      REFERENCES lieu (id_lieu),
  CONSTRAINT fk_evt_createur  FOREIGN KEY (id_createur)  REFERENCES utilisateur (id_utilisateur)
) ENGINE=InnoDB;

-- 8. inscription (classe d'association : porte le statut / la liste d'attente)
CREATE TABLE inscription (
  id_inscription     INT         NOT NULL AUTO_INCREMENT,
  id_evenement       INT         NOT NULL,
  id_utilisateur     INT         NOT NULL,
  date_inscription   DATETIME    NOT NULL,       -- détermine aussi l'ordre de la liste d'attente
  statut             VARCHAR(20) NOT NULL,       -- CONFIRMEE ou EN_ATTENTE
  presence_confirmee TINYINT(1)  NOT NULL DEFAULT 0,
  PRIMARY KEY (id_inscription),
  CONSTRAINT fk_ins_evenement   FOREIGN KEY (id_evenement)   REFERENCES evenement (id_evenement),
  CONSTRAINT fk_ins_utilisateur FOREIGN KEY (id_utilisateur) REFERENCES utilisateur (id_utilisateur),
  -- AJOUT DÉFENSIF (non listé dans le document) : un utilisateur ne s'inscrit qu'une fois à un événement.
  CONSTRAINT uk_inscription_utilisateur_evenement UNIQUE (id_utilisateur, id_evenement)
) ENGINE=InnoDB;

-- 9. avis -------------------------------------------------------------------
CREATE TABLE avis (
  id_avis        INT          NOT NULL AUTO_INCREMENT,
  id_evenement   INT          NOT NULL,
  id_utilisateur INT          NOT NULL,
  note           TINYINT      NOT NULL,          -- de 1 à 5
  commentaire    VARCHAR(500) NULL,
  date_avis      DATETIME     NOT NULL,
  PRIMARY KEY (id_avis),
  CONSTRAINT fk_avis_evenement   FOREIGN KEY (id_evenement)   REFERENCES evenement (id_evenement),
  CONSTRAINT fk_avis_utilisateur FOREIGN KEY (id_utilisateur) REFERENCES utilisateur (id_utilisateur),
  -- AJOUT DÉFENSIF (non listé dans le document) : un seul avis par utilisateur et par événement.
  CONSTRAINT uk_avis_utilisateur_evenement UNIQUE (id_utilisateur, id_evenement)
) ENGINE=InnoDB;

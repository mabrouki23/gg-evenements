package ca.qc.cgodin.ggevenements.config;

import ca.qc.cgodin.ggevenements.model.*;
import ca.qc.cgodin.ggevenements.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Initialisation au démarrage :
 *  - toujours : les trois rôles + un compte administrateur si la base ne contient aucun utilisateur;
 *  - si app.donnees-demo=true et la base est vide : catégories, lieux, clubs, événements, inscriptions et avis de démonstration.
 * Les mots de passe sont encodés en BCrypt avant d'être enregistrés.
 */
@Component
public class DonneesInitiales implements CommandLineRunner {

    public static final String EMAIL_ADMIN = "admin@ggevenements.ca";
    public static final String EMAIL_ORG1 = "organisateur@ggevenements.ca";
    public static final String EMAIL_ORG2 = "organisateur2@ggevenements.ca";
    public static final String EMAIL_ETU1 = "etudiant1@ggevenements.ca";
    public static final String EMAIL_ETU2 = "etudiant2@ggevenements.ca";
    public static final String EMAIL_ETU3 = "etudiant3@ggevenements.ca";
    public static final String EMAIL_INACTIF = "inactif@ggevenements.ca";
    public static final String MDP_ADMIN = "Admin123!";
    public static final String MDP_ORGANISATEUR = "Organisateur123!";
    public static final String MDP_ETUDIANT = "Etudiant123!";

    private final RoleRepository roleRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final CategorieRepository categorieRepository;
    private final LieuRepository lieuRepository;
    private final ClubRepository clubRepository;
    private final EvenementRepository evenementRepository;
    private final InscriptionRepository inscriptionRepository;
    private final AvisRepository avisRepository;
    private final PasswordEncoder passwordEncoder;
    private final boolean demo;

    public DonneesInitiales(RoleRepository roleRepository, UtilisateurRepository utilisateurRepository,
                            CategorieRepository categorieRepository, LieuRepository lieuRepository,
                            ClubRepository clubRepository, EvenementRepository evenementRepository,
                            InscriptionRepository inscriptionRepository, AvisRepository avisRepository,
                            PasswordEncoder passwordEncoder, @Value("${app.donnees-demo:true}") boolean demo) {
        this.roleRepository = roleRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.categorieRepository = categorieRepository;
        this.lieuRepository = lieuRepository;
        this.clubRepository = clubRepository;
        this.evenementRepository = evenementRepository;
        this.inscriptionRepository = inscriptionRepository;
        this.avisRepository = avisRepository;
        this.passwordEncoder = passwordEncoder;
        this.demo = demo;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // Rôles du schéma : PUBLIC, ORGANISATEUR, ADMIN
        Role rPublic = role(Role.PUBLIC, "Utilisateur authentifié standard");
        Role rOrg = role(Role.ORGANISATEUR, "Responsable d'un club, peut créer des événements");
        Role rAdmin = role(Role.ADMIN, "Membre du personnel de la Vie étudiante");

        if (utilisateurRepository.count() > 0) {
            return;
        }
        utilisateur("Sophie", "Tremblay", EMAIL_ADMIN, MDP_ADMIN, true, rPublic, rAdmin);
        if (!demo) {
            return;
        }

        Utilisateur org1 = utilisateur("Marc-André", "Gagnon", EMAIL_ORG1, MDP_ORGANISATEUR, true, rPublic, rOrg);
        Utilisateur org2 = utilisateur("Camille", "Roy", EMAIL_ORG2, MDP_ORGANISATEUR, true, rPublic, rOrg);
        Utilisateur e1 = utilisateur("Léa", "Bouchard", EMAIL_ETU1, MDP_ETUDIANT, true, rPublic);
        Utilisateur e2 = utilisateur("Nabil", "Haddad", EMAIL_ETU2, MDP_ETUDIANT, true, rPublic);
        Utilisateur e3 = utilisateur("Jade", "Lavoie", EMAIL_ETU3, MDP_ETUDIANT, true, rPublic);
        Utilisateur e4 = utilisateur("Samuel", "Pelletier", "etudiant4@ggevenements.ca", MDP_ETUDIANT, true, rPublic);
        Utilisateur e5 = utilisateur("Inès", "Benali", "etudiant5@ggevenements.ca", MDP_ETUDIANT, true, rPublic);
        utilisateur("Olivier", "Côté", EMAIL_INACTIF, MDP_ETUDIANT, false, rPublic);

        // Catégories et lieux : exemples du document de schéma
        Categorie sport = categorieRepository.save(new Categorie("Sportif", "Tournois, randonnées et activités physiques", "#2E7D32"));
        Categorie culturel = categorieRepository.save(new Categorie("Culturel", "Spectacles, cinéma, arts et sorties culturelles", "#8E24AA"));
        Categorie academique = categorieRepository.save(new Categorie("Académique", "Ateliers, conférences et activités d'apprentissage", "#1F3864"));
        Categorie social = categorieRepository.save(new Categorie("Social", "Soirées et activités sociales", "#BF8F00"));

        String pp = "Pavillon principal";
        Lieu agora = lieuRepository.save(new Lieu("Agora", pp, "Hall central, rez-de-chaussée", 300));
        Lieu gymnase = lieuRepository.save(new Lieu("Gymnase double", "Pavillon sportif", "Rez-de-chaussée, aile est", 200));
        Lieu salle = lieuRepository.save(new Lieu("Salle A-204", pp, "Étage 2, local A-204", 40));
        Lieu amphi = lieuRepository.save(new Lieu("Amphithéâtre", "Pavillon des arts", "Rez-de-chaussée, entrée principale", 150));
        Lieu cafe = lieuRepository.save(new Lieu("Café étudiant", pp, "Rez-de-chaussée, aile ouest", 60));

        // Un utilisateur peut être responsable de plusieurs clubs (comme dans l'exemple du schéma : id_responsable 2 → clubs 1 et 3)
        Club echecs = clubRepository.save(new Club("Club d'échecs",
                "Parties amicales, tournois et initiation aux échecs pour tous les niveaux.", "echecs@ggevenements.ca", org1));
        Club info = clubRepository.save(new Club("Association étudiante en informatique",
                "Ateliers techniques, conférences, hackathons et activités sociales pour les étudiants en informatique.", "info@ggevenements.ca", org1));
        Club pleinAir = clubRepository.save(new Club("Club de plein air",
                "Randonnées, sorties en nature et activités de plein air.", "pleinair@ggevenements.ca", org2));

        LocalDateTime maintenant = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0);

        // ---- Événements passés (avec avis)
        Evenement passe1 = evenement("Atelier d'initiation à Arduino", "Montage de circuits et premiers programmes, matériel fourni.",
                maintenant.minusDays(20), 3, 20, academique, salle, info, org1, StatutEvenement.TERMINE);
        Evenement passe2 = evenement("Randonnée au Mont-Royal", "Randonnée de 10 km à rythme modéré. Prévoir de bonnes chaussures et de l'eau.",
                maintenant.minusDays(9), 4, 25, sport, agora, pleinAir, org2, StatutEvenement.TERMINE);
        inscrire(e1, passe1, StatutInscription.CONFIRMEE, 30 * 24 * 60, true);
        inscrire(e2, passe1, StatutInscription.CONFIRMEE, 29 * 24 * 60, true);
        inscrire(e3, passe1, StatutInscription.CONFIRMEE, 28 * 24 * 60, false);
        inscrire(e1, passe2, StatutInscription.CONFIRMEE, 15 * 24 * 60, true);
        inscrire(e4, passe2, StatutInscription.CONFIRMEE, 14 * 24 * 60, true);
        avisRepository.save(new Avis(e1, passe1, 5, "Super atelier, très bien encadré !"));
        avisRepository.save(new Avis(e2, passe1, 4, "Intéressant, un peu court."));
        avisRepository.save(new Avis(e4, passe2, 4, "Belle randonnée, bonne ambiance."));

        // ---- Événement annulé
        Evenement annule = evenement("Sortie de karting (annulée)", "Sortie annulée faute de participants.",
                maintenant.plusDays(6), 4, 25, sport, gymnase, pleinAir, org2, StatutEvenement.ANNULE);
        inscrire(e2, annule, StatutInscription.CONFIRMEE, 3 * 24 * 60, false);

        // ---- Événements à venir
        Evenement complet = evenement("Soirée jeux de société", "Une soirée pour se détendre : jeux fournis, collations, bonne humeur.",
                maintenant.plusDays(3), 4, 2, social, cafe, echecs, org1, StatutEvenement.PLANIFIE);
        inscrire(e1, complet, StatutInscription.CONFIRMEE, 300, false);
        inscrire(e2, complet, StatutInscription.CONFIRMEE, 200, false);
        inscrire(e3, complet, StatutInscription.EN_ATTENTE, 100, false);
        inscrire(e4, complet, StatutInscription.EN_ATTENTE, 50, false);

        Evenement presque = evenement("Conférence : l'IA au quotidien", "Panel sur les usages de l'intelligence artificielle en technologie et en santé.",
                maintenant.plusDays(4), 2, 5, academique, amphi, info, org1, StatutEvenement.PLANIFIE);
        inscrire(e1, presque, StatutInscription.CONFIRMEE, 200, false);
        inscrire(e5, presque, StatutInscription.CONFIRMEE, 100, false);

        // titre, catégorie, lieu, club, capacité
        Object[][] autres = {
                {"Tournoi d'échecs éclair", social, cafe, echecs, 32},
                {"Atelier de soudure électronique", academique, salle, info, 12},
                {"Compétition de robots suiveurs de ligne", academique, agora, info, 100},
                {"Cinéma en plein air à l'Agora", culturel, agora, pleinAir, 200},
                {"Soirée d'improvisation : match d'ouverture", culturel, amphi, echecs, 120},
                {"Atelier de premiers soins en nature", academique, salle, pleinAir, 25},
                {"Marathon de programmation (24 h)", academique, salle, info, 30},
                {"Yoga sur l'heure du midi", sport, gymnase, pleinAir, 35}
        };
        int jour = 5;
        for (Object[] a : autres) {
            Club club = (Club) a[3];
            evenement((String) a[0], "Événement organisé par la Vie étudiante et les clubs du collège. Venez nombreux !",
                    maintenant.plusDays(jour++), 3, (Integer) a[4], (Categorie) a[1], (Lieu) a[2], club,
                    club.getResponsable(), StatutEvenement.PLANIFIE);
        }
    }

    private Role role(String nom, String description) {
        return roleRepository.findByNom(nom).orElseGet(() -> roleRepository.save(new Role(nom, description)));
    }

    private Utilisateur utilisateur(String prenom, String nom, String courriel, String mdp, boolean actif, Role... roles) {
        Utilisateur u = new Utilisateur(prenom, nom, courriel, passwordEncoder.encode(mdp));
        u.setActif(actif);
        u.setRoles(new java.util.HashSet<>(Set.of(roles)));
        return utilisateurRepository.save(u);
    }

    private Evenement evenement(String titre, String description, LocalDateTime debut, int dureeHeures, int capacite,
                                Categorie c, Lieu l, Club club, Utilisateur createur, StatutEvenement statut) {
        Evenement e = new Evenement(titre, description, debut, debut.plusHours(dureeHeures), capacite, c, l, club, createur);
        e.setStatut(statut);
        return evenementRepository.save(e);
    }

    private void inscrire(Utilisateur u, Evenement e, StatutInscription statut, int minutesAvant, boolean present) {
        Inscription i = new Inscription(u, e, statut, LocalDateTime.now().minusMinutes(minutesAvant));
        i.setPresenceConfirmee(present);
        inscriptionRepository.save(i);
    }
}

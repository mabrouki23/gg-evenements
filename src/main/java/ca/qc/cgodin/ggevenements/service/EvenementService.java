package ca.qc.cgodin.ggevenements.service;

import ca.qc.cgodin.ggevenements.dto.DetailEvenement;
import ca.qc.cgodin.ggevenements.dto.EvenementForm;
import ca.qc.cgodin.ggevenements.dto.FiltreEvenements;
import ca.qc.cgodin.ggevenements.exception.RegleAffairesException;
import ca.qc.cgodin.ggevenements.model.*;
import ca.qc.cgodin.ggevenements.repository.*;
import ca.qc.cgodin.ggevenements.service.spec.EvenementSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class EvenementService {

    public static final int TAILLE_PAGE = 9;

    private final EvenementRepository evenementRepository;
    private final InscriptionRepository inscriptionRepository;
    private final AvisRepository avisRepository;
    private final CategorieRepository categorieRepository;
    private final LieuRepository lieuRepository;
    private final ClubRepository clubRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final StockageService stockageService;
    private final AccesOrganisateurService accesOrganisateur;
    private final InscriptionService inscriptionService;

    public EvenementService(EvenementRepository evenementRepository, InscriptionRepository inscriptionRepository,
                            AvisRepository avisRepository, CategorieRepository categorieRepository,
                            LieuRepository lieuRepository, ClubRepository clubRepository,
                            UtilisateurRepository utilisateurRepository, StockageService stockageService,
                            AccesOrganisateurService accesOrganisateur, InscriptionService inscriptionService) {
        this.evenementRepository = evenementRepository;
        this.inscriptionRepository = inscriptionRepository;
        this.avisRepository = avisRepository;
        this.categorieRepository = categorieRepository;
        this.lieuRepository = lieuRepository;
        this.clubRepository = clubRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.stockageService = stockageService;
        this.accesOrganisateur = accesOrganisateur;
        this.inscriptionService = inscriptionService;
    }

    // ------------------------------------------------------------------ lecture (public)

    /**
     * Liste des événements à venir (fonctionnalités 2 et 7) : statut PLANIFIE, date de début future,
     * 9 par page. Filtres et tri exécutés par la base de données (Specifications), jamais en mémoire.
     *
     * @param tri "date" (défaut), "titre" ou "places"
     */
    @Transactional(readOnly = true)
    public Page<Evenement> aVenir(FiltreEvenements f, String tri, int page) {
        Specification<Evenement> spec = Specification
                .where(EvenementSpecifications.aVenir(LocalDateTime.now()))
                .and(EvenementSpecifications.parCategorie(f.categorieId))
                .and(EvenementSpecifications.parClub(f.clubId))
                .and(EvenementSpecifications.titreContient(f.q))
                .and(EvenementSpecifications.debutAPartirDu(f.du))
                .and(EvenementSpecifications.debutJusquAu(f.au));

        Sort sort;
        if ("titre".equals(tri)) {
            sort = Sort.by(Sort.Order.asc("titre").ignoreCase(), Sort.Order.asc("dateDebut"));
        } else if ("places".equals(tri)) {
            sort = Sort.by(Sort.Order.asc("placesRestantes"), Sort.Order.asc("dateDebut"));
        } else {
            sort = Sort.by(Sort.Order.asc("dateDebut"), Sort.Order.asc("id"));
        }
        return evenementRepository.findAll(spec, PageRequest.of(Math.max(page, 0), TAILLE_PAGE, sort));
    }

    /** Détail (fonctionnalité 4) : l'événement et la situation de l'utilisateur courant. */
    @Transactional(readOnly = true)
    public DetailEvenement detail(Integer id, Integer utilisateurId) {
        Evenement e = evenementRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Événement introuvable"));

        StatutInscription mon = inscriptionRepository.findByUtilisateurIdAndEvenementId(utilisateurId, id)
                .map(Inscription::getStatut).orElse(null);
        boolean ouvert = e.isOuvertAuxInscriptions();

        List<Avis> avis = List.of();
        Double moyenne = null;
        if (e.isTermine() && !e.isAnnule()) {
            avis = avisRepository.findByEvenementIdOrderByDateAvisDescIdDesc(id);
            if (!avis.isEmpty()) {
                moyenne = avisRepository.moyenneParEvenement(id);
            }
        }
        return new DetailEvenement(e, mon, ouvert && mon == null, ouvert && mon != null, avis, moyenne);
    }

    @Transactional(readOnly = true)
    public List<Evenement> aVenirDuClub(Integer clubId) {
        return evenementRepository.findByClubIdAndStatutAndDateDebutAfterOrderByDateDebutAsc(
                clubId, StatutEvenement.PLANIFIE, LocalDateTime.now());
    }

    // ------------------------------------------------------------------ espace organisateur

    /** Événements de tous les clubs dont l'organisateur est responsable. */
    @Transactional(readOnly = true)
    public List<Evenement> evenementsDuClubDe(Integer organisateurId) {
        return evenementRepository.findByClubResponsableIdOrderByDateDebutDesc(organisateurId);
    }

    /**
     * Club au nom duquel l'organisateur crée un événement. Jamais un champ du formulaire :
     *  - un seul club actif -> choisi automatiquement ;
     *  - plusieurs clubs actifs -> le « club actif » choisi dans « Mes événements à organiser » (clubActifId),
     *    vérifié ici : il doit absolument appartenir à l'organisateur connecté (403 sinon).
     */
    @Transactional(readOnly = true)
    public Club clubPourCreation(Integer organisateurId, Integer clubActifId) {
        List<Club> actifs = clubRepository.findByResponsableIdOrderByNomAsc(organisateurId).stream()
                .filter(Club::isActif).toList();
        if (actifs.isEmpty()) {
            throw new RegleAffairesException("Vous n'êtes responsable d'aucun club actif : impossible de créer un événement.");
        }
        if (clubActifId != null) {
            Club choisi = clubRepository.findById(clubActifId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Club invalide"));
            if (choisi.getResponsable() == null || !choisi.getResponsable().getId().equals(organisateurId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ce club n'est pas le vôtre.");
            }
            if (!choisi.isActif()) {
                throw new RegleAffairesException("Ce club est désactivé : impossible de créer un événement en son nom.");
            }
            return choisi;
        }
        if (actifs.size() == 1) {
            return actifs.get(0);
        }
        throw new RegleAffairesException("Vous êtes responsable de plusieurs clubs : choisissez d'abord le club actif "
                + "dans « Mes événements à organiser ».");
    }

    @Transactional(readOnly = true)
    public Evenement pourModification(Integer evenementId, Integer organisateurId) {
        return accesOrganisateur.verifier(evenementId, organisateurId);
    }

    /** Création (fonctionnalité 10) : le club, le statut, la date de création et le créateur sont imposés par le serveur. */
    @Transactional
    public Evenement creer(EvenementForm f, Integer organisateurId, Integer clubActifId) {
        Utilisateur createur = utilisateurRepository.findById(organisateurId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Utilisateur invalide"));
        Club club = clubPourCreation(organisateurId, clubActifId);
        Categorie categorie = categorieRepository.findById(f.getCategorieId())
                .orElseThrow(() -> new RegleAffairesException("categorieId", "Catégorie invalide."));
        Lieu lieu = lieuRepository.findById(f.getLieuId())
                .orElseThrow(() -> new RegleAffairesException("lieuId", "Lieu invalide."));
        verifierCapaciteLieu(f.getCapaciteMax(), lieu);
        String image = stockageService.enregistrer(f.getAffiche(), "affiche");

        Evenement e = new Evenement(f.getTitre().trim(), f.getDescription().trim(), f.getDateDebut(), f.getDateFin(),
                f.getCapaciteMax(), categorie, lieu, club, createur);
        e.setImage(image);
        return evenementRepository.save(e);
    }

    /** Modification (fonctionnalité 11), réservée au club de l'organisateur. */
    @Transactional
    public Evenement modifier(Integer id, EvenementForm f, Integer organisateurId) {
        accesOrganisateur.verifier(id, organisateurId);
        Evenement e = evenementRepository.verrouiller(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Événement introuvable"));
        if (!e.isOuvertAuxInscriptions()) {
            throw new RegleAffairesException("Seul un événement planifié et pas encore commencé peut être modifié.");
        }
        long confirmees = inscriptionRepository.countByEvenementIdAndStatut(id, StatutInscription.CONFIRMEE);
        if (f.getCapaciteMax() < confirmees) {
            throw new RegleAffairesException("capaciteMax",
                    "La capacité ne peut pas être inférieure au nombre d'inscriptions déjà confirmées (" + confirmees + ").");
        }
        Categorie categorie = categorieRepository.findById(f.getCategorieId())
                .orElseThrow(() -> new RegleAffairesException("categorieId", "Catégorie invalide."));
        Lieu lieu = lieuRepository.findById(f.getLieuId())
                .orElseThrow(() -> new RegleAffairesException("lieuId", "Lieu invalide."));
        verifierCapaciteLieu(f.getCapaciteMax(), lieu);

        String nouvelleImage = stockageService.enregistrer(f.getAffiche(), "affiche");
        e.setTitre(f.getTitre().trim());
        e.setDescription(f.getDescription().trim());
        e.setCategorie(categorie);
        e.setLieu(lieu);
        e.setDateDebut(f.getDateDebut());
        e.setDateFin(f.getDateFin());
        e.setCapaciteMax(f.getCapaciteMax());
        if (nouvelleImage != null) {
            e.setImage(nouvelleImage);
        }
        // Si la capacité a augmenté, les personnes en attente prennent les places libérées.
        inscriptionService.promouvoirListeAttente(e);
        return e;
    }

    /** Annulation : changement de statut seulement. Rien n'est supprimé (inscriptions et avis conservés). */
    @Transactional
    public void annuler(Integer id, Integer organisateurId) {
        accesOrganisateur.verifier(id, organisateurId);
        Evenement e = evenementRepository.verrouiller(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Événement introuvable"));
        if (e.getStatut() != StatutEvenement.PLANIFIE || e.isTermine()) {
            throw new RegleAffairesException("Seul un événement planifié et non terminé peut être annulé.");
        }
        e.setStatut(StatutEvenement.ANNULE);
    }

    private void verifierCapaciteLieu(Integer capacite, Lieu lieu) {
        if (capacite != null && capacite > lieu.getCapaciteMax()) {
            throw new RegleAffairesException("capaciteMax",
                    "Le lieu « " + lieu.getNom() + " » ne peut accueillir que " + lieu.getCapaciteMax() + " personnes.");
        }
    }

    // ------------------------------------------------------------------ synchronisation des statuts

    @Transactional
    public int marquerTermines() {
        return evenementRepository.marquerTermines(StatutEvenement.TERMINE, StatutEvenement.PLANIFIE, LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public Optional<Evenement> trouver(Integer id) {
        return evenementRepository.findById(id);
    }
}

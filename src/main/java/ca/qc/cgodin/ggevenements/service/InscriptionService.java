package ca.qc.cgodin.ggevenements.service;

import ca.qc.cgodin.ggevenements.dto.InscriptionsEvenement;
import ca.qc.cgodin.ggevenements.dto.LigneMonEvenement;
import ca.qc.cgodin.ggevenements.dto.MesEvenements;
import ca.qc.cgodin.ggevenements.exception.RegleAffairesException;
import ca.qc.cgodin.ggevenements.model.*;
import ca.qc.cgodin.ggevenements.repository.AvisRepository;
import ca.qc.cgodin.ggevenements.repository.EvenementRepository;
import ca.qc.cgodin.ggevenements.repository.InscriptionRepository;
import ca.qc.cgodin.ggevenements.repository.UtilisateurRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class InscriptionService {

    private final InscriptionRepository inscriptionRepository;
    private final EvenementRepository evenementRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final AvisRepository avisRepository;
    private final AccesOrganisateurService accesOrganisateur;

    public InscriptionService(InscriptionRepository inscriptionRepository, EvenementRepository evenementRepository,
                              UtilisateurRepository utilisateurRepository, AvisRepository avisRepository,
                              AccesOrganisateurService accesOrganisateur) {
        this.inscriptionRepository = inscriptionRepository;
        this.evenementRepository = evenementRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.avisRepository = avisRepository;
        this.accesOrganisateur = accesOrganisateur;
    }

    /**
     * Inscription (fonctionnalité 5). Une SEULE transaction : verrou sur l'événement (SELECT ... FOR UPDATE),
     * vérification du doublon, décompte des confirmés, puis création. Deux inscriptions simultanées sont donc
     * sérialisées et l'événement ne peut jamais dépasser sa capacité.
     */
    @Transactional
    public StatutInscription sinscrire(Integer evenementId, Integer utilisateurId) {
        Evenement e = evenementRepository.verrouiller(evenementId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Événement introuvable"));

        if (!e.isOuvertAuxInscriptions()) {
            throw new RegleAffairesException("Les inscriptions ne sont pas possibles pour cet événement.");
        }
        if (inscriptionRepository.existsByUtilisateurIdAndEvenementId(utilisateurId, evenementId)) {
            throw new RegleAffairesException("Vous êtes déjà inscrit(e) à cet événement.");
        }
        Utilisateur u = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Utilisateur invalide"));

        long confirmees = inscriptionRepository.countByEvenementIdAndStatut(evenementId, StatutInscription.CONFIRMEE);
        StatutInscription statut = confirmees < e.getCapaciteMax()
                ? StatutInscription.CONFIRMEE
                : StatutInscription.EN_ATTENTE;

        inscriptionRepository.save(new Inscription(u, e, statut, LocalDateTime.now()));
        return statut;
    }

    /**
     * Désinscription (fonctionnalité 6). Une SEULE transaction : le retrait ET la promotion éventuelle de la
     * première personne en liste d'attente réussissent ensemble, ou aucun des deux n'a lieu.
     */
    @Transactional
    public void seDesinscrire(Integer evenementId, Integer utilisateurId) {
        Evenement e = evenementRepository.verrouiller(evenementId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Événement introuvable"));

        Inscription inscription = inscriptionRepository.findByUtilisateurIdAndEvenementId(utilisateurId, evenementId)
                .orElseThrow(() -> new RegleAffairesException("Vous n'êtes pas inscrit(e) à cet événement."));

        if (!e.isOuvertAuxInscriptions()) {
            throw new RegleAffairesException("Vous ne pouvez plus vous désinscrire de cet événement.");
        }

        boolean etaitConfirmee = inscription.getStatut() == StatutInscription.CONFIRMEE;
        inscriptionRepository.delete(inscription);
        inscriptionRepository.flush();

        if (etaitConfirmee) {
            // La plus ancienne date d'inscription passe en premier.
            inscriptionRepository
                    .findFirstByEvenementIdAndStatutOrderByDateInscriptionAscIdAsc(evenementId, StatutInscription.EN_ATTENTE)
                    .ifPresent(prochain -> prochain.setStatut(StatutInscription.CONFIRMEE));
        }
    }

    /** Promeut, dans l'ordre d'inscription, autant de personnes en attente que de places libres. */
    @Transactional
    public void promouvoirListeAttente(Evenement e) {
        long confirmees = inscriptionRepository.countByEvenementIdAndStatut(e.getId(), StatutInscription.CONFIRMEE);
        while (confirmees < e.getCapaciteMax()) {
            Optional<Inscription> prochain = inscriptionRepository
                    .findFirstByEvenementIdAndStatutOrderByDateInscriptionAscIdAsc(e.getId(), StatutInscription.EN_ATTENTE);
            if (prochain.isEmpty()) {
                break;
            }
            prochain.get().setStatut(StatutInscription.CONFIRMEE);
            confirmees++;
        }
    }

    /** « Mes événements » (fonctionnalité 3). */
    @Transactional(readOnly = true)
    public MesEvenements mesEvenements(Integer utilisateurId) {
        LocalDateTime maintenant = LocalDateTime.now();
        Set<Integer> avisSoumis = new HashSet<>(avisRepository.idsEvenementsAvecAvis(utilisateurId));

        var aVenir = inscriptionRepository.findAVenir(utilisateurId, maintenant).stream()
                .map(i -> new LigneMonEvenement(i.getEvenement(), i.getStatut(),
                        i.getEvenement().isOuvertAuxInscriptions(), false, false))
                .toList();

        var passes = inscriptionRepository.findPasses(utilisateurId, maintenant).stream()
                .map(i -> {
                    Evenement ev = i.getEvenement();
                    boolean deja = avisSoumis.contains(ev.getId());
                    boolean peut = !deja && !ev.isAnnule() && i.getStatut() == StatutInscription.CONFIRMEE;
                    return new LigneMonEvenement(ev, i.getStatut(), false, deja, peut);
                })
                .toList();

        return new MesEvenements(aVenir, passes);
    }

    /** Écran « Gérer les inscriptions » (fonctionnalité 12) : accès limité au club de l'organisateur (403 sinon). */
    @Transactional(readOnly = true)
    public InscriptionsEvenement inscriptionsPourOrganisateur(Integer evenementId, Integer organisateurId) {
        Evenement e = accesOrganisateur.verifier(evenementId, organisateurId);
        return new InscriptionsEvenement(e,
                inscriptionRepository.findByEvenementIdAndStatutOrderByDateInscriptionAscIdAsc(evenementId, StatutInscription.CONFIRMEE),
                inscriptionRepository.findByEvenementIdAndStatutOrderByDateInscriptionAscIdAsc(evenementId, StatutInscription.EN_ATTENTE));
    }

    /** Case « Présence confirmée ». @return l'id de l'événement (pour la redirection). */
    @Transactional
    public Integer marquerPresence(Integer inscriptionId, boolean presente, Integer organisateurId) {
        Inscription i = inscriptionRepository.findById(inscriptionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inscription introuvable"));
        Integer evenementId = i.getEvenement().getId();
        accesOrganisateur.verifier(evenementId, organisateurId);
        if (i.getStatut() != StatutInscription.CONFIRMEE) {
            throw new RegleAffairesException("Seules les personnes confirmées peuvent avoir leur présence marquée.");
        }
        i.setPresenceConfirmee(presente);
        return evenementId;
    }
}

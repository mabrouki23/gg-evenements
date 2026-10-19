package ca.qc.cgodin.ggevenements.service;

import ca.qc.cgodin.ggevenements.dto.AvisForm;
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

@Service
public class AvisService {

    private final AvisRepository avisRepository;
    private final EvenementRepository evenementRepository;
    private final InscriptionRepository inscriptionRepository;
    private final UtilisateurRepository utilisateurRepository;

    public AvisService(AvisRepository avisRepository, EvenementRepository evenementRepository,
                       InscriptionRepository inscriptionRepository, UtilisateurRepository utilisateurRepository) {
        this.avisRepository = avisRepository;
        this.evenementRepository = evenementRepository;
        this.inscriptionRepository = inscriptionRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    /** Vérifie (couche service) que l'utilisateur peut laisser un avis, et retourne l'événement. */
    @Transactional(readOnly = true)
    public Evenement verifierPeutLaisserAvis(Integer evenementId, Integer utilisateurId) {
        Evenement e = evenementRepository.findById(evenementId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Événement introuvable"));
        if (e.isAnnule() || !e.isTermine()) {
            throw new RegleAffairesException("Vous ne pouvez laisser un avis qu'après la fin d'un événement.");
        }
        Inscription i = inscriptionRepository.findByUtilisateurIdAndEvenementId(utilisateurId, evenementId).orElse(null);
        if (i == null || i.getStatut() != StatutInscription.CONFIRMEE) {
            throw new RegleAffairesException("Seules les personnes confirmées peuvent laisser un avis (une personne restée en liste d'attente ne le peut pas).");
        }
        if (avisRepository.existsByUtilisateurIdAndEvenementId(utilisateurId, evenementId)) {
            throw new RegleAffairesException("Vous avez déjà laissé un avis pour cet événement.");
        }
        return e;
    }

    @Transactional
    public void soumettre(Integer evenementId, Integer utilisateurId, AvisForm form) {
        Evenement e = verifierPeutLaisserAvis(evenementId, utilisateurId);
        Utilisateur u = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Utilisateur invalide"));
        String commentaire = form.getCommentaire() == null || form.getCommentaire().isBlank()
                ? null : form.getCommentaire().trim();
        avisRepository.save(new Avis(u, e, form.getNote(), commentaire));
    }
}

package ca.qc.cgodin.ggevenements.service;

import ca.qc.cgodin.ggevenements.model.Evenement;
import ca.qc.cgodin.ggevenements.model.Utilisateur;
import ca.qc.cgodin.ggevenements.repository.EvenementRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Règle de sécurité « métier » : un organisateur ne gère QUE les événements de son propre club.
 * Vérifiée dans la couche service (HTTP 403), indépendamment de ce que l'interface affiche.
 */
@Service
public class AccesOrganisateurService {

    private final EvenementRepository evenementRepository;

    public AccesOrganisateurService(EvenementRepository evenementRepository) {
        this.evenementRepository = evenementRepository;
    }

    @Transactional(readOnly = true)
    public Evenement verifier(Integer evenementId, Integer utilisateurId) {
        Evenement e = evenementRepository.findById(evenementId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Événement introuvable"));
        Utilisateur responsable = e.getClub().getResponsable();
        if (responsable == null || !responsable.getId().equals(utilisateurId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cet événement n'appartient pas à votre club.");
        }
        return e;
    }
}

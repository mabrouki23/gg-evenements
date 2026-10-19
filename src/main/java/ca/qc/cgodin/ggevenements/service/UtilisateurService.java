package ca.qc.cgodin.ggevenements.service;

import ca.qc.cgodin.ggevenements.exception.RegleAffairesException;
import ca.qc.cgodin.ggevenements.model.Role;
import ca.qc.cgodin.ggevenements.model.Utilisateur;
import ca.qc.cgodin.ggevenements.repository.ClubRepository;
import ca.qc.cgodin.ggevenements.repository.RoleRepository;
import ca.qc.cgodin.ggevenements.repository.UtilisateurRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final RoleRepository roleRepository;
    private final ClubRepository clubRepository;

    public UtilisateurService(UtilisateurRepository utilisateurRepository, RoleRepository roleRepository,
                              ClubRepository clubRepository) {
        this.utilisateurRepository = utilisateurRepository;
        this.roleRepository = roleRepository;
        this.clubRepository = clubRepository;
    }

    @Transactional(readOnly = true)
    public List<Utilisateur> tous() {
        return utilisateurRepository.findAllByOrderByNomAscPrenomAsc();
    }

    @Transactional(readOnly = true)
    public List<Utilisateur> actifs() {
        return utilisateurRepository.findByActifTrueOrderByNomAscPrenomAsc();
    }

    /** Désactive / réactive un compte. Aucun historique n'est supprimé ; aucune suppression physique n'existe. */
    @Transactional
    public Utilisateur basculerActif(Integer id, Integer administrateurId) {
        Utilisateur u = utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
        if (u.getId().equals(administrateurId)) {
            throw new RegleAffairesException("Vous ne pouvez pas désactiver votre propre compte.");
        }
        u.setActif(!u.isActif());
        return u;
    }

    /** Attribue ou retire le rôle Organisateur. */
    @Transactional
    public Utilisateur basculerOrganisateur(Integer id) {
        Utilisateur u = utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
        if (u.aRole(Role.ORGANISATEUR)) {
            if (clubRepository.existsByResponsableId(id)) {
                throw new RegleAffairesException("Impossible de retirer le rôle Organisateur : cette personne est responsable d'au moins un club. "
                        + "Désignez d'abord un autre responsable pour ce ou ces clubs.");
            }
            u.getRoles().removeIf(r -> r.getNom().equals(Role.ORGANISATEUR));
        } else {
            Role organisateur = roleRepository.findByNom(Role.ORGANISATEUR)
                    .orElseThrow(() -> new IllegalStateException("Rôle ORGANISATEUR absent de la base"));
            u.getRoles().add(organisateur);
        }
        return u;
    }
}

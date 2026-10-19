package ca.qc.cgodin.ggevenements.service;

import ca.qc.cgodin.ggevenements.dto.ClubForm;
import ca.qc.cgodin.ggevenements.exception.RegleAffairesException;
import ca.qc.cgodin.ggevenements.model.Club;
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
public class ClubService {

    private final ClubRepository clubRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final RoleRepository roleRepository;

    public ClubService(ClubRepository clubRepository, UtilisateurRepository utilisateurRepository,
                       RoleRepository roleRepository) {
        this.clubRepository = clubRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.roleRepository = roleRepository;
    }

    /** Clubs actifs seulement : ce sont ceux proposés dans les listes déroulantes des usagers. */
    @Transactional(readOnly = true)
    public List<Club> actifs() {
        return clubRepository.findByActifTrueOrderByNomAsc();
    }

    @Transactional(readOnly = true)
    public List<Club> tous() {
        return clubRepository.findAllByOrderByNomAsc();
    }

    /** Fiche publique : un club désactivé reste consultable (historique). */
    @Transactional(readOnly = true)
    public Club trouver(Integer id) {
        return clubRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Club introuvable"));
    }

    /** Clubs dont l'utilisateur est responsable (un utilisateur peut en diriger plusieurs). */
    @Transactional(readOnly = true)
    public List<Club> clubsDe(Integer responsableId) {
        return clubRepository.findByResponsableIdOrderByNomAsc(responsableId);
    }

    /** Retourne le club seulement si l'utilisateur en est le responsable ; sinon HTTP 403. */
    @Transactional(readOnly = true)
    public Club clubDuResponsable(Integer clubId, Integer responsableId) {
        Club c = trouver(clubId);
        if (c.getResponsable() == null || !c.getResponsable().getId().equals(responsableId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ce club n'est pas le vôtre.");
        }
        return c;
    }

    @Transactional
    public Club creer(ClubForm f) {
        if (clubRepository.existsByNomIgnoreCase(f.getNom().trim())) {
            throw new RegleAffairesException("nom", "Un club porte déjà ce nom.");
        }
        Utilisateur responsable = verifierResponsable(f.getResponsableId());
        Club club = new Club(f.getNom().trim(), nettoyer(f.getDescription()), f.getCourrielContact().trim(), responsable);
        accorderRoleOrganisateur(responsable);
        return clubRepository.save(club);
    }

    @Transactional
    public Club modifier(Integer id, ClubForm f) {
        Club club = trouver(id);
        if (clubRepository.existsByNomIgnoreCaseAndIdNot(f.getNom().trim(), id)) {
            throw new RegleAffairesException("nom", "Un club porte déjà ce nom.");
        }
        Utilisateur responsable = verifierResponsable(f.getResponsableId());
        club.setNom(f.getNom().trim());
        club.setDescription(nettoyer(f.getDescription()));
        club.setCourrielContact(f.getCourrielContact().trim());
        club.setResponsable(responsable);
        accorderRoleOrganisateur(responsable);
        return club;
    }

    @Transactional
    public Club basculerActif(Integer id) {
        Club club = trouver(id);
        club.setActif(!club.isActif());
        return club;
    }

    private Utilisateur verifierResponsable(Integer responsableId) {
        Utilisateur u = utilisateurRepository.findById(responsableId)
                .orElseThrow(() -> new RegleAffairesException("responsableId", "Responsable introuvable."));
        if (!u.isActif()) {
            throw new RegleAffairesException("responsableId", "Ce compte est désactivé : il ne peut pas être responsable d'un club.");
        }
        return u;
    }

    /** Devenir responsable d'un club accorde automatiquement le rôle Organisateur. */
    private void accorderRoleOrganisateur(Utilisateur u) {
        if (!u.aRole(Role.ORGANISATEUR)) {
            Role organisateur = roleRepository.findByNom(Role.ORGANISATEUR)
                    .orElseThrow(() -> new IllegalStateException("Rôle ORGANISATEUR absent de la base"));
            u.getRoles().add(organisateur);
        }
    }

    private String nettoyer(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}

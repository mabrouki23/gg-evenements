package ca.qc.cgodin.ggevenements.api;

import ca.qc.cgodin.ggevenements.api.ApiDto.*;
import ca.qc.cgodin.ggevenements.dto.DetailEvenement;
import ca.qc.cgodin.ggevenements.dto.InscriptionsEvenement;
import ca.qc.cgodin.ggevenements.dto.LigneMonEvenement;
import ca.qc.cgodin.ggevenements.dto.MesEvenements;
import ca.qc.cgodin.ggevenements.dto.Statistiques;
import ca.qc.cgodin.ggevenements.model.*;
import ca.qc.cgodin.ggevenements.security.UtilisateurDetails;

import java.time.format.DateTimeFormatter;
import java.util.List;

/** Conversion des entités et des objets de service en objets JSON. */
final class ApiMapper {

    private static final DateTimeFormatter CHAMP_DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private ApiMapper() { }

    static MoiDto moi(UtilisateurDetails u) {
        List<String> roles = u.getAuthorities().stream()
                .map(a -> a.getAuthority().replaceFirst("^ROLE_", ""))
                .sorted()
                .toList();
        return new MoiDto(u.getId(), u.getNomComplet(), roles);
    }

    static CategorieDto categorie(Categorie c) {
        return new CategorieDto(c.getId(), c.getNom(), c.getDescription(), c.getCouleur());
    }

    static LieuDto lieu(Lieu l) {
        return new LieuDto(l.getId(), l.getNom(), l.getCampus(), l.getAdresse(), l.getCapaciteMax());
    }

    static ClubResume clubResume(Club c) {
        return new ClubResume(c.getId(), c.getNom());
    }

    static ClubDto club(Club c) {
        Utilisateur r = c.getResponsable();
        return new ClubDto(c.getId(), c.getNom(), c.getDescription(), c.getCourrielContact(), c.isActif(),
                r == null ? null : r.getId(), r == null ? null : r.getNomComplet());
    }

    static UtilisateurDto utilisateur(Utilisateur u) {
        return new UtilisateurDto(u.getId(), u.getPrenom(), u.getNom(), u.getCourriel(), u.isActif(),
                u.getRoles().stream().map(Role::getNom).sorted().toList());
    }

    static EvenementDto evenement(Evenement e) {
        return new EvenementDto(e.getId(), e.getTitre(), e.getDescription(),
                e.getImage() == null ? null : "/uploads/" + e.getImage(),
                categorie(e.getCategorie()), clubResume(e.getClub()), lieu(e.getLieu()),
                e.getDateDebut(), e.getDateFin(), e.getCapaciteMax(), e.getStatut().name(),
                e.isTermine(), e.isAnnule(), e.getNbConfirmees(), e.getNbEnAttente(), e.getPlacesRestantes());
    }

    static AvisDto avis(Avis a) {
        return new AvisDto(a.getId(), a.getUtilisateur().getNomComplet(), a.getNote(), a.getCommentaire(), a.getDateAvis());
    }

    static DetailDto detail(DetailEvenement d) {
        return new DetailDto(evenement(d.evenement), d.monStatut == null ? null : d.monStatut.name(),
                d.peutSinscrire, d.peutSeDesinscrire, d.avis.stream().map(ApiMapper::avis).toList(), d.moyenne);
    }

    private static LigneMesEvenementsDto ligne(LigneMonEvenement l) {
        return new LigneMesEvenementsDto(evenement(l.evenement), l.statut.name(), l.peutSeDesinscrire,
                l.avisDejaSoumis, l.peutLaisserAvis);
    }

    static MesEvenementsDto mesEvenements(MesEvenements m) {
        return new MesEvenementsDto(m.aVenir.stream().map(ApiMapper::ligne).toList(),
                m.passes.stream().map(ApiMapper::ligne).toList());
    }

    private static InscriptionDto inscription(Inscription i) {
        return new InscriptionDto(i.getId(), i.getUtilisateur().getNomComplet(), i.getUtilisateur().getCourriel(),
                i.getDateInscription(), i.isPresenceConfirmee());
    }

    static InscriptionsDto inscriptions(InscriptionsEvenement ie) {
        return new InscriptionsDto(evenement(ie.evenement),
                ie.confirmees.stream().map(ApiMapper::inscription).toList(),
                ie.enAttente.stream().map(ApiMapper::inscription).toList());
    }

    static StatsDto stats(Statistiques s) {
        return new StatsDto(s.planifies, s.annules, s.termines, s.tauxRemplissage,
                s.clubsActifs.stream().map(c -> new ClubActifDto(c.nom, c.nbEvenements)).toList(),
                s.noteMoyenne, s.nbAvis);
    }

    static EvenementFormDto formulaire(Evenement e) {
        return new EvenementFormDto(e.getTitre(), e.getDescription(), e.getCategorie().getId(), e.getLieu().getId(),
                e.getDateDebut().format(CHAMP_DATETIME), e.getDateFin().format(CHAMP_DATETIME),
                e.getCapaciteMax(), e.getImage());
    }
}

package ca.qc.cgodin.ggevenements.service.spec;

import ca.qc.cgodin.ggevenements.model.Evenement;
import ca.qc.cgodin.ggevenements.model.StatutEvenement;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Requêtes dynamiques (Specifications Spring Data JPA) : un critère absent est simplement ignoré. */
public final class EvenementSpecifications {

    private EvenementSpecifications() { }

    private static Specification<Evenement> aucun() {
        return (root, query, cb) -> null;
    }

    /** Événements à venir : statut PLANIFIE et date de début future. */
    public static Specification<Evenement> aVenir(LocalDateTime maintenant) {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get("statut"), StatutEvenement.PLANIFIE),
                cb.greaterThan(root.<LocalDateTime>get("dateDebut"), maintenant));
    }

    public static Specification<Evenement> parCategorie(Integer categorieId) {
        if (categorieId == null) return aucun();
        return (root, query, cb) -> cb.equal(root.get("categorie").get("id"), categorieId);
    }

    public static Specification<Evenement> parClub(Integer clubId) {
        if (clubId == null) return aucun();
        return (root, query, cb) -> cb.equal(root.get("club").get("id"), clubId);
    }

    /** Recherche sur une partie du titre, sans tenir compte de la casse (les caractères % et _ saisis sont neutralisés). */
    public static Specification<Evenement> titreContient(String texte) {
        if (texte == null || texte.isBlank()) return aucun();
        String motif = "%" + echapper(texte.trim().toLowerCase()) + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.<String>get("titre")), motif, '\\');
    }

    public static Specification<Evenement> debutAPartirDu(LocalDate du) {
        if (du == null) return aucun();
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.<LocalDateTime>get("dateDebut"), du.atStartOfDay());
    }

    public static Specification<Evenement> debutJusquAu(LocalDate au) {
        if (au == null) return aucun();
        return (root, query, cb) -> cb.lessThan(root.<LocalDateTime>get("dateDebut"), au.plusDays(1).atStartOfDay());
    }

    private static String echapper(String s) {
        return s.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}

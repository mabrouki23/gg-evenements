package ca.qc.cgodin.ggevenements.repository;

import ca.qc.cgodin.ggevenements.model.Inscription;
import ca.qc.cgodin.ggevenements.model.StatutInscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface InscriptionRepository extends JpaRepository<Inscription, Integer> {

    Optional<Inscription> findByUtilisateurIdAndEvenementId(Integer utilisateurId, Integer evenementId);

    boolean existsByUtilisateurIdAndEvenementId(Integer utilisateurId, Integer evenementId);

    long countByEvenementIdAndStatut(Integer evenementId, StatutInscription statut);

    List<Inscription> findByEvenementIdAndStatutOrderByDateInscriptionAscIdAsc(Integer evenementId, StatutInscription statut);

    /** La personne en attente inscrite depuis le plus longtemps. */
    Optional<Inscription> findFirstByEvenementIdAndStatutOrderByDateInscriptionAscIdAsc(Integer evenementId, StatutInscription statut);

    @Query("select i from Inscription i join fetch i.evenement e where i.utilisateur.id = :uid and e.dateFin >= :maintenant "
            + "order by e.dateDebut asc")
    List<Inscription> findAVenir(@Param("uid") Integer utilisateurId, @Param("maintenant") LocalDateTime maintenant);

    @Query("select i from Inscription i join fetch i.evenement e where i.utilisateur.id = :uid and e.dateFin < :maintenant "
            + "order by e.dateDebut desc")
    List<Inscription> findPasses(@Param("uid") Integer utilisateurId, @Param("maintenant") LocalDateTime maintenant);
}

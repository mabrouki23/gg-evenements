package ca.qc.cgodin.ggevenements.repository;

import ca.qc.cgodin.ggevenements.model.Evenement;
import ca.qc.cgodin.ggevenements.model.StatutEvenement;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EvenementRepository extends JpaRepository<Evenement, Integer>, JpaSpecificationExecutor<Evenement> {

    /** Verrou pessimiste (SELECT ... FOR UPDATE) : sérialise les inscriptions/désinscriptions d un même événement. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Evenement e where e.id = :id")
    Optional<Evenement> verrouiller(@Param("id") Integer id);

    List<Evenement> findByClubIdOrderByDateDebutDesc(Integer clubId);

    /** Événements de tous les clubs dont l'utilisateur est responsable. */
    List<Evenement> findByClubResponsableIdOrderByDateDebutDesc(Integer responsableId);

    List<Evenement> findByClubIdAndStatutAndDateDebutAfterOrderByDateDebutAsc(Integer clubId, StatutEvenement statut, LocalDateTime apres);

    long countByCategorieId(Integer categorieId);

    long countByLieuId(Integer lieuId);

    /** Passe à TERMINE les événements planifiés dont la date de fin est dépassée. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Evenement e set e.statut = :termine where e.statut = :planifie and e.dateFin < :maintenant")
    int marquerTermines(@Param("termine") StatutEvenement termine,
                        @Param("planifie") StatutEvenement planifie,
                        @Param("maintenant") LocalDateTime maintenant);

    // ---------- Statistiques : agrégations exécutées par la base de données ----------

    @Query("select e.statut, count(e) from Evenement e group by e.statut")
    List<Object[]> compterParStatut();

    /** Moyenne (0 à 1) de confirmés / capacité pour les événements terminés. */
    @Query(value = "SELECT AVG(COALESCE(c.nb, 0) * 1.0 / e.capacite_max) FROM evenement e "
            + "LEFT JOIN (SELECT id_evenement, COUNT(*) AS nb FROM inscription WHERE statut = 'CONFIRMEE' GROUP BY id_evenement) c "
            + "ON c.id_evenement = e.id_evenement WHERE e.statut = 'TERMINE'", nativeQuery = true)
    Number tauxMoyenRemplissageTermines();

    @Query("select c.nom, count(e) from Evenement e join e.club c where e.statut <> :exclu "
            + "group by c.id, c.nom order by count(e) desc, c.nom asc")
    List<Object[]> clubsLesPlusActifs(@Param("exclu") StatutEvenement exclu, Pageable pageable);
}

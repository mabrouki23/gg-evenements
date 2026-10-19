package ca.qc.cgodin.ggevenements.repository;

import ca.qc.cgodin.ggevenements.model.Avis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AvisRepository extends JpaRepository<Avis, Integer> {

    boolean existsByUtilisateurIdAndEvenementId(Integer utilisateurId, Integer evenementId);

    List<Avis> findByEvenementIdOrderByDateAvisDescIdDesc(Integer evenementId);

    @Query("select avg(a.note) from Avis a where a.evenement.id = :evenementId")
    Double moyenneParEvenement(@Param("evenementId") Integer evenementId);

    @Query("select a.evenement.id from Avis a where a.utilisateur.id = :uid")
    List<Integer> idsEvenementsAvecAvis(@Param("uid") Integer utilisateurId);

    @Query("select avg(a.note) from Avis a")
    Double moyenneGenerale();
}

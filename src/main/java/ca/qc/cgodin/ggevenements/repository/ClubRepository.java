package ca.qc.cgodin.ggevenements.repository;

import ca.qc.cgodin.ggevenements.model.Club;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClubRepository extends JpaRepository<Club, Integer> {
    List<Club> findAllByOrderByNomAsc();
    List<Club> findByActifTrueOrderByNomAsc();
    /** Un utilisateur peut être responsable de plusieurs clubs. */
    List<Club> findByResponsableIdOrderByNomAsc(Integer utilisateurId);
    boolean existsByResponsableId(Integer utilisateurId);
    boolean existsByNomIgnoreCase(String nom);
    boolean existsByNomIgnoreCaseAndIdNot(String nom, Integer id);
}

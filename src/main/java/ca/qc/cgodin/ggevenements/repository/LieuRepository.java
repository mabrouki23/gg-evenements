package ca.qc.cgodin.ggevenements.repository;

import ca.qc.cgodin.ggevenements.model.Lieu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LieuRepository extends JpaRepository<Lieu, Integer> {
    List<Lieu> findAllByOrderByNomAsc();
    boolean existsByNomIgnoreCase(String nom);
    boolean existsByNomIgnoreCaseAndIdNot(String nom, Integer id);
}

package ca.qc.cgodin.ggevenements.repository;

import ca.qc.cgodin.ggevenements.model.Categorie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategorieRepository extends JpaRepository<Categorie, Integer> {
    List<Categorie> findAllByOrderByNomAsc();
    boolean existsByNomIgnoreCase(String nom);
    boolean existsByNomIgnoreCaseAndIdNot(String nom, Integer id);
}

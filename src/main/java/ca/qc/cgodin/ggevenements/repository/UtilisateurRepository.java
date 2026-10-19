package ca.qc.cgodin.ggevenements.repository;

import ca.qc.cgodin.ggevenements.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Integer> {
    Optional<Utilisateur> findByCourrielIgnoreCase(String courriel);
    boolean existsByCourrielIgnoreCase(String courriel);
    List<Utilisateur> findAllByOrderByNomAscPrenomAsc();
    List<Utilisateur> findByActifTrueOrderByNomAscPrenomAsc();
}

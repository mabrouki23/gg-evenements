package ca.qc.cgodin.ggevenements.service;

import ca.qc.cgodin.ggevenements.dto.CategorieForm;
import ca.qc.cgodin.ggevenements.exception.RegleAffairesException;
import ca.qc.cgodin.ggevenements.model.Categorie;
import ca.qc.cgodin.ggevenements.repository.CategorieRepository;
import ca.qc.cgodin.ggevenements.repository.EvenementRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CategorieService {

    private final CategorieRepository categorieRepository;
    private final EvenementRepository evenementRepository;

    public CategorieService(CategorieRepository categorieRepository, EvenementRepository evenementRepository) {
        this.categorieRepository = categorieRepository;
        this.evenementRepository = evenementRepository;
    }

    @Transactional(readOnly = true)
    public List<Categorie> toutes() {
        return categorieRepository.findAllByOrderByNomAsc();
    }

    @Transactional(readOnly = true)
    public Categorie trouver(Integer id) {
        return categorieRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Catégorie introuvable"));
    }

    @Transactional
    public Categorie creer(CategorieForm f) {
        if (categorieRepository.existsByNomIgnoreCase(f.getNom().trim())) {
            throw new RegleAffairesException("nom", "Une catégorie porte déjà ce nom.");
        }
        return categorieRepository.save(new Categorie(f.getNom().trim(), nettoyer(f.getDescription()), f.getCouleur()));
    }

    @Transactional
    public Categorie modifier(Integer id, CategorieForm f) {
        Categorie c = trouver(id);
        if (categorieRepository.existsByNomIgnoreCaseAndIdNot(f.getNom().trim(), id)) {
            throw new RegleAffairesException("nom", "Une catégorie porte déjà ce nom.");
        }
        c.setNom(f.getNom().trim());
        c.setDescription(nettoyer(f.getDescription()));
        c.setCouleur(f.getCouleur());
        return c;
    }

    /** Suppression refusée (avec un message clair) si au moins un événement utilise la catégorie. */
    @Transactional
    public void supprimer(Integer id) {
        Categorie c = trouver(id);
        long utilisations = evenementRepository.countByCategorieId(id);
        if (utilisations > 0) {
            throw new RegleAffairesException("Impossible de supprimer la catégorie « " + c.getNom()
                    + " » : elle est utilisée par " + utilisations + " événement(s).");
        }
        categorieRepository.delete(c);
    }

    private String nettoyer(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}

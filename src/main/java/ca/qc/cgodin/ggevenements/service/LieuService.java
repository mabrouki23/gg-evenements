package ca.qc.cgodin.ggevenements.service;

import ca.qc.cgodin.ggevenements.dto.LieuForm;
import ca.qc.cgodin.ggevenements.exception.RegleAffairesException;
import ca.qc.cgodin.ggevenements.model.Lieu;
import ca.qc.cgodin.ggevenements.repository.EvenementRepository;
import ca.qc.cgodin.ggevenements.repository.LieuRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class LieuService {

    private final LieuRepository lieuRepository;
    private final EvenementRepository evenementRepository;

    public LieuService(LieuRepository lieuRepository, EvenementRepository evenementRepository) {
        this.lieuRepository = lieuRepository;
        this.evenementRepository = evenementRepository;
    }

    @Transactional(readOnly = true)
    public List<Lieu> tous() {
        return lieuRepository.findAllByOrderByNomAsc();
    }

    @Transactional(readOnly = true)
    public Lieu trouver(Integer id) {
        return lieuRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lieu introuvable"));
    }

    @Transactional
    public Lieu creer(LieuForm f) {
        if (lieuRepository.existsByNomIgnoreCase(f.getNom().trim())) {
            throw new RegleAffairesException("nom", "Un lieu porte déjà ce nom.");
        }
        return lieuRepository.save(new Lieu(f.getNom().trim(), f.getCampus().trim(), f.getAdresse().trim(), f.getCapaciteMax()));
    }

    @Transactional
    public Lieu modifier(Integer id, LieuForm f) {
        Lieu l = trouver(id);
        if (lieuRepository.existsByNomIgnoreCaseAndIdNot(f.getNom().trim(), id)) {
            throw new RegleAffairesException("nom", "Un lieu porte déjà ce nom.");
        }
        l.setNom(f.getNom().trim());
        l.setCampus(f.getCampus().trim());
        l.setAdresse(f.getAdresse().trim());
        l.setCapaciteMax(f.getCapaciteMax());
        return l;
    }

    /** Suppression refusée (avec un message clair) si au moins un événement utilise le lieu. */
    @Transactional
    public void supprimer(Integer id) {
        Lieu l = trouver(id);
        long utilisations = evenementRepository.countByLieuId(id);
        if (utilisations > 0) {
            throw new RegleAffairesException("Impossible de supprimer le lieu « " + l.getNom()
                    + " » : il est utilisé par " + utilisations + " événement(s).");
        }
        lieuRepository.delete(l);
    }
}

package ca.qc.cgodin.ggevenements.service;

import ca.qc.cgodin.ggevenements.dto.Statistiques;
import ca.qc.cgodin.ggevenements.model.StatutEvenement;
import ca.qc.cgodin.ggevenements.repository.AvisRepository;
import ca.qc.cgodin.ggevenements.repository.EvenementRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Tableau de bord (fonctionnalité 16) : COUNT / AVG / GROUP BY exécutés par la base de données. */
@Service
public class StatistiquesService {

    private final EvenementRepository evenementRepository;
    private final AvisRepository avisRepository;

    public StatistiquesService(EvenementRepository evenementRepository, AvisRepository avisRepository) {
        this.evenementRepository = evenementRepository;
        this.avisRepository = avisRepository;
    }

    @Transactional
    public Statistiques calculer() {
        // Les statuts sont à jour avant de compter.
        evenementRepository.marquerTermines(StatutEvenement.TERMINE, StatutEvenement.PLANIFIE, LocalDateTime.now());

        Map<StatutEvenement, Long> parStatut = new HashMap<>();
        for (Object[] ligne : evenementRepository.compterParStatut()) {
            parStatut.put((StatutEvenement) ligne[0], ((Number) ligne[1]).longValue());
        }

        Number taux = evenementRepository.tauxMoyenRemplissageTermines();
        Double tauxPourcentage = taux == null ? null : taux.doubleValue() * 100.0;

        List<Statistiques.ClubActif> clubs = evenementRepository
                .clubsLesPlusActifs(StatutEvenement.ANNULE, PageRequest.of(0, 5)).stream()
                .map(l -> new Statistiques.ClubActif((String) l[0], ((Number) l[1]).longValue()))
                .toList();

        return new Statistiques(
                parStatut.getOrDefault(StatutEvenement.PLANIFIE, 0L),
                parStatut.getOrDefault(StatutEvenement.ANNULE, 0L),
                parStatut.getOrDefault(StatutEvenement.TERMINE, 0L),
                tauxPourcentage, clubs, avisRepository.moyenneGenerale(), avisRepository.count());
    }
}

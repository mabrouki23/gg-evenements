package ca.qc.cgodin.ggevenements.config;

import ca.qc.cgodin.ggevenements.service.EvenementService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Fait passer au statut TERMINE les événements planifiés dont la date de fin est dépassée. */
@Component
public class TachesPlanifiees {

    private final EvenementService evenementService;

    public TachesPlanifiees(EvenementService evenementService) {
        this.evenementService = evenementService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void auDemarrage() {
        evenementService.marquerTermines();
    }

    @Scheduled(fixedRate = 60_000)
    public void toutesLesMinutes() {
        evenementService.marquerTermines();
    }
}

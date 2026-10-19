package ca.qc.cgodin.ggevenements.dto;

import ca.qc.cgodin.ggevenements.model.Evenement;
import ca.qc.cgodin.ggevenements.model.Inscription;

import java.util.List;

public class InscriptionsEvenement {
    public final Evenement evenement;
    public final List<Inscription> confirmees;
    public final List<Inscription> enAttente;

    public InscriptionsEvenement(Evenement evenement, List<Inscription> confirmees, List<Inscription> enAttente) {
        this.evenement = evenement;
        this.confirmees = confirmees;
        this.enAttente = enAttente;
    }
}

package ca.qc.cgodin.ggevenements.dto;

import java.util.List;

public class MesEvenements {
    public final List<LigneMonEvenement> aVenir;
    public final List<LigneMonEvenement> passes;

    public MesEvenements(List<LigneMonEvenement> aVenir, List<LigneMonEvenement> passes) {
        this.aVenir = aVenir;
        this.passes = passes;
    }
}

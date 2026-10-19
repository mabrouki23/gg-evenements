package ca.qc.cgodin.ggevenements.dto;

import ca.qc.cgodin.ggevenements.model.Evenement;
import ca.qc.cgodin.ggevenements.model.StatutInscription;

/** Une ligne de « Mes événements ». */
public class LigneMonEvenement {
    public final Evenement evenement;
    public final StatutInscription statut;
    public final boolean peutSeDesinscrire;
    public final boolean avisDejaSoumis;
    public final boolean peutLaisserAvis;

    public LigneMonEvenement(Evenement evenement, StatutInscription statut, boolean peutSeDesinscrire,
                             boolean avisDejaSoumis, boolean peutLaisserAvis) {
        this.evenement = evenement;
        this.statut = statut;
        this.peutSeDesinscrire = peutSeDesinscrire;
        this.avisDejaSoumis = avisDejaSoumis;
        this.peutLaisserAvis = peutLaisserAvis;
    }
}

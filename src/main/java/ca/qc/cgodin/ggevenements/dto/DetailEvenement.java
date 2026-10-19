package ca.qc.cgodin.ggevenements.dto;

import ca.qc.cgodin.ggevenements.model.Avis;
import ca.qc.cgodin.ggevenements.model.Evenement;
import ca.qc.cgodin.ggevenements.model.StatutInscription;

import java.util.List;

/** Données de la page « détail » : l'événement + la situation de l'utilisateur courant. */
public class DetailEvenement {
    public final Evenement evenement;
    /** null = aucune inscription. */
    public final StatutInscription monStatut;
    public final boolean peutSinscrire;
    public final boolean peutSeDesinscrire;
    public final List<Avis> avis;
    public final Double moyenne;

    public DetailEvenement(Evenement evenement, StatutInscription monStatut, boolean peutSinscrire,
                           boolean peutSeDesinscrire, List<Avis> avis, Double moyenne) {
        this.evenement = evenement;
        this.monStatut = monStatut;
        this.peutSinscrire = peutSinscrire;
        this.peutSeDesinscrire = peutSeDesinscrire;
        this.avis = avis;
        this.moyenne = moyenne;
    }
}

package ca.qc.cgodin.ggevenements.dto;

import java.util.List;

public class Statistiques {

    public static class ClubActif {
        public final String nom;
        public final long nbEvenements;

        public ClubActif(String nom, long nbEvenements) {
            this.nom = nom;
            this.nbEvenements = nbEvenements;
        }
    }

    public final long planifies;
    public final long annules;
    public final long termines;
    /** Pourcentage (0 à 100) ou null s'il n'y a aucun événement terminé. */
    public final Double tauxRemplissage;
    public final List<ClubActif> clubsActifs;
    /** null s'il n'y a aucun avis. */
    public final Double noteMoyenne;
    public final long nbAvis;

    public Statistiques(long planifies, long annules, long termines, Double tauxRemplissage,
                        List<ClubActif> clubsActifs, Double noteMoyenne, long nbAvis) {
        this.planifies = planifies;
        this.annules = annules;
        this.termines = termines;
        this.tauxRemplissage = tauxRemplissage;
        this.clubsActifs = clubsActifs;
        this.noteMoyenne = noteMoyenne;
        this.nbAvis = nbAvis;
    }
}

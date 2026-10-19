package ca.qc.cgodin.ggevenements.dto;

import java.time.LocalDate;

/** Critères de la liste et de la recherche avancée (tous facultatifs et combinables). */
public class FiltreEvenements {
    public String q;
    public Integer categorieId;
    public Integer clubId;
    public LocalDate du;
    public LocalDate au;

    public FiltreEvenements() { }

    public FiltreEvenements(String q, Integer categorieId, Integer clubId, LocalDate du, LocalDate au) {
        this.q = q;
        this.categorieId = categorieId;
        this.clubId = clubId;
        this.du = du;
        this.au = au;
    }
}

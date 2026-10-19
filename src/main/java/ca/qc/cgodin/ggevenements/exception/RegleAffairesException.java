package ca.qc.cgodin.ggevenements.exception;

/** Violation d une règle d affaires (message destiné à l utilisateur, champ optionnel pour l affichage à côté du champ fautif). */
public class RegleAffairesException extends RuntimeException {

    private final String champ;

    public RegleAffairesException(String message) {
        this(null, message);
    }

    public RegleAffairesException(String champ, String message) {
        super(message);
        this.champ = champ;
    }

    public String getChamp() { return champ; }
}

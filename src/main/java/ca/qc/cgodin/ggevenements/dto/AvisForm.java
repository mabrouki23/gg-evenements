package ca.qc.cgodin.ggevenements.dto;

import jakarta.validation.constraints.*;

public class AvisForm {

    @NotNull(message = "La note est obligatoire (de 1 à 5).")
    @Min(value = 1, message = "La note doit être comprise entre 1 et 5.")
    @Max(value = 5, message = "La note doit être comprise entre 1 et 5.")
    private Integer note;

    @Size(max = 500, message = "Le commentaire ne doit pas dépasser 500 caractères.")
    private String commentaire;

    public Integer getNote() { return note; }
    public void setNote(Integer note) { this.note = note; }
    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
}

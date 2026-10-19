package ca.qc.cgodin.ggevenements.dto;

import ca.qc.cgodin.ggevenements.model.Club;
import jakarta.validation.constraints.*;

public class ClubForm {

    @NotBlank(message = "Le nom du club est obligatoire.")
    @Size(max = 80, message = "Le nom ne doit pas dépasser 80 caractères.")
    private String nom;

    @Size(max = 300, message = "La description ne doit pas dépasser 300 caractères.")
    private String description;

    @NotBlank(message = "Le courriel de contact est obligatoire.")
    @Email(message = "Le courriel de contact est invalide.")
    @Size(max = 100, message = "Le courriel ne doit pas dépasser 100 caractères.")
    private String courrielContact;

    @NotNull(message = "Veuillez choisir un responsable.")
    private Integer responsableId;

    public static ClubForm depuis(Club c) {
        ClubForm f = new ClubForm();
        f.nom = c.getNom();
        f.description = c.getDescription();
        f.courrielContact = c.getCourrielContact();
        f.responsableId = c.getResponsable().getId();
        return f;
    }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCourrielContact() { return courrielContact; }
    public void setCourrielContact(String courrielContact) { this.courrielContact = courrielContact; }
    public Integer getResponsableId() { return responsableId; }
    public void setResponsableId(Integer responsableId) { this.responsableId = responsableId; }
}

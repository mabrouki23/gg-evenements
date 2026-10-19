package ca.qc.cgodin.ggevenements.dto;

import ca.qc.cgodin.ggevenements.model.Categorie;
import jakarta.validation.constraints.*;

public class CategorieForm {

    @NotBlank(message = "Le nom de la catégorie est obligatoire.")
    @Size(max = 50, message = "Le nom ne doit pas dépasser 50 caractères.")
    private String nom;

    @Size(max = 150, message = "La description ne doit pas dépasser 150 caractères.")
    private String description;

    @NotBlank(message = "La couleur est obligatoire.")
    @Pattern(regexp = "^#[0-9a-fA-F]{6}$", message = "La couleur doit être au format #RRGGBB.")
    private String couleur = "#2563eb";

    public static CategorieForm depuis(Categorie c) {
        CategorieForm f = new CategorieForm();
        f.nom = c.getNom();
        f.description = c.getDescription();
        f.couleur = c.getCouleur();
        return f;
    }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCouleur() { return couleur; }
    public void setCouleur(String couleur) { this.couleur = couleur; }
}

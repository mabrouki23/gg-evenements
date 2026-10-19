package ca.qc.cgodin.ggevenements.dto;

import ca.qc.cgodin.ggevenements.model.Lieu;
import jakarta.validation.constraints.*;

public class LieuForm {

    @NotBlank(message = "Le nom du lieu est obligatoire.")
    @Size(max = 80, message = "Le nom ne doit pas dépasser 80 caractères.")
    private String nom;

    @NotBlank(message = "Le campus est obligatoire.")
    @Size(max = 50, message = "Le campus ne doit pas dépasser 50 caractères.")
    private String campus;

    @NotBlank(message = "L'adresse est obligatoire.")
    @Size(max = 150, message = "L'adresse ne doit pas dépasser 150 caractères.")
    private String adresse;

    @NotNull(message = "La capacité maximale est obligatoire.")
    @Min(value = 1, message = "La capacité maximale doit être un entier positif.")
    private Integer capaciteMax;

    public static LieuForm depuis(Lieu l) {
        LieuForm f = new LieuForm();
        f.nom = l.getNom();
        f.campus = l.getCampus();
        f.adresse = l.getAdresse();
        f.capaciteMax = l.getCapaciteMax();
        return f;
    }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getCampus() { return campus; }
    public void setCampus(String campus) { this.campus = campus; }
    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }
    public Integer getCapaciteMax() { return capaciteMax; }
    public void setCapaciteMax(Integer capaciteMax) { this.capaciteMax = capaciteMax; }
}

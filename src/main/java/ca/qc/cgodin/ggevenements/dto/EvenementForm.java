package ca.qc.cgodin.ggevenements.dto;

import ca.qc.cgodin.ggevenements.model.Evenement;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

/** Formulaire de création / modification. Le club n'en fait volontairement PAS partie. */
public class EvenementForm {

    @NotBlank(message = "Le titre est obligatoire.")
    @Size(max = 120, message = "Le titre ne doit pas dépasser 120 caractères.")
    private String titre;

    @NotBlank(message = "La description est obligatoire.")
    @Size(max = 1000, message = "La description ne doit pas dépasser 1000 caractères.")
    private String description;

    @NotNull(message = "Veuillez choisir une catégorie.")
    private Integer categorieId;

    @NotNull(message = "Veuillez choisir un lieu.")
    private Integer lieuId;

    @NotNull(message = "La date et l'heure de début sont obligatoires.")
    @Future(message = "La date de début doit être dans le futur.")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime dateDebut;

    @NotNull(message = "La date et l'heure de fin sont obligatoires.")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime dateFin;

    @NotNull(message = "La capacité maximale est obligatoire.")
    @Min(value = 1, message = "La capacité maximale doit être un entier positif.")
    @Max(value = 100000, message = "La capacité maximale est trop élevée.")
    private Integer capaciteMax;

    /** Affiche : téléversement facultatif. */
    private MultipartFile affiche;

    /** Affichage seulement (modification) : nom de l'affiche actuelle. */
    private String imageActuelle;

    @AssertTrue(message = "La date de fin doit être postérieure à la date de début.")
    public boolean isDateFinValide() {
        return dateDebut == null || dateFin == null || dateFin.isAfter(dateDebut);
    }

    public static EvenementForm depuis(Evenement e) {
        EvenementForm f = new EvenementForm();
        f.titre = e.getTitre();
        f.description = e.getDescription();
        f.categorieId = e.getCategorie().getId();
        f.lieuId = e.getLieu().getId();
        f.dateDebut = e.getDateDebut();
        f.dateFin = e.getDateFin();
        f.capaciteMax = e.getCapaciteMax();
        f.imageActuelle = e.getImage();
        return f;
    }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getCategorieId() { return categorieId; }
    public void setCategorieId(Integer categorieId) { this.categorieId = categorieId; }
    public Integer getLieuId() { return lieuId; }
    public void setLieuId(Integer lieuId) { this.lieuId = lieuId; }
    public LocalDateTime getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDateTime dateDebut) { this.dateDebut = dateDebut; }
    public LocalDateTime getDateFin() { return dateFin; }
    public void setDateFin(LocalDateTime dateFin) { this.dateFin = dateFin; }
    public Integer getCapaciteMax() { return capaciteMax; }
    public void setCapaciteMax(Integer capaciteMax) { this.capaciteMax = capaciteMax; }
    public MultipartFile getAffiche() { return affiche; }
    public void setAffiche(MultipartFile affiche) { this.affiche = affiche; }
    public String getImageActuelle() { return imageActuelle; }
    public void setImageActuelle(String imageActuelle) { this.imageActuelle = imageActuelle; }
}

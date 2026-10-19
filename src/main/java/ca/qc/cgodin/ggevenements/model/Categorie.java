package ca.qc.cgodin.ggevenements.model;

import jakarta.persistence.*;

/** Table « categorie ». */
@Entity
@Table(name = "categorie")
public class Categorie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categorie")
    private Integer id;

    @Column(name = "nom", nullable = false, length = 50)
    private String nom;

    @Column(name = "description", length = 150)
    private String description;

    /** Couleur hexadécimale (ex. #1F3864) utilisée pour la pastille. */
    @Column(name = "couleur", nullable = false, length = 7)
    private String couleur = "#1F3864";

    protected Categorie() { }

    public Categorie(String nom, String description, String couleur) {
        this.nom = nom;
        this.description = description;
        this.couleur = couleur;
    }

    public Integer getId() { return id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCouleur() { return couleur; }
    public void setCouleur(String couleur) { this.couleur = couleur; }
}

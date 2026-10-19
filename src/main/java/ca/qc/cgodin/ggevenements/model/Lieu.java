package ca.qc.cgodin.ggevenements.model;

import jakarta.persistence.*;

/** Table « lieu ». */
@Entity
@Table(name = "lieu")
public class Lieu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_lieu")
    private Integer id;

    @Column(name = "nom", nullable = false, length = 80)
    private String nom;

    @Column(name = "campus", nullable = false, length = 50)
    private String campus;

    @Column(name = "adresse", nullable = false, length = 150)
    private String adresse;

    @Column(name = "capacite_max", nullable = false)
    private int capaciteMax;

    protected Lieu() { }

    public Lieu(String nom, String campus, String adresse, int capaciteMax) {
        this.nom = nom;
        this.campus = campus;
        this.adresse = adresse;
        this.capaciteMax = capaciteMax;
    }

    public Integer getId() { return id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getCampus() { return campus; }
    public void setCampus(String campus) { this.campus = campus; }
    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }
    public int getCapaciteMax() { return capaciteMax; }
    public void setCapaciteMax(int capaciteMax) { this.capaciteMax = capaciteMax; }
}

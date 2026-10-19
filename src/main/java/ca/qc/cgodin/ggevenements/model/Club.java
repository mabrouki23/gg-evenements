package ca.qc.cgodin.ggevenements.model;

import jakarta.persistence.*;

/** Table « club ». Un club a un unique responsable ; un utilisateur peut cependant être responsable de plusieurs clubs. */
@Entity
@Table(name = "club")
public class Club {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_club")
    private Integer id;

    @Column(name = "nom", nullable = false, length = 80)
    private String nom;

    @Column(name = "description", length = 300)
    private String description;

    @Column(name = "courriel_contact", nullable = false, length = 100)
    private String courrielContact;

    /** Chemin ou nom de fichier du logo (facultatif). */
    @Column(name = "logo", length = 150)
    private String logo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_responsable", nullable = false)
    private Utilisateur responsable;

    @Column(name = "actif", nullable = false)
    private boolean actif = true;

    protected Club() { }

    public Club(String nom, String description, String courrielContact, Utilisateur responsable) {
        this.nom = nom;
        this.description = description;
        this.courrielContact = courrielContact;
        this.responsable = responsable;
    }

    public Integer getId() { return id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCourrielContact() { return courrielContact; }
    public void setCourrielContact(String courrielContact) { this.courrielContact = courrielContact; }
    public String getLogo() { return logo; }
    public void setLogo(String logo) { this.logo = logo; }
    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }
    public Utilisateur getResponsable() { return responsable; }
    public void setResponsable(Utilisateur responsable) { this.responsable = responsable; }
}

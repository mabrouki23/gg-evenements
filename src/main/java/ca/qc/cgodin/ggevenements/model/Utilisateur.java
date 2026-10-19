package ca.qc.cgodin.ggevenements.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/** Table « utilisateur ». */
@Entity
@Table(name = "utilisateur")
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_utilisateur")
    private Integer id;

    @Column(name = "prenom", nullable = false, length = 50)
    private String prenom;

    @Column(name = "nom", nullable = false, length = 50)
    private String nom;

    @Column(name = "courriel", nullable = false, unique = true, length = 100)
    private String courriel;

    /** Haché avec BCrypt (60 caractères) : jamais stocké en clair. */
    @Column(name = "mot_passe", nullable = false, length = 60)
    private String motDePasse;

    @Column(name = "actif", nullable = false)
    private boolean actif = true;

    @Column(name = "date_inscription", nullable = false)
    private LocalDateTime dateInscription = LocalDateTime.now();

    /** Table associative utilisateur_role (clé primaire composite id_utilisateur + id_role). */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "utilisateur_role",
            joinColumns = @JoinColumn(name = "id_utilisateur"),
            inverseJoinColumns = @JoinColumn(name = "id_role"))
    private Set<Role> roles = new HashSet<>();

    protected Utilisateur() { }

    public Utilisateur(String prenom, String nom, String courriel, String motDePasseEncode) {
        this.prenom = prenom;
        this.nom = nom;
        this.courriel = courriel;
        this.motDePasse = motDePasseEncode;
    }

    public boolean aRole(String nomRole) {
        return roles.stream().anyMatch(r -> r.getNom().equals(nomRole));
    }

    public String getNomComplet() { return prenom + " " + nom; }

    public Integer getId() { return id; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getCourriel() { return courriel; }
    public void setCourriel(String courriel) { this.courriel = courriel; }
    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }
    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }
    public LocalDateTime getDateInscription() { return dateInscription; }
    public Set<Role> getRoles() { return roles; }
    public void setRoles(Set<Role> roles) { this.roles = roles; }
}

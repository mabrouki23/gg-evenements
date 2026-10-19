package ca.qc.cgodin.ggevenements.model;

import jakarta.persistence.*;

/** Table « role » : PUBLIC, ORGANISATEUR ou ADMIN. Dans Spring Security l'autorité est « ROLE_ » + nom_role. */
@Entity
@Table(name = "role")
public class Role {

    public static final String PUBLIC = "PUBLIC";
    public static final String ORGANISATEUR = "ORGANISATEUR";
    public static final String ADMIN = "ADMIN";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_role")
    private Integer id;

    @Column(name = "nom_role", nullable = false, length = 30)
    private String nom;

    @Column(name = "description", length = 150)
    private String description;

    protected Role() { }

    public Role(String nom, String description) {
        this.nom = nom;
        this.description = description;
    }

    public Integer getId() { return id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}

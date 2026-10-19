package ca.qc.cgodin.ggevenements.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/** Table « avis ». */
@Entity
@Table(name = "avis",
        uniqueConstraints = @UniqueConstraint(name = "uk_avis_utilisateur_evenement",
                columnNames = {"id_utilisateur", "id_evenement"}))
public class Avis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_avis")
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_evenement", nullable = false)
    private Evenement evenement;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_utilisateur", nullable = false)
    private Utilisateur utilisateur;

    @Column(name = "note", nullable = false)
    private int note;

    @Column(name = "commentaire", length = 500)
    private String commentaire;

    @Column(name = "date_avis", nullable = false)
    private LocalDateTime dateAvis = LocalDateTime.now();

    protected Avis() { }

    public Avis(Utilisateur utilisateur, Evenement evenement, int note, String commentaire) {
        this.utilisateur = utilisateur;
        this.evenement = evenement;
        this.note = note;
        this.commentaire = commentaire;
        this.dateAvis = LocalDateTime.now();
    }

    public Integer getId() { return id; }
    public Utilisateur getUtilisateur() { return utilisateur; }
    public Evenement getEvenement() { return evenement; }
    public int getNote() { return note; }
    public String getCommentaire() { return commentaire; }
    public LocalDateTime getDateAvis() { return dateAvis; }
}

package ca.qc.cgodin.ggevenements.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/** Table « inscription » (classe d'association utilisateur / événement, porte le statut et la liste d'attente). */
@Entity
@Table(name = "inscription",
        uniqueConstraints = @UniqueConstraint(name = "uk_inscription_utilisateur_evenement",
                columnNames = {"id_utilisateur", "id_evenement"}))
public class Inscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_inscription")
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_evenement", nullable = false)
    private Evenement evenement;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_utilisateur", nullable = false)
    private Utilisateur utilisateur;

    /** Sert aussi à déterminer l'ordre de la liste d'attente. */
    @Column(name = "date_inscription", nullable = false)
    private LocalDateTime dateInscription;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 20)
    private StatutInscription statut;

    @Column(name = "presence_confirmee", nullable = false)
    private boolean presenceConfirmee = false;

    protected Inscription() { }

    public Inscription(Utilisateur utilisateur, Evenement evenement, StatutInscription statut, LocalDateTime dateInscription) {
        this.utilisateur = utilisateur;
        this.evenement = evenement;
        this.statut = statut;
        this.dateInscription = dateInscription;
    }

    public Integer getId() { return id; }
    public Utilisateur getUtilisateur() { return utilisateur; }
    public Evenement getEvenement() { return evenement; }
    public StatutInscription getStatut() { return statut; }
    public void setStatut(StatutInscription statut) { this.statut = statut; }
    public LocalDateTime getDateInscription() { return dateInscription; }
    public boolean isPresenceConfirmee() { return presenceConfirmee; }
    public void setPresenceConfirmee(boolean presenceConfirmee) { this.presenceConfirmee = presenceConfirmee; }
}

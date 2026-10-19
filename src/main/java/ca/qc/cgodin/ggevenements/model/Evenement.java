package ca.qc.cgodin.ggevenements.model;

import jakarta.persistence.*;
import org.hibernate.annotations.Formula;

import java.time.LocalDateTime;

/** Table « evenement ». */
@Entity
@Table(name = "evenement")
public class Evenement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evenement")
    private Integer id;

    @Column(name = "titre", nullable = false, length = 120)
    private String titre;

    @Column(name = "description", nullable = false, length = 1000)
    private String description;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_club", nullable = false)
    private Club club;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_categorie", nullable = false)
    private Categorie categorie;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_lieu", nullable = false)
    private Lieu lieu;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_createur", nullable = false)
    private Utilisateur createur;

    @Column(name = "date_debut", nullable = false)
    private LocalDateTime dateDebut;

    @Column(name = "date_fin", nullable = false)
    private LocalDateTime dateFin;

    @Column(name = "capacite_max", nullable = false)
    private int capaciteMax;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 20)
    private StatutEvenement statut = StatutEvenement.PLANIFIE;

    /** Nom du fichier d'affiche téléversé (null = image par défaut). */
    @Column(name = "image_affiche", length = 150)
    private String image;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    // Valeurs calculées par la base de données (lecture seule, aucune colonne) : évitent de charger les
    // inscriptions en mémoire et permettent le tri SQL par « places restantes ».
    @Formula("(SELECT COUNT(*) FROM inscription i WHERE i.id_evenement = id_evenement AND i.statut = 'CONFIRMEE')")
    private int nbConfirmees;

    @Formula("(SELECT COUNT(*) FROM inscription i WHERE i.id_evenement = id_evenement AND i.statut = 'EN_ATTENTE')")
    private int nbEnAttente;

    @Formula("capacite_max - (SELECT COUNT(*) FROM inscription i WHERE i.id_evenement = id_evenement AND i.statut = 'CONFIRMEE')")
    private int placesRestantes;

    protected Evenement() { }

    public Evenement(String titre, String description, LocalDateTime dateDebut, LocalDateTime dateFin,
                     int capaciteMax, Categorie categorie, Lieu lieu, Club club, Utilisateur createur) {
        this.titre = titre;
        this.description = description;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.capaciteMax = capaciteMax;
        this.categorie = categorie;
        this.lieu = lieu;
        this.club = club;
        this.createur = createur;
        this.statut = StatutEvenement.PLANIFIE;
        this.dateCreation = LocalDateTime.now();
    }

    public boolean isAnnule() { return statut == StatutEvenement.ANNULE; }

    /** Terminé = statut TERMINE, ou PLANIFIE dont la date de fin est passée (avant la synchronisation planifiée). */
    public boolean isTermine() {
        return statut == StatutEvenement.TERMINE
                || (statut == StatutEvenement.PLANIFIE && dateFin.isBefore(LocalDateTime.now()));
    }

    /** Planifié et pas encore commencé : inscription, désinscription et modification permises. */
    public boolean isOuvertAuxInscriptions() {
        return statut == StatutEvenement.PLANIFIE && dateDebut.isAfter(LocalDateTime.now());
    }

    public Integer getId() { return id; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public LocalDateTime getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDateTime dateDebut) { this.dateDebut = dateDebut; }
    public LocalDateTime getDateFin() { return dateFin; }
    public void setDateFin(LocalDateTime dateFin) { this.dateFin = dateFin; }
    public int getCapaciteMax() { return capaciteMax; }
    public void setCapaciteMax(int capaciteMax) { this.capaciteMax = capaciteMax; }
    public StatutEvenement getStatut() { return statut; }
    public void setStatut(StatutEvenement statut) { this.statut = statut; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public Categorie getCategorie() { return categorie; }
    public void setCategorie(Categorie categorie) { this.categorie = categorie; }
    public Lieu getLieu() { return lieu; }
    public void setLieu(Lieu lieu) { this.lieu = lieu; }
    public Club getClub() { return club; }
    public Utilisateur getCreateur() { return createur; }
    public int getNbConfirmees() { return nbConfirmees; }
    public int getNbEnAttente() { return nbEnAttente; }
    public int getPlacesRestantes() { return placesRestantes; }
}

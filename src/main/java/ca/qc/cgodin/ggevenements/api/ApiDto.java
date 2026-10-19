package ca.qc.cgodin.ggevenements.api;

import java.time.LocalDateTime;
import java.util.List;

/** Objets JSON de l'API REST. Aucune entité JPA n'est jamais renvoyée telle quelle (pas de hachage de mot de passe, etc.). */
public final class ApiDto {

    private ApiDto() { }

    public record MoiDto(Integer id, String nomComplet, List<String> roles) { }

    public record CategorieDto(Integer id, String nom, String description, String couleur) { }

    public record LieuDto(Integer id, String nom, String campus, String adresse, int capaciteMax) { }

    public record ClubResume(Integer id, String nom) { }

    public record ClubDto(Integer id, String nom, String description, String courrielContact, boolean actif,
                          Integer responsableId, String responsableNom) { }

    public record UtilisateurDto(Integer id, String prenom, String nom, String courriel, boolean actif, List<String> roles) { }

    public record EvenementDto(Integer id, String titre, String description, String imageUrl,
                               CategorieDto categorie, ClubResume club, LieuDto lieu,
                               LocalDateTime dateDebut, LocalDateTime dateFin, int capaciteMax,
                               String statut, boolean termine, boolean annule,
                               int nbConfirmees, int nbEnAttente, int placesRestantes) { }

    public record PageDto<T>(List<T> contenu, int page, int totalPages, long totalElements) { }

    public record AvisDto(Integer id, String auteur, int note, String commentaire, LocalDateTime date) { }

    public record DetailDto(EvenementDto evenement, String monStatut, boolean peutSinscrire,
                            boolean peutSeDesinscrire, List<AvisDto> avis, Double moyenne) { }

    public record LigneMesEvenementsDto(EvenementDto evenement, String statut, boolean peutSeDesinscrire,
                                        boolean avisDejaSoumis, boolean peutLaisserAvis) { }

    public record MesEvenementsDto(List<LigneMesEvenementsDto> aVenir, List<LigneMesEvenementsDto> passes) { }

    public record InscriptionDto(Integer id, String nom, String courriel, LocalDateTime dateInscription,
                                 boolean presenceConfirmee) { }

    public record InscriptionsDto(EvenementDto evenement, List<InscriptionDto> confirmees, List<InscriptionDto> enAttente) { }

    public record ClubActifDto(String nom, long nbEvenements) { }

    public record StatsDto(long planifies, long annules, long termines, Double tauxRemplissage,
                           List<ClubActifDto> clubsActifs, Double noteMoyenne, long nbAvis) { }

    public record ReferenceDto(List<CategorieDto> categories, List<ClubResume> clubs, List<LieuDto> lieux) { }

    public record FicheClubDto(ClubDto club, List<EvenementDto> evenements) { }

    /** Données pré-remplies du formulaire de modification (dates au format des champs datetime-local). */
    public record EvenementFormDto(String titre, String description, Integer categorieId, Integer lieuId,
                                   String dateDebut, String dateFin, Integer capaciteMax, String imageActuelle) { }

    public record OrganisateurClubsDto(List<ClubDto> clubs, Integer clubActifId, ClubDto clubCreation, String messageClub) { }

    public record StatutDto(String statut) { }

    public record PresenceDto(boolean presente) { }

    public record ClubActifChoixDto(Integer id) { }
}

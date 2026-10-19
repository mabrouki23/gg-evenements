package ca.qc.cgodin.ggevenements.api;

import ca.qc.cgodin.ggevenements.api.ApiDto.*;
import ca.qc.cgodin.ggevenements.dto.AvisForm;
import ca.qc.cgodin.ggevenements.dto.FiltreEvenements;
import ca.qc.cgodin.ggevenements.model.Evenement;
import ca.qc.cgodin.ggevenements.model.StatutInscription;
import ca.qc.cgodin.ggevenements.security.UtilisateurDetails;
import ca.qc.cgodin.ggevenements.service.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/** Espace public : fonctionnalités 2 à 8 (liste, recherche, détail, inscription, désinscription, mes événements, avis). */
@RestController
@RequestMapping("/api")
public class ApiEvenementController {

    private final EvenementService evenementService;
    private final InscriptionService inscriptionService;
    private final AvisService avisService;
    private final CategorieService categorieService;
    private final ClubService clubService;
    private final LieuService lieuService;

    public ApiEvenementController(EvenementService evenementService, InscriptionService inscriptionService,
                                  AvisService avisService, CategorieService categorieService,
                                  ClubService clubService, LieuService lieuService) {
        this.evenementService = evenementService;
        this.inscriptionService = inscriptionService;
        this.avisService = avisService;
        this.categorieService = categorieService;
        this.clubService = clubService;
        this.lieuService = lieuService;
    }

    /** Listes déroulantes : catégories, clubs ACTIFS seulement, lieux. */
    @GetMapping("/reference")
    public ReferenceDto reference() {
        return new ReferenceDto(
                categorieService.toutes().stream().map(ApiMapper::categorie).toList(),
                clubService.actifs().stream().map(ApiMapper::clubResume).toList(),
                lieuService.tous().stream().map(ApiMapper::lieu).toList());
    }

    /**
     * Événements à venir (9 par page). Sert à la fois la liste (fonctionnalité 2) et la recherche avancée (7) :
     * tous les paramètres sont facultatifs et combinables.
     */
    @GetMapping("/evenements")
    public PageDto<EvenementDto> liste(@RequestParam(required = false) String q,
                                       @RequestParam(required = false) Integer categorie,
                                       @RequestParam(required = false) Integer club,
                                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate du,
                                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate au,
                                       @RequestParam(defaultValue = "date") String tri,
                                       @RequestParam(defaultValue = "0") int page) {
        Page<Evenement> p = evenementService.aVenir(new FiltreEvenements(q, categorie, club, du, au), tri, page);
        return new PageDto<>(p.getContent().stream().map(ApiMapper::evenement).toList(),
                p.getNumber(), p.getTotalPages(), p.getTotalElements());
    }

    @GetMapping("/evenements/{id}")
    public DetailDto detail(@PathVariable Integer id, @AuthenticationPrincipal UtilisateurDetails moi) {
        return ApiMapper.detail(evenementService.detail(id, moi.getId()));
    }

    /** Un clic : CONFIRMEE si la capacité le permet, sinon EN_ATTENTE. */
    @PostMapping("/evenements/{id}/inscription")
    @ResponseStatus(HttpStatus.CREATED)
    public StatutDto sinscrire(@PathVariable Integer id, @AuthenticationPrincipal UtilisateurDetails moi) {
        StatutInscription statut = inscriptionService.sinscrire(id, moi.getId());
        return new StatutDto(statut.name());
    }

    /** Retire l'inscription et promeut, dans la même transaction, la plus ancienne personne en attente. */
    @DeleteMapping("/evenements/{id}/inscription")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void seDesinscrire(@PathVariable Integer id, @AuthenticationPrincipal UtilisateurDetails moi) {
        inscriptionService.seDesinscrire(id, moi.getId());
    }

    @GetMapping("/mes-evenements")
    public MesEvenementsDto mesEvenements(@AuthenticationPrincipal UtilisateurDetails moi) {
        return ApiMapper.mesEvenements(inscriptionService.mesEvenements(moi.getId()));
    }

    @PostMapping("/evenements/{id}/avis")
    @ResponseStatus(HttpStatus.CREATED)
    public void laisserAvis(@PathVariable Integer id, @Valid @RequestBody AvisForm form,
                            @AuthenticationPrincipal UtilisateurDetails moi) {
        avisService.soumettre(id, moi.getId(), form);
    }

    /** Fiche publique d'un club avec ses événements à venir. */
    @GetMapping("/clubs/{id}")
    public FicheClubDto fiche(@PathVariable Integer id) {
        return new FicheClubDto(ApiMapper.club(clubService.trouver(id)),
                evenementService.aVenirDuClub(id).stream().map(ApiMapper::evenement).toList());
    }
}

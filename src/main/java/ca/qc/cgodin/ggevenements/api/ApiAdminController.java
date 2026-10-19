package ca.qc.cgodin.ggevenements.api;

import ca.qc.cgodin.ggevenements.api.ApiDto.*;
import ca.qc.cgodin.ggevenements.dto.CategorieForm;
import ca.qc.cgodin.ggevenements.dto.ClubForm;
import ca.qc.cgodin.ggevenements.dto.LieuForm;
import ca.qc.cgodin.ggevenements.security.UtilisateurDetails;
import ca.qc.cgodin.ggevenements.service.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Espace administrateur (fonctionnalités 13 à 16) : utilisateurs, clubs, catégories, lieux, statistiques. */
@RestController
@RequestMapping("/api/admin")
public class ApiAdminController {

    private final UtilisateurService utilisateurService;
    private final ClubService clubService;
    private final CategorieService categorieService;
    private final LieuService lieuService;
    private final StatistiquesService statistiquesService;

    public ApiAdminController(UtilisateurService utilisateurService, ClubService clubService,
                              CategorieService categorieService, LieuService lieuService,
                              StatistiquesService statistiquesService) {
        this.utilisateurService = utilisateurService;
        this.clubService = clubService;
        this.categorieService = categorieService;
        this.lieuService = lieuService;
        this.statistiquesService = statistiquesService;
    }

    // ------------------------------------------------------------ utilisateurs (13)

    @GetMapping("/utilisateurs")
    public List<UtilisateurDto> utilisateurs() {
        return utilisateurService.tous().stream().map(ApiMapper::utilisateur).toList();
    }

    @PostMapping("/utilisateurs/{id}/actif")
    public UtilisateurDto basculerActif(@PathVariable Integer id, @AuthenticationPrincipal UtilisateurDetails moi) {
        return ApiMapper.utilisateur(utilisateurService.basculerActif(id, moi.getId()));
    }

    @PostMapping("/utilisateurs/{id}/organisateur")
    public UtilisateurDto basculerOrganisateur(@PathVariable Integer id) {
        return ApiMapper.utilisateur(utilisateurService.basculerOrganisateur(id));
    }

    // ------------------------------------------------------------ clubs (14)

    @GetMapping("/clubs")
    public List<ClubDto> clubs() {
        return clubService.tous().stream().map(ApiMapper::club).toList();
    }

    @GetMapping("/clubs/{id}")
    public ClubDto club(@PathVariable Integer id) {
        return ApiMapper.club(clubService.trouver(id));
    }

    @PostMapping("/clubs")
    @ResponseStatus(HttpStatus.CREATED)
    public ClubDto creerClub(@Valid @RequestBody ClubForm form) {
        return ApiMapper.club(clubService.creer(form));
    }

    @PutMapping("/clubs/{id}")
    public ClubDto modifierClub(@PathVariable Integer id, @Valid @RequestBody ClubForm form) {
        return ApiMapper.club(clubService.modifier(id, form));
    }

    @PostMapping("/clubs/{id}/actif")
    public ClubDto basculerClub(@PathVariable Integer id) {
        return ApiMapper.club(clubService.basculerActif(id));
    }

    // ------------------------------------------------------------ catégories (15)

    @GetMapping("/categories")
    public List<CategorieDto> categories() {
        return categorieService.toutes().stream().map(ApiMapper::categorie).toList();
    }

    @PostMapping("/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public CategorieDto creerCategorie(@Valid @RequestBody CategorieForm form) {
        return ApiMapper.categorie(categorieService.creer(form));
    }

    @PutMapping("/categories/{id}")
    public CategorieDto modifierCategorie(@PathVariable Integer id, @Valid @RequestBody CategorieForm form) {
        return ApiMapper.categorie(categorieService.modifier(id, form));
    }

    /** Refusée avec un message clair (HTTP 422) si au moins un événement utilise la catégorie. */
    @DeleteMapping("/categories/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimerCategorie(@PathVariable Integer id) {
        categorieService.supprimer(id);
    }

    // ------------------------------------------------------------ lieux (15)

    @GetMapping("/lieux")
    public List<LieuDto> lieux() {
        return lieuService.tous().stream().map(ApiMapper::lieu).toList();
    }

    @PostMapping("/lieux")
    @ResponseStatus(HttpStatus.CREATED)
    public LieuDto creerLieu(@Valid @RequestBody LieuForm form) {
        return ApiMapper.lieu(lieuService.creer(form));
    }

    @PutMapping("/lieux/{id}")
    public LieuDto modifierLieu(@PathVariable Integer id, @Valid @RequestBody LieuForm form) {
        return ApiMapper.lieu(lieuService.modifier(id, form));
    }

    @DeleteMapping("/lieux/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimerLieu(@PathVariable Integer id) {
        lieuService.supprimer(id);
    }

    // ------------------------------------------------------------ tableau de bord (16)

    @GetMapping("/stats")
    public StatsDto stats() {
        return ApiMapper.stats(statistiquesService.calculer());
    }
}

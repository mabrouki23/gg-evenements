package ca.qc.cgodin.ggevenements.api;

import ca.qc.cgodin.ggevenements.api.ApiDto.*;
import ca.qc.cgodin.ggevenements.dto.EvenementForm;
import ca.qc.cgodin.ggevenements.exception.RegleAffairesException;
import ca.qc.cgodin.ggevenements.model.Club;
import ca.qc.cgodin.ggevenements.model.Evenement;
import ca.qc.cgodin.ggevenements.security.UtilisateurDetails;
import ca.qc.cgodin.ggevenements.service.ClubService;
import ca.qc.cgodin.ggevenements.service.EvenementService;
import ca.qc.cgodin.ggevenements.service.InscriptionService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Espace organisateur (fonctionnalités 10 à 12). Toutes les vérifications d'appartenance au club sont faites
 * dans la couche service (HTTP 403) : l'API ne se fie jamais au frontend.
 */
@RestController
@RequestMapping("/api/organisateur")
public class ApiOrganisateurController {

    /** Club « actif » de la session (organisateur responsable de plusieurs clubs), toujours revérifié par le service. */
    private static final String CLUB_ACTIF = "clubActifId";

    private final EvenementService evenementService;
    private final InscriptionService inscriptionService;
    private final ClubService clubService;

    public ApiOrganisateurController(EvenementService evenementService, InscriptionService inscriptionService,
                                     ClubService clubService) {
        this.evenementService = evenementService;
        this.inscriptionService = inscriptionService;
        this.clubService = clubService;
    }

    /** Mes clubs, le club actif et le club au nom duquel un événement serait créé (null + message si impossible). */
    @GetMapping("/clubs")
    public OrganisateurClubsDto clubs(@AuthenticationPrincipal UtilisateurDetails moi, HttpSession session) {
        List<Club> clubs = clubService.clubsDe(moi.getId());
        Integer clubActifId = (Integer) session.getAttribute(CLUB_ACTIF);
        ClubDto creation = null;
        String message = null;
        try {
            creation = ApiMapper.club(evenementService.clubPourCreation(moi.getId(), clubActifId));
        } catch (RegleAffairesException e) {
            message = e.getMessage();
        }
        return new OrganisateurClubsDto(clubs.stream().map(ApiMapper::club).toList(), clubActifId, creation, message);
    }

    /** Choisit le club actif ; le service refuse (403) un club qui n'appartient pas à l'organisateur. */
    @PostMapping("/club-actif")
    public ClubDto choisirClub(@RequestBody ClubActifChoixDto choix, @AuthenticationPrincipal UtilisateurDetails moi,
                               HttpSession session) {
        Club c = clubService.clubDuResponsable(choix.id(), moi.getId());
        session.setAttribute(CLUB_ACTIF, c.getId());
        return ApiMapper.club(c);
    }

    @GetMapping("/evenements")
    public List<EvenementDto> evenements(@AuthenticationPrincipal UtilisateurDetails moi) {
        return evenementService.evenementsDuClubDe(moi.getId()).stream().map(ApiMapper::evenement).toList();
    }

    /** Création (multipart : champs du formulaire + affiche facultative). Le club n'est jamais lu dans la requête. */
    @PostMapping(value = "/evenements", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public EvenementDto creer(@Valid @ModelAttribute EvenementForm form, @AuthenticationPrincipal UtilisateurDetails moi,
                              HttpSession session) {
        Evenement e = evenementService.creer(form, moi.getId(), (Integer) session.getAttribute(CLUB_ACTIF));
        return ApiMapper.evenement(e);
    }

    @GetMapping("/evenements/{id}/formulaire")
    public EvenementFormDto formulaire(@PathVariable Integer id, @AuthenticationPrincipal UtilisateurDetails moi) {
        return ApiMapper.formulaire(evenementService.pourModification(id, moi.getId())); // 403 si autre club
    }

    @PostMapping(value = "/evenements/{id}/modifier", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public EvenementDto modifier(@PathVariable Integer id, @Valid @ModelAttribute EvenementForm form,
                                 @AuthenticationPrincipal UtilisateurDetails moi) {
        evenementService.pourModification(id, moi.getId()); // 403 avant tout traitement
        return ApiMapper.evenement(evenementService.modifier(id, form, moi.getId()));
    }

    /** Annulation : changement de statut seulement, rien n'est supprimé. */
    @PostMapping("/evenements/{id}/annuler")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void annuler(@PathVariable Integer id, @AuthenticationPrincipal UtilisateurDetails moi) {
        evenementService.annuler(id, moi.getId());
    }

    @GetMapping("/evenements/{id}/inscriptions")
    public InscriptionsDto inscriptions(@PathVariable Integer id, @AuthenticationPrincipal UtilisateurDetails moi) {
        return ApiMapper.inscriptions(inscriptionService.inscriptionsPourOrganisateur(id, moi.getId()));
    }

    @PostMapping("/inscriptions/{id}/presence")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void presence(@PathVariable Integer id, @RequestBody PresenceDto presence,
                         @AuthenticationPrincipal UtilisateurDetails moi) {
        inscriptionService.marquerPresence(id, presence.presente(), moi.getId());
    }
}

package ca.qc.cgodin.ggevenements.api;

import ca.qc.cgodin.ggevenements.api.ApiDto.MoiDto;
import ca.qc.cgodin.ggevenements.security.UtilisateurDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Session courante et jeton CSRF. La connexion (POST /api/login) et la déconnexion (POST /api/logout) sont gérées par Spring Security. */
@RestController
@RequestMapping("/api")
public class ApiAuthController {

    /** Force la création du cookie XSRF-TOKEN, que le frontend renvoie ensuite dans l'en-tête X-XSRF-TOKEN. */
    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken token) {
        return Map.of("token", token.getToken());
    }

    /** 200 avec l'utilisateur connecté, ou 401 si personne n'est connecté. */
    @GetMapping("/me")
    public ResponseEntity<MoiDto> moi(@AuthenticationPrincipal UtilisateurDetails u, CsrfToken token) {
        token.getToken(); // s'assure que le cookie CSRF existe dès le chargement de l'application
        if (u == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(ApiMapper.moi(u));
    }
}

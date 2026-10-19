package ca.qc.cgodin.ggevenements.api;

import ca.qc.cgodin.ggevenements.exception.RegleAffairesException;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Transforme toutes les erreurs de l'API en JSON : { "message": "...", "errors": { "champ": "message" } }.
 * Les erreurs de champ permettent au frontend React d'afficher chaque message à côté du champ fautif.
 */
@RestControllerAdvice(basePackages = "ca.qc.cgodin.ggevenements.api")
public class ApiExceptionHandler {

    private final MessageSource messageSource;

    public ApiExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    private ResponseEntity<Map<String, Object>> reponse(HttpStatus statut, String message, Map<String, String> erreurs) {
        Map<String, Object> corps = new LinkedHashMap<>();
        corps.put("message", message);
        corps.put("errors", erreurs);
        return ResponseEntity.status(statut).body(corps);
    }

    /** Règle d'affaires violée (capacité minimale, doublon, suppression d'un élément utilisé...). */
    @ExceptionHandler(RegleAffairesException.class)
    public ResponseEntity<Map<String, Object>> regleAffaires(RegleAffairesException e) {
        Map<String, String> erreurs = new LinkedHashMap<>();
        if (e.getChamp() != null) {
            erreurs.put(e.getChamp(), e.getMessage());
        }
        return reponse(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage(), erreurs);
    }

    /** Bean Validation (@Valid) : une entrée par champ en erreur. */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<Map<String, Object>> validation(BindException e) {
        Map<String, String> erreurs = new LinkedHashMap<>();
        for (FieldError fe : e.getBindingResult().getFieldErrors()) {
            erreurs.putIfAbsent(fe.getField(), messageSource.getMessage(fe, LocaleContextHolder.getLocale()));
        }
        return reponse(HttpStatus.BAD_REQUEST, "Certaines données sont invalides.", erreurs);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> statut(ResponseStatusException e) {
        HttpStatus statut = HttpStatus.resolve(e.getStatusCode().value());
        String message = e.getReason() != null ? e.getReason() : (statut != null ? statut.getReasonPhrase() : "Erreur");
        return ResponseEntity.status(e.getStatusCode()).body(Map.of("message", message, "errors", Map.of()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> accesRefuse(AccessDeniedException e) {
        return reponse(HttpStatus.FORBIDDEN, "Accès refusé.", Map.of());
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class})
    public ResponseEntity<Map<String, Object>> requeteInvalide(Exception e) {
        return reponse(HttpStatus.BAD_REQUEST, "Requête invalide.", Map.of());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> fichierTropGros(MaxUploadSizeExceededException e) {
        return reponse(HttpStatus.PAYLOAD_TOO_LARGE, "L'affiche dépasse la taille maximale (5 Mo).",
                Map.of("affiche", "L'affiche dépasse la taille maximale (5 Mo)."));
    }
}

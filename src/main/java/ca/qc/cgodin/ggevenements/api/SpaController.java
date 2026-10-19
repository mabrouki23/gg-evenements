package ca.qc.cgodin.ggevenements.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Sert l'application React : toute adresse « de page » (qui n'est ni /api, ni /uploads, ni un fichier statique)
 * renvoie index.html, et c'est React Router qui affiche la bonne page.
 */
@Controller
public class SpaController {

    @GetMapping({"/", "/{chemin:^(?!api$|uploads$|assets$|images$)[^\\.]*}",
            "/{chemin:^(?!api$|uploads$|assets$|images$)[^\\.]*}/**"})
    public String index() {
        return "forward:/index.html";
    }
}

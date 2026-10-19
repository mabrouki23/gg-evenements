package ca.qc.cgodin.ggevenements.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@EnableScheduling
public class WebConfig implements WebMvcConfigurer {

    private final Path dossierTeleversements;

    public WebConfig(@Value("${app.upload-dir:uploads}") String dossier) {
        this.dossierTeleversements = Paths.get(dossier).toAbsolutePath().normalize();
    }

    /** Sert les affiches téléversées à l'adresse /uploads/** (réservé aux utilisateurs connectés). */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Le « / » final est indispensable : sans lui, Spring colle le nom du fichier au nom du dossier.
        String emplacement = dossierTeleversements.toUri().toString();
        if (!emplacement.endsWith("/")) {
            emplacement += "/";
        }
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(emplacement);
    }
}

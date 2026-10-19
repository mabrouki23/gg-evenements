package ca.qc.cgodin.ggevenements.service;

import ca.qc.cgodin.ggevenements.exception.RegleAffairesException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

/** Enregistrement des affiches téléversées (type et taille vérifiés, nom de fichier généré). */
@Service
public class StockageService {

    private static final Map<String, String> TYPES = Map.of(
            "image/jpeg", ".jpg", "image/png", ".png", "image/gif", ".gif", "image/webp", ".webp");

    private final Path dossier;

    public StockageService(@Value("${app.upload-dir:uploads}") String dossier) {
        this.dossier = Paths.get(dossier).toAbsolutePath().normalize();
    }

    /** @return le nom du fichier enregistré, ou null si aucun fichier n'a été fourni. */
    public String enregistrer(MultipartFile fichier, String champ) {
        if (fichier == null || fichier.isEmpty()) {
            return null;
        }
        String extension = TYPES.get(fichier.getContentType());
        if (extension == null) {
            throw new RegleAffairesException(champ, "L'affiche doit être une image JPEG, PNG, GIF ou WebP.");
        }
        try {
            Files.createDirectories(dossier);
            String nom = UUID.randomUUID() + extension;
            try (InputStream in = fichier.getInputStream()) {
                Files.copy(in, dossier.resolve(nom), StandardCopyOption.REPLACE_EXISTING);
            }
            return nom;
        } catch (IOException e) {
            throw new RegleAffairesException(champ, "Impossible d'enregistrer l'affiche. Réessayez.");
        }
    }
}

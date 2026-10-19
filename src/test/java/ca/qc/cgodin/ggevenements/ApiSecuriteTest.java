package ca.qc.cgodin.ggevenements;

import ca.qc.cgodin.ggevenements.config.DonneesInitiales;
import ca.qc.cgodin.ggevenements.model.Utilisateur;
import ca.qc.cgodin.ggevenements.repository.UtilisateurRepository;
import ca.qc.cgodin.ggevenements.security.UtilisateurDetails;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Sécurité de l'API REST : 401 / 403 en JSON, connexion JSON, CSRF, accès par rôle. */
@SpringBootTest
@AutoConfigureMockMvc
class ApiSecuriteTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UtilisateurRepository utilisateurRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private UtilisateurDetails details(String courriel) {
        Utilisateur u = utilisateurRepository.findByCourrielIgnoreCase(courriel).orElseThrow();
        return new UtilisateurDetails(u);
    }

    @Test
    void motsDePasseSontEncodesEnBCrypt() {
        Utilisateur u = utilisateurRepository.findByCourrielIgnoreCase(DonneesInitiales.EMAIL_ETU1).orElseThrow();
        assertNotEquals(DonneesInitiales.MDP_ETUDIANT, u.getMotDePasse());
        assertTrue(u.getMotDePasse().startsWith("$2"));
        assertTrue(passwordEncoder.matches(DonneesInitiales.MDP_ETUDIANT, u.getMotDePasse()));
    }

    @Test
    void sansConnexionLApiRepond401() throws Exception {
        mockMvc.perform(get("/api/me")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/evenements")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/mes-evenements")).andExpect(status().isUnauthorized());
    }

    @Test
    void connexionValideRenvoieLUtilisateurEnJson() throws Exception {
        mockMvc.perform(post("/api/login").with(csrf())
                        .param("courriel", DonneesInitiales.EMAIL_ETU1)
                        .param("motDePasse", DonneesInitiales.MDP_ETUDIANT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomComplet").value("Léa Bouchard"));
    }

    @Test
    void identifiantsInvalidesDonnentUnMessageGenerique() throws Exception {
        mockMvc.perform(post("/api/login").with(csrf())
                        .param("courriel", DonneesInitiales.EMAIL_ETU1)
                        .param("motDePasse", "mauvais"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID"))
                .andExpect(jsonPath("$.message").value("Identifiants invalides"));
        mockMvc.perform(post("/api/login").with(csrf())
                        .param("courriel", "inconnu@ggevenements.ca")
                        .param("motDePasse", "mauvais"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID"));
    }

    @Test
    void compteDesactiveEstSignaleSeulementSiLeMotDePasseEstBon() throws Exception {
        mockMvc.perform(post("/api/login").with(csrf())
                        .param("courriel", DonneesInitiales.EMAIL_INACTIF)
                        .param("motDePasse", DonneesInitiales.MDP_ETUDIANT))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("DISABLED"));
        // Mauvais mot de passe : aucune fuite d'information sur l'état du compte
        mockMvc.perform(post("/api/login").with(csrf())
                        .param("courriel", DonneesInitiales.EMAIL_INACTIF)
                        .param("motDePasse", "mauvais"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID"));
    }

    @Test
    void utilisateurOrdinaireNaPasAccesAuxEspacesReserves() throws Exception {
        UtilisateurDetails etu = details(DonneesInitiales.EMAIL_ETU1);
        mockMvc.perform(get("/api/evenements").with(user(etu))).andExpect(status().isOk());
        mockMvc.perform(get("/api/admin/utilisateurs").with(user(etu))).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/admin/stats").with(user(etu))).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/organisateur/evenements").with(user(etu))).andExpect(status().isForbidden());
    }

    @Test
    void administrateurAccedeALAdministrationMaisPasAuModuleOrganisateur() throws Exception {
        UtilisateurDetails admin = details(DonneesInitiales.EMAIL_ADMIN);
        mockMvc.perform(get("/api/admin/utilisateurs").with(user(admin))).andExpect(status().isOk());
        mockMvc.perform(get("/api/admin/stats").with(user(admin))).andExpect(status().isOk());
        mockMvc.perform(get("/api/organisateur/evenements").with(user(admin))).andExpect(status().isForbidden());
    }

    @Test
    void organisateurAccedeASonEspace() throws Exception {
        UtilisateurDetails org = details(DonneesInitiales.EMAIL_ORG1);
        mockMvc.perform(get("/api/organisateur/evenements").with(user(org))).andExpect(status().isOk());
        mockMvc.perform(get("/api/admin/utilisateurs").with(user(org))).andExpect(status().isForbidden());
    }

    @Test
    void requeteDeModificationSansJetonCsrfEstRefusee() throws Exception {
        UtilisateurDetails etu = details(DonneesInitiales.EMAIL_ETU1);
        mockMvc.perform(post("/api/evenements/1/inscription").with(user(etu))).andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/evenements/1/inscription").with(user(etu))).andExpect(status().isForbidden());
    }

    @Test
    void liste9ParPageEtDetailInexistantDonne404() throws Exception {
        UtilisateurDetails etu = details(DonneesInitiales.EMAIL_ETU1);
        mockMvc.perform(get("/api/evenements").with(user(etu)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenu.length()").value(9));
        mockMvc.perform(get("/api/evenements/999999").with(user(etu)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void deconnexionRepond204() throws Exception {
        UtilisateurDetails etu = details(DonneesInitiales.EMAIL_ETU1);
        mockMvc.perform(post("/api/logout").with(user(etu)).with(csrf())).andExpect(status().isNoContent());
    }
}

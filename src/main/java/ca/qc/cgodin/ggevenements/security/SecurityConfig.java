package ca.qc.cgodin.ggevenements.security;

import ca.qc.cgodin.ggevenements.api.ApiDto.MoiDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Sécurité de l'API REST pour le frontend React :
 *  - session HTTP (cookie JSESSIONID, non lisible par JavaScript) ;
 *  - CSRF par cookie XSRF-TOKEN que React renvoie dans l'en-tête X-XSRF-TOKEN ;
 *  - réponses JSON (401 / 403) au lieu de redirections et de pages HTML.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /** Mots de passe : toujours encodés avec BCrypt (jamais en clair). */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(UserDetailsService userDetailsService,
                                                            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        // Exécuté seulement APRÈS la vérification du mot de passe : un compte désactivé avec le bon mot de passe
        // reçoit un message clair, alors qu'un mauvais mot de passe reçoit toujours « Identifiants invalides ».
        provider.setPostAuthenticationChecks(details -> {
            if (details instanceof UtilisateurDetails u && !u.isActif()) {
                throw new DisabledException("Compte désactivé");
            }
        });
        return provider;
    }

    private static void ecrireJson(HttpServletResponse response, ObjectMapper mapper, HttpStatus statut, Object corps)
            throws IOException {
        response.setStatus(statut.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        mapper.writeValue(response.getWriter(), corps);
    }

    /** Connexion réussie : renvoie l'utilisateur connecté en JSON (200). */
    @Bean
    public AuthenticationSuccessHandler succesConnexion(ObjectMapper mapper) {
        return (request, response, authentication) -> {
            UtilisateurDetails u = (UtilisateurDetails) authentication.getPrincipal();
            List<String> roles = authentication.getAuthorities().stream()
                    .map(a -> a.getAuthority().replaceFirst("^ROLE_", "")).sorted().toList();
            ecrireJson(response, mapper, HttpStatus.OK, new MoiDto(u.getId(), u.getNomComplet(), roles));
        };
    }

    /** Échec : INVALID (message générique, sans préciser quel champ est fautif) ou DISABLED (compte désactivé). */
    @Bean
    public AuthenticationFailureHandler echecConnexion(ObjectMapper mapper) {
        return (request, response, exception) -> {
            boolean desactive = exception instanceof DisabledException;
            ecrireJson(response, mapper, HttpStatus.UNAUTHORIZED, Map.of(
                    "code", desactive ? "DISABLED" : "INVALID",
                    "message", desactive
                            ? "Ce compte a été désactivé — contactez la Vie étudiante"
                            : "Identifiants invalides"));
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, DaoAuthenticationProvider provider,
                                           AuthenticationSuccessHandler succesConnexion,
                                           AuthenticationFailureHandler echecConnexion,
                                           ObjectMapper mapper) throws Exception {
        CsrfTokenRequestAttributeHandler gestionnaireCsrf = new CsrfTokenRequestAttributeHandler();
        gestionnaireCsrf.setCsrfRequestAttributeName(null); // jeton « simple » : la valeur du cookie est celle de l'en-tête

        http
            .authenticationProvider(provider)
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(gestionnaireCsrf)
                .ignoringRequestMatchers("/h2-console/**"))
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/csrf", "/api/me", "/api/login", "/api/logout", "/error", "/h2-console/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/organisateur/**").hasRole("ORGANISATEUR")
                .requestMatchers("/api/**", "/uploads/**").authenticated()
                // L'application React (index.html, scripts, styles, images) est publique : c'est l'API qui est protégée.
                .anyRequest().permitAll())
            .exceptionHandling(e -> e
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                .accessDeniedHandler((request, response, ex) ->
                        ecrireJson(response, mapper, HttpStatus.FORBIDDEN,
                                Map.of("message", "Accès refusé.", "errors", Map.of()))))
            .formLogin(form -> form
                .loginPage("/login") // page React ; la page de connexion générique de Spring n'est pas générée
                .loginProcessingUrl("/api/login")
                .usernameParameter("courriel")
                .passwordParameter("motDePasse")
                .successHandler(succesConnexion)
                .failureHandler(echecConnexion)
                .permitAll())
            .logout(logout -> logout
                .logoutUrl("/api/logout")
                .logoutSuccessHandler((request, response, authentication) -> response.setStatus(HttpStatus.NO_CONTENT.value()))
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID"));
        return http.build();
    }
}

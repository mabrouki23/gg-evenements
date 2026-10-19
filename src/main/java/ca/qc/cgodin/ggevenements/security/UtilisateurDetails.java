package ca.qc.cgodin.ggevenements.security;

import ca.qc.cgodin.ggevenements.model.Utilisateur;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Principal Spring Security. isEnabled() reste vrai volontairement : l'état « désactivé » est vérifié
 * APRÈS la validation du mot de passe (voir SecurityConfig), pour ne jamais révéler l'état d'un compte
 * à quelqu'un qui ne connaît pas le bon mot de passe.
 */
public class UtilisateurDetails implements UserDetails {

    private static final long serialVersionUID = 1L;

    private final Integer id;
    private final String prenom;
    private final String nom;
    private final String courriel;
    private final String motDePasse;
    private final boolean actif;
    private final List<SimpleGrantedAuthority> autorites;

    public UtilisateurDetails(Utilisateur u) {
        this.id = u.getId();
        this.prenom = u.getPrenom();
        this.nom = u.getNom();
        this.courriel = u.getCourriel();
        this.motDePasse = u.getMotDePasse();
        this.actif = u.isActif();
        this.autorites = u.getRoles().stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r.getNom())).toList();
    }

    public Integer getId() { return id; }
    public String getNomComplet() { return prenom + " " + nom; }
    public boolean isActif() { return actif; }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return autorites; }
    @Override public String getPassword() { return motDePasse; }
    @Override public String getUsername() { return courriel; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}

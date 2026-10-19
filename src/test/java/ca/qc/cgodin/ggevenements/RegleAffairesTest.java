package ca.qc.cgodin.ggevenements;

import ca.qc.cgodin.ggevenements.config.DonneesInitiales;
import ca.qc.cgodin.ggevenements.dto.EvenementForm;
import ca.qc.cgodin.ggevenements.dto.FiltreEvenements;
import ca.qc.cgodin.ggevenements.exception.RegleAffairesException;
import ca.qc.cgodin.ggevenements.model.*;
import ca.qc.cgodin.ggevenements.repository.*;
import ca.qc.cgodin.ggevenements.service.CategorieService;
import ca.qc.cgodin.ggevenements.service.EvenementService;
import ca.qc.cgodin.ggevenements.service.InscriptionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RegleAffairesTest {

    @Autowired private InscriptionService inscriptionService;
    @Autowired private EvenementService evenementService;
    @Autowired private CategorieService categorieService;
    @Autowired private UtilisateurRepository utilisateurRepository;
    @Autowired private ClubRepository clubRepository;
    @Autowired private CategorieRepository categorieRepository;
    @Autowired private LieuRepository lieuRepository;
    @Autowired private EvenementRepository evenementRepository;
    @Autowired private InscriptionRepository inscriptionRepository;
    @Autowired private RoleRepository roleRepository;

    private Utilisateur u(String courriel) {
        return utilisateurRepository.findByCourrielIgnoreCase(courriel).orElseThrow();
    }

    /** Crée un événement à venir du club de l'organisateur 1, avec la capacité demandée. */
    private Evenement nouvelEvenement(int capacite) {
        Utilisateur org = u(DonneesInitiales.EMAIL_ORG1);
        Club club = clubRepository.findByResponsableIdOrderByNomAsc(org.getId()).get(0);
        Evenement e = new Evenement("Test " + UUID.randomUUID(), "Description", LocalDateTime.now().plusDays(10),
                LocalDateTime.now().plusDays(10).plusHours(2), capacite,
                categorieRepository.findAll().get(0), lieuRepository.findAll().get(0), club, org);
        return evenementRepository.save(e);
    }

    @Test
    void capaciteEtListeDAttente() {
        Evenement e = nouvelEvenement(1);
        Utilisateur a = u(DonneesInitiales.EMAIL_ETU1);
        Utilisateur b = u(DonneesInitiales.EMAIL_ETU2);
        assertEquals(StatutInscription.CONFIRMEE, inscriptionService.sinscrire(e.getId(), a.getId()));
        assertEquals(StatutInscription.EN_ATTENTE, inscriptionService.sinscrire(e.getId(), b.getId()));
        assertEquals(1, inscriptionRepository.countByEvenementIdAndStatut(e.getId(), StatutInscription.CONFIRMEE));
    }

    @Test
    void doubleInscriptionEstRefuseParLaCoucheService() {
        Evenement e = nouvelEvenement(5);
        Utilisateur a = u(DonneesInitiales.EMAIL_ETU1);
        inscriptionService.sinscrire(e.getId(), a.getId());
        assertThrows(RegleAffairesException.class, () -> inscriptionService.sinscrire(e.getId(), a.getId()));
    }

    @Test
    void desinscriptionPromeutLaPlusAncienneInscriptionEnAttente() {
        Evenement e = nouvelEvenement(1);
        Utilisateur a = u(DonneesInitiales.EMAIL_ETU1);
        Utilisateur b = u(DonneesInitiales.EMAIL_ETU2);
        Utilisateur c = u(DonneesInitiales.EMAIL_ETU3);
        inscriptionService.sinscrire(e.getId(), a.getId());   // confirmé
        inscriptionService.sinscrire(e.getId(), b.getId());   // attente (plus ancien)
        inscriptionService.sinscrire(e.getId(), c.getId());   // attente
        inscriptionService.seDesinscrire(e.getId(), a.getId());

        assertTrue(inscriptionRepository.findByUtilisateurIdAndEvenementId(a.getId(), e.getId()).isEmpty());
        assertEquals(StatutInscription.CONFIRMEE,
                inscriptionRepository.findByUtilisateurIdAndEvenementId(b.getId(), e.getId()).orElseThrow().getStatut());
        assertEquals(StatutInscription.EN_ATTENTE,
                inscriptionRepository.findByUtilisateurIdAndEvenementId(c.getId(), e.getId()).orElseThrow().getStatut());
    }

    @Test
    void desinscriptionDUneListeDAttenteNePromeutPersonne() {
        Evenement e = nouvelEvenement(1);
        Utilisateur a = u(DonneesInitiales.EMAIL_ETU1);
        Utilisateur b = u(DonneesInitiales.EMAIL_ETU2);
        inscriptionService.sinscrire(e.getId(), a.getId());
        inscriptionService.sinscrire(e.getId(), b.getId());
        inscriptionService.seDesinscrire(e.getId(), b.getId());
        assertEquals(StatutInscription.CONFIRMEE,
                inscriptionRepository.findByUtilisateurIdAndEvenementId(a.getId(), e.getId()).orElseThrow().getStatut());
    }

    @Test
    void unOrganisateurNePeutPasGererLesInscriptionsDUnAutreClub() {
        Evenement e = nouvelEvenement(5); // club de l'organisateur 1
        Utilisateur autre = u(DonneesInitiales.EMAIL_ORG2);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> inscriptionService.inscriptionsPourOrganisateur(e.getId(), autre.getId()));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        // Idem pour la modification et l'annulation
        assertThrows(ResponseStatusException.class, () -> evenementService.annuler(e.getId(), autre.getId()));
        assertThrows(ResponseStatusException.class, () -> evenementService.pourModification(e.getId(), autre.getId()));
    }

    @Test
    void capaciteNePeutPasEtreReduiteSousLesInscriptionsConfirmees() {
        Evenement e = nouvelEvenement(3);
        Utilisateur org = u(DonneesInitiales.EMAIL_ORG1);
        inscriptionService.sinscrire(e.getId(), u(DonneesInitiales.EMAIL_ETU1).getId());
        inscriptionService.sinscrire(e.getId(), u(DonneesInitiales.EMAIL_ETU2).getId());

        EvenementForm f = EvenementForm.depuis(evenementRepository.findById(e.getId()).orElseThrow());
        f.setCapaciteMax(1);
        RegleAffairesException ex = assertThrows(RegleAffairesException.class,
                () -> evenementService.modifier(e.getId(), f, org.getId()));
        assertEquals("capaciteMax", ex.getChamp());
    }

    @Test
    void augmenterLaCapacitePromeutLaListeDAttente() {
        Evenement e = nouvelEvenement(1);
        Utilisateur org = u(DonneesInitiales.EMAIL_ORG1);
        Utilisateur a = u(DonneesInitiales.EMAIL_ETU1);
        Utilisateur b = u(DonneesInitiales.EMAIL_ETU2);
        inscriptionService.sinscrire(e.getId(), a.getId());
        inscriptionService.sinscrire(e.getId(), b.getId());

        EvenementForm f = EvenementForm.depuis(evenementRepository.findById(e.getId()).orElseThrow());
        f.setCapaciteMax(2);
        evenementService.modifier(e.getId(), f, org.getId());
        assertEquals(StatutInscription.CONFIRMEE,
                inscriptionRepository.findByUtilisateurIdAndEvenementId(b.getId(), e.getId()).orElseThrow().getStatut());
    }

    @Test
    void annulerNeSupprimePasLEvenement() {
        Evenement e = nouvelEvenement(5);
        Utilisateur org = u(DonneesInitiales.EMAIL_ORG1);
        inscriptionService.sinscrire(e.getId(), u(DonneesInitiales.EMAIL_ETU1).getId());
        evenementService.annuler(e.getId(), org.getId());
        Evenement apres = evenementRepository.findById(e.getId()).orElseThrow();
        assertEquals(StatutEvenement.ANNULE, apres.getStatut());
        assertEquals(1, inscriptionRepository.countByEvenementIdAndStatut(e.getId(), StatutInscription.CONFIRMEE));
        // Une inscription est refusée sur un événement annulé
        assertThrows(RegleAffairesException.class,
                () -> inscriptionService.sinscrire(e.getId(), u(DonneesInitiales.EMAIL_ETU2).getId()));
    }

    @Test
    void rechercheEtTriSontExecutesParLaBaseDeDonnees() {
        Page<Evenement> premiere = evenementService.aVenir(new FiltreEvenements(), "date", 0);
        assertTrue(premiere.getContent().size() <= 9);
        assertTrue(premiere.getContent().stream().allMatch(ev -> ev.getStatut() == StatutEvenement.PLANIFIE));
        // Les trois tris fonctionnent
        assertNotNull(evenementService.aVenir(new FiltreEvenements(), "titre", 0));
        Page<Evenement> parPlaces = evenementService.aVenir(new FiltreEvenements(), "places", 0);
        for (int i = 1; i < parPlaces.getContent().size(); i++) {
            assertTrue(parPlaces.getContent().get(i - 1).getPlacesRestantes() <= parPlaces.getContent().get(i).getPlacesRestantes());
        }
        // Recherche par titre (insensible à la casse) : aucun événement annulé n'apparaît
        Page<Evenement> karting = evenementService.aVenir(new FiltreEvenements("KARTING", null, null, null, null), "date", 0);
        assertEquals(0, karting.getTotalElements());
        Page<Evenement> robots = evenementService.aVenir(new FiltreEvenements("robots", null, null, null, null), "date", 0);
        assertTrue(robots.getTotalElements() >= 1);
    }

    @Test
    void uneCategorieUtiliseeNePeutPasEtreSupprimee() {
        Evenement e = nouvelEvenement(5);
        Integer categorieId = e.getCategorie().getId();
        assertThrows(RegleAffairesException.class, () -> categorieService.supprimer(categorieId));
    }

    /** Inscriptions simultanées : le verrou sur l'événement empêche tout dépassement de capacité. */
    @Test
    void inscriptionsSimultaneesNeDepassentJamaisLaCapacite() throws Exception {
        final int capacite = 3;
        final int candidats = 8;
        Evenement e = nouvelEvenement(capacite);
        Role role = roleRepository.findByNom(Role.PUBLIC).orElseThrow();
        List<Integer> ids = new ArrayList<>();
        for (int i = 0; i < candidats; i++) {
            Utilisateur nouveau = new Utilisateur("Test", "Concurrent" + i, "conc-" + UUID.randomUUID() + "@ggevenements.ca", "x");
            nouveau.getRoles().add(role);
            ids.add(utilisateurRepository.save(nouveau).getId());
        }
        ExecutorService pool = Executors.newFixedThreadPool(candidats);
        CountDownLatch depart = new CountDownLatch(1);
        List<Future<?>> taches = new ArrayList<>();
        for (Integer id : ids) {
            taches.add(pool.submit(() -> {
                depart.await();
                return inscriptionService.sinscrire(e.getId(), id);
            }));
        }
        depart.countDown();
        for (Future<?> t : taches) {
            t.get(30, TimeUnit.SECONDS);
        }
        pool.shutdown();

        assertEquals(capacite, inscriptionRepository.countByEvenementIdAndStatut(e.getId(), StatutInscription.CONFIRMEE));
        assertEquals(candidats - capacite, inscriptionRepository.countByEvenementIdAndStatut(e.getId(), StatutInscription.EN_ATTENTE));
    }
}

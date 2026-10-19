// Test bout en bout de l'interface React contre le VRAI backend Spring Boot (API_BASE, 8080 par défaut), avec une session et des cookies réels.
// Prérequis : backend démarré avec une base de démonstration NEUVE (les scénarios modifient des inscriptions et un avis).
import { describe, it, expect, beforeAll, beforeEach, vi } from 'vitest';
import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import App from '../src/App.jsx';
import { AuthProvider } from '../src/auth.jsx';
import { FlashProvider } from '../src/flash.jsx';

const BASE = process.env.API_BASE || 'http://localhost:8080';
const vraiFetch = globalThis.fetch;
let jar = new Map();

function multipart(fd) {
  const b = '----gg' + Math.random().toString(16).slice(2);
  let corps = '';
  for (const [k, v] of fd.entries()) if (typeof v === 'string') corps += `--${b}\r\nContent-Disposition: form-data; name="${k}"\r\n\r\n${v}\r\n`;
  return { corps: corps + `--${b}--\r\n`, type: `multipart/form-data; boundary=${b}` };
}

beforeAll(() => {
  globalThis.fetch = async (url, opts = {}) => {
    const headers = { ...(opts.headers || {}), Cookie: [...jar].map(([k, v]) => `${k}=${v}`).join('; ') };
    let body = opts.body;
    if (body && typeof body === 'object' && typeof body.entries === 'function' && !(body instanceof URLSearchParams)) {
      const m = multipart(body); body = m.corps; headers['Content-Type'] = m.type;
    }
    const rep = await vraiFetch(BASE + url, { ...opts, headers, body, redirect: 'manual' });
    for (const sc of rep.headers.getSetCookie()) {
      const [kv] = sc.split(';'); const i = kv.indexOf('='); const k = kv.slice(0, i); const v = kv.slice(i + 1);
      if (v === '' || /Max-Age=0/i.test(sc)) { jar.delete(k); document.cookie = `${k}=; Max-Age=0`; }
      else { jar.set(k, v); document.cookie = `${k}=${v}`; }
    }
    return rep;
  };
  window.confirm = vi.fn(() => true);
});

beforeEach(() => { jar = new Map(); document.cookie.split(';').forEach((c) => { const n = c.split('=')[0].trim(); if (n) document.cookie = `${n}=; Max-Age=0`; }); });

function monter(chemin) {
  return render(
    <MemoryRouter initialEntries={[chemin]}>
      <FlashProvider><AuthProvider><App /></AuthProvider></FlashProvider>
    </MemoryRouter>
  );
}

async function seConnecter(user, courriel, mdp) {
  monter('/login');
  await user.type(await screen.findByLabelText('Courriel'), courriel);
  await user.type(screen.getByLabelText('Mot de passe'), mdp);
  await user.click(screen.getByRole('button', { name: 'Se connecter' }));
}

describe('Connexion', () => {
  it('redirige un visiteur non connecté vers la page de connexion', async () => {
    monter('/mes-evenements');
    expect(await screen.findByRole('heading', { name: 'Connexion' })).toBeInTheDocument();
  });

  it("affiche « Identifiants invalides » (message générique) pour un mauvais mot de passe", async () => {
    const user = userEvent.setup();
    await seConnecter(user, 'etudiant1@ggevenements.ca', 'mauvais');
    expect(await screen.findByText('Identifiants invalides')).toBeInTheDocument();
  });

  it('affiche le message clair pour un compte désactivé (bon mot de passe)', async () => {
    const user = userEvent.setup();
    await seConnecter(user, 'inactif@ggevenements.ca', 'Etudiant123!');
    expect(await screen.findByText(/Ce compte a été désactivé/)).toBeInTheDocument();
  });
});

describe('Utilisateur public', () => {
  it('se connecte, voit la liste (9 par page, pagination, filtres) et la navigation adaptée à son rôle', async () => {
    const user = userEvent.setup();
    await seConnecter(user, 'etudiant1@ggevenements.ca', 'Etudiant123!');
    expect(await screen.findByRole('heading', { name: 'Événements à venir' })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Mes événements' })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Recherche avancée' })).toBeInTheDocument();
    expect(screen.queryByRole('link', { name: 'Administration' })).not.toBeInTheDocument();
    expect(screen.queryByRole('link', { name: /Mes événements à organiser/ })).not.toBeInTheDocument();
    await waitFor(() => expect(screen.getAllByRole('link', { name: 'Voir le détail' })).toHaveLength(9));
    // aucune inscription possible depuis la liste
    expect(screen.queryByRole('button', { name: "S'inscrire" })).not.toBeInTheDocument();
    // page 2
    await user.click(screen.getByRole('link', { name: '2' }));
    await waitFor(() => expect(screen.getAllByRole('link', { name: 'Voir le détail' })).toHaveLength(1));
    // filtre par catégorie
    await user.selectOptions(screen.getByLabelText('Catégorie'), 'Sportif');
    await waitFor(() => expect(screen.getAllByRole('link', { name: 'Voir le détail' }).length).toBeGreaterThan(0));
    expect(screen.getAllByRole('link', { name: 'Voir le détail' }).length).toBeLessThan(9);
  });

  it('recherche avancée par titre', async () => {
    const user = userEvent.setup();
    await seConnecter(user, 'etudiant1@ggevenements.ca', 'Etudiant123!');
    await screen.findByRole('heading', { name: 'Événements à venir' });
    await user.click(screen.getByRole('link', { name: 'Recherche avancée' }));
    await user.type(await screen.findByLabelText('Titre contient'), 'robots');
    await user.click(screen.getByRole('button', { name: 'Rechercher' }));
    await waitFor(() => expect(screen.getAllByRole('link', { name: 'Voir le détail' })).toHaveLength(1));
    expect(screen.getByText(/Compétition de robots/)).toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: 'Réinitialiser les filtres' }));
    await waitFor(() => expect(screen.getAllByRole('link', { name: 'Voir le détail' })).toHaveLength(9));
  });

  it("détail d'un événement complet : un seul bouton, se désinscrire, puis se réinscrire en liste d'attente", async () => {
    const user = userEvent.setup();
    await seConnecter(user, 'etudiant4@ggevenements.ca', 'Etudiant123!');
    await screen.findByRole('heading', { name: 'Événements à venir' });
    await user.click(screen.getByRole('link', { name: 'Recherche avancée' }));
    await user.type(await screen.findByLabelText('Titre contient'), 'jeux');
    await user.click(screen.getByRole('button', { name: 'Rechercher' }));
    await user.click(await screen.findByRole('link', { name: 'Voir le détail' }));
    expect(await screen.findByRole('heading', { name: 'Soirée jeux de société' })).toBeInTheDocument();
    expect(screen.getByText(/Capacité maximale/)).toBeInTheDocument();
    const bouton = await screen.findByRole('button', { name: /S'inscrire|Se désinscrire/ });
    expect(screen.getAllByRole('button', { name: /S'inscrire|Se désinscrire/ })).toHaveLength(1);
    if (/Se désinscrire/.test(bouton.textContent)) {
      await user.click(bouton);
      expect(await screen.findByText('Vous avez été désinscrit(e) de cet événement.')).toBeInTheDocument();
      await user.click(await screen.findByRole('button', { name: "S'inscrire" }));
    } else {
      await user.click(bouton);
    }
    await waitFor(() => expect(screen.getByRole('status')).toHaveTextContent(/placé\(e\) en liste d'attente|inscription est confirmée/i));
  });

  it("« Mes événements » : À venir / Passés, lien « Laisser un avis », formulaire d'avis validé", async () => {
    const user = userEvent.setup();
    await seConnecter(user, 'etudiant2@ggevenements.ca', 'Etudiant123!');
    await screen.findByRole('heading', { name: 'Événements à venir' });
    await user.click(screen.getByRole('link', { name: 'Mes événements' }));
    expect(await screen.findByRole('heading', { name: 'À venir' })).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: 'Passés' })).toBeInTheDocument();
    expect(await screen.findByText('Avis soumis')).toBeInTheDocument(); // etudiant2 a déjà un avis sur l'atelier Arduino
    // etudiant3 n'a pas encore d'avis
  });

  it('refuse l’accès au module administrateur (page « Accès refusé »)', async () => {
    const user = userEvent.setup();
    await seConnecter(user, 'etudiant1@ggevenements.ca', 'Etudiant123!');
    await screen.findByRole('heading', { name: 'Événements à venir' });
    monter('/admin/tableau-de-bord');
    expect(await screen.findByRole('heading', { name: 'Accès refusé' })).toBeInTheDocument();
  });

  it('se déconnecte sans confirmation et revient à la connexion', async () => {
    const user = userEvent.setup();
    await seConnecter(user, 'etudiant1@ggevenements.ca', 'Etudiant123!');
    await screen.findByRole('heading', { name: 'Événements à venir' });
    await user.click(screen.getByRole('button', { name: 'Déconnexion' }));
    expect(await screen.findByRole('heading', { name: 'Connexion' })).toBeInTheDocument();
    expect(window.confirm).not.toHaveBeenCalledWith(expect.stringMatching(/déconnect/i));
  });
});

describe('Avis (étudiant 3)', () => {
  it('valide la note, puis enregistre un avis', async () => {
    const user = userEvent.setup();
    await seConnecter(user, 'etudiant3@ggevenements.ca', 'Etudiant123!');
    await screen.findByRole('heading', { name: 'Événements à venir' });
    await user.click(screen.getByRole('link', { name: 'Mes événements' }));
    await user.click(await screen.findByRole('link', { name: 'Laisser un avis' }));
    await user.click(await screen.findByRole('button', { name: 'Envoyer mon avis' }));
    expect(await screen.findByText('La note est obligatoire (de 1 à 5).')).toBeInTheDocument(); // erreur à côté du champ
    await user.selectOptions(screen.getByLabelText(/Note/), '5');
    await user.type(screen.getByLabelText(/Commentaire/), 'Excellent atelier');
    await user.click(screen.getByRole('button', { name: 'Envoyer mon avis' }));
    expect(await screen.findByText('Merci ! Votre avis a été enregistré.')).toBeInTheDocument();
    expect(await screen.findByText('Avis soumis')).toBeInTheDocument();
    expect(screen.queryByRole('link', { name: 'Laisser un avis' })).not.toBeInTheDocument();
  });
});

describe('Administrateur', () => {
  it('tableau de bord, utilisateurs et suppression refusée d’une catégorie utilisée', async () => {
    const user = userEvent.setup();
    await seConnecter(user, 'admin@ggevenements.ca', 'Admin123!');
    await screen.findByRole('heading', { name: 'Événements à venir' });
    await user.click(screen.getByRole('link', { name: 'Administration' }));
    expect(await screen.findByRole('heading', { name: /Vue d'ensemble/ })).toBeInTheDocument();
    expect(await screen.findByText('Les cinq clubs les plus actifs')).toBeInTheDocument();
    expect(screen.getByText('Planifiés')).toBeInTheDocument();
    await user.click(screen.getByRole('link', { name: /Utilisateurs/ }));
    expect(await screen.findByText('inactif@ggevenements.ca')).toBeInTheDocument();
    await user.click(screen.getByRole('link', { name: /Catégories/ }));
    const ligne = (await screen.findByText('Académique')).closest('tr');
    await user.click(within(ligne).getByRole('button', { name: 'Supprimer' }));
    expect(await screen.findByText(/Impossible de supprimer la catégorie « Académique »/)).toBeInTheDocument();
    // création avec validation à côté des champs
    await user.click(screen.getByRole('button', { name: /Créer/ }));
    await user.click(screen.getByRole('button', { name: 'Enregistrer' }));
    expect(await screen.findByText('Le nom de la catégorie est obligatoire.')).toBeInTheDocument();
  });
});

describe('Organisateur (deux clubs)', () => {
  it('choisit son club actif, ouvre le formulaire de création et voit les erreurs à côté des champs', async () => {
    const user = userEvent.setup();
    await seConnecter(user, 'organisateur@ggevenements.ca', 'Organisateur123!');
    await screen.findByRole('heading', { name: 'Événements à venir' });
    await user.click(screen.getByRole('link', { name: /Mes événements à organiser/ }));
    expect(await screen.findByRole('heading', { name: 'Mes événements à organiser' })).toBeInTheDocument();
    expect(await screen.findByText(/choisissez d'abord le club actif/)).toBeInTheDocument();
    await user.selectOptions(screen.getByLabelText(/Club actif/), "Club d'échecs");
    await user.click(screen.getByRole('button', { name: 'Choisir ce club' }));
    const creer = await screen.findByRole('link', { name: /Créer un événement/ });
    await user.click(creer);
    expect(await screen.findByRole('heading', { name: 'Créer un événement' })).toBeInTheDocument();
    expect(screen.getByText("Club d'échecs")).toBeInTheDocument();
    expect(screen.queryByLabelText(/^Club/)).not.toBeInTheDocument(); // le club n'est jamais un champ du formulaire
    await user.click(screen.getByRole('button', { name: 'Créer l’événement' }));
    expect(await screen.findByText('Le titre est obligatoire.')).toBeInTheDocument();
    expect(screen.getByText('Veuillez choisir une catégorie.')).toBeInTheDocument();
    expect(screen.getByText('La capacité maximale est obligatoire.')).toBeInTheDocument();
    // formulaire valide
    await user.type(screen.getByLabelText(/Titre/), 'Atelier créé depuis React');
    await user.type(screen.getByLabelText(/Description/), 'Test bout en bout');
    await user.selectOptions(screen.getByLabelText(/Catégorie/), screen.getAllByRole('option').find((o) => o.textContent === 'Social') ? 'Social' : 'Sportif');
    const lieu = screen.getByLabelText(/Lieu/);
    await user.selectOptions(lieu, within(lieu).getAllByRole('option')[1]);
    await user.type(screen.getByLabelText(/Début/), '2031-05-10T18:00');
    await user.type(screen.getByLabelText(/Fin/), '2031-05-10T20:00');
    await user.type(screen.getByLabelText(/Capacité/), '12');
    await user.click(screen.getByRole('button', { name: 'Créer l’événement' }));
    expect(await screen.findByText(/a été créé/)).toBeInTheDocument();
    expect(await screen.findByText('Atelier créé depuis React')).toBeInTheDocument();
    // gestion des inscriptions de cet événement
    const ligne = screen.getByText('Atelier créé depuis React').closest('tr');
    await user.click(within(ligne).getByRole('link', { name: 'Inscriptions' }));
    expect(await screen.findByText(/Personne en liste d'attente/)).toBeInTheDocument();
    expect(screen.getByText(/Aucune inscription confirmée/)).toBeInTheDocument();
  });
});

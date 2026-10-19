import { useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../../api.js';
import { useFlash } from '../../flash.jsx';
import { useChargement } from '../../hooks.js';
import { BadgeStatut, Chargement, ErreurPage } from '../../components/Commun.jsx';
import { dateHeure, estOuvert } from '../../utils.js';

/** Mes événements à organiser : les événements de MES clubs seulement (le serveur filtre et contrôle). */
export default function OrganisateurEvenements() {
  const clubs = useChargement(() => api.get('/api/organisateur/clubs'), 'clubs');
  const evts = useChargement(() => api.get('/api/organisateur/evenements'), 'evts');
  const flash = useFlash();
  const [choix, setChoix] = useState('');

  async function annuler(e) {
    if (!window.confirm(`Annuler « ${e.titre} » ? Il ne sera pas supprimé : l'historique des inscriptions est conservé.`)) return;
    try {
      await api.post(`/api/organisateur/evenements/${e.id}/annuler`);
      flash.ok("L'événement a été annulé. L'historique des inscriptions est conservé.");
    } catch (err) {
      flash.err(err.message);
    }
    evts.recharger();
  }

  async function choisirClub(ev) {
    ev.preventDefault();
    try {
      await api.post('/api/organisateur/club-actif', { id: Number(choix) });
      flash.ok('Club actif mis à jour.');
    } catch (err) {
      flash.err(err.message);
    }
    clubs.recharger();
  }

  if (clubs.erreur) return <ErreurPage erreur={clubs.erreur} />;
  if (evts.erreur) return <ErreurPage erreur={evts.erreur} />;
  if (!clubs.data || !evts.data) return <Chargement />;
  const { clubs: mesClubs, clubActifId, clubCreation, messageClub } = clubs.data;
  const actifs = mesClubs.filter((c) => c.actif);

  return (
    <>
      <h1>Mes événements à organiser</h1>

      {mesClubs.length === 0 && <div className="alerte err">Vous n'êtes responsable d'aucun club pour le moment. Contactez la Vie étudiante.</div>}

      {mesClubs.length > 1 && (
        <div className="carte-blanche">
          {/* Plusieurs clubs : choix du club actif (le club n'est JAMAIS un champ du formulaire de création) */}
          <form className="filtres" onSubmit={choisirClub}>
            <div className="champ">
              <label htmlFor="club-actif">Club actif pour la création d'événements</label>
              <select id="club-actif" value={choix || clubActifId || ''} onChange={(e) => setChoix(e.target.value)}>
                <option value="" disabled>— Choisir un club —</option>
                {actifs.map((c) => <option key={c.id} value={c.id}>{c.nom}</option>)}
              </select>
            </div>
            <div className="champ"><button type="submit" className="btn sec" disabled={!(choix || clubActifId)}>Choisir ce club</button></div>
          </form>
        </div>
      )}
      {mesClubs.length === 1 && (
        <p className="meta">Club : <strong>{mesClubs[0].nom}</strong> {!mesClubs[0].actif && <span className="badge b-gris">Club désactivé</span>}</p>
      )}
      {clubCreation && mesClubs.length > 1 && <p className="meta">Club actif : <strong>{clubCreation.nom}</strong></p>}

      {mesClubs.length > 0 && (
        <p>
          {clubCreation
            ? <Link className="btn" to="/organisateur/evenements/nouveau">＋ Créer un événement</Link>
            : <span className="meta">{messageClub}</span>}
        </p>
      )}

      {mesClubs.length > 0 && evts.data.length === 0 && <div className="vide carte-blanche">Vos clubs n'ont encore aucun événement.</div>}
      {evts.data.length > 0 && (
        <table>
          <thead><tr><th>Événement</th><th>Club</th><th>Début</th><th>Statut</th><th>Inscriptions</th><th>Actions</th></tr></thead>
          <tbody>
            {evts.data.map((e) => (
              <tr key={e.id}>
                <td><Link to={`/evenements/${e.id}`}><strong>{e.titre}</strong></Link><br /><span className="meta">{e.lieu.nom}</span></td>
                <td>{e.club.nom}</td>
                <td>{dateHeure(e.dateDebut)}</td>
                <td><BadgeStatut evenement={e} /></td>
                <td>{e.nbConfirmees} / {e.capaciteMax} confirmés<br /><span className="meta">{e.nbEnAttente} en attente</span></td>
                <td>
                  <div className="actions">
                    <Link className="btn sec petit" to={`/organisateur/evenements/${e.id}/inscriptions`}>Inscriptions</Link>
                    {estOuvert(e) && <Link className="btn sec petit" to={`/organisateur/evenements/${e.id}/modifier`}>Modifier</Link>}
                    {!e.annule && !e.termine && <button type="button" className="btn danger petit" onClick={() => annuler(e)}>Annuler l'événement</button>}
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </>
  );
}

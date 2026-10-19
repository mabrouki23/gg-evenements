import { Link } from 'react-router-dom';
import { api } from '../api.js';
import { useFlash } from '../flash.jsx';
import { useChargement } from '../hooks.js';
import { Chargement, ErreurPage } from '../components/Commun.jsx';
import { dateHeure } from '../utils.js';

function BadgeInscription({ ligne }) {
  if (ligne.evenement.annule) return <span className="badge b-rouge">Annulé</span>;
  return ligne.statut === 'CONFIRMEE'
    ? <span className="badge b-vert">Confirmé</span>
    : <span className="badge b-orange">En liste d'attente</span>;
}

/** « Mes événements » (fonctionnalité 3) : sous-listes À venir et Passés. */
export default function MesEvenements() {
  const { data, erreur, chargement, recharger } = useChargement(() => api.get('/api/mes-evenements'), 'mes');
  const flash = useFlash();

  async function seDesinscrire(ligne) {
    if (!window.confirm('Voulez-vous vraiment vous désinscrire de cet événement ?')) return;
    try {
      await api.del(`/api/evenements/${ligne.evenement.id}/inscription`);
      flash.ok('Vous avez été désinscrit(e) de cet événement.');
    } catch (e) {
      flash.err(e.message);
    }
    recharger();
  }

  if (erreur) return <ErreurPage erreur={erreur} />;
  if (chargement && !data) return <Chargement />;
  if (!data) return null;

  return (
    <>
      <h1>Mes événements</h1>

      <h2>À venir</h2>
      {data.aVenir.length === 0 ? (
        <div className="vide carte-blanche">Vous n'avez aucune inscription à venir.</div>
      ) : (
        <table>
          <thead><tr><th>Événement</th><th>Date</th><th>Statut</th><th>Actions</th></tr></thead>
          <tbody>
            {data.aVenir.map((l) => (
              <tr key={l.evenement.id}>
                <td><strong>{l.evenement.titre}</strong><br /><span className="meta">{l.evenement.club.nom}</span></td>
                <td>{dateHeure(l.evenement.dateDebut)}</td>
                <td><BadgeInscription ligne={l} /></td>
                <td>
                  <div className="actions">
                    <Link className="btn sec petit" to={`/evenements/${l.evenement.id}`}>Voir le détail</Link>
                    {l.peutSeDesinscrire && <button type="button" className="btn danger petit" onClick={() => seDesinscrire(l)}>Se désinscrire</button>}
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      <h2 style={{ marginTop: '2rem' }}>Passés</h2>
      {data.passes.length === 0 ? (
        <div className="vide carte-blanche">Aucun événement passé.</div>
      ) : (
        <table>
          <thead><tr><th>Événement</th><th>Date</th><th>Avis</th><th>Actions</th></tr></thead>
          <tbody>
            {data.passes.map((l) => (
              <tr key={l.evenement.id}>
                <td>
                  <strong>{l.evenement.titre}</strong><br /><span className="meta">{l.evenement.club.nom}</span>{' '}
                  {l.evenement.annule && <span className="badge b-rouge">Annulé</span>}
                </td>
                <td>{dateHeure(l.evenement.dateDebut)}</td>
                <td>
                  {l.avisDejaSoumis && <span className="badge b-vert">Avis soumis</span>}
                  {!l.avisDejaSoumis && l.statut === 'EN_ATTENTE' && <span className="meta">Non disponible (liste d'attente)</span>}
                  {!l.avisDejaSoumis && l.statut === 'CONFIRMEE' && !l.evenement.annule && <span className="meta">Aucun avis soumis</span>}
                </td>
                <td>
                  <div className="actions">
                    <Link className="btn sec petit" to={`/evenements/${l.evenement.id}`}>Voir le détail</Link>
                    {l.peutLaisserAvis && <Link className="btn petit" to={`/evenements/${l.evenement.id}/avis`}>Laisser un avis</Link>}
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

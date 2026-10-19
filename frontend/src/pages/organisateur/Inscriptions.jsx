import { Link, useParams } from 'react-router-dom';
import { api } from '../../api.js';
import { useFlash } from '../../flash.jsx';
import { useChargement } from '../../hooks.js';
import { Chargement, ErreurPage } from '../../components/Commun.jsx';
import { dateHeure } from '../../utils.js';

/** Gérer les inscriptions (fonctionnalité 12) : confirmés / liste d'attente, case « Présence confirmée ». */
export default function Inscriptions() {
  const { id } = useParams();
  const { data, erreur, recharger } = useChargement(() => api.get(`/api/organisateur/evenements/${id}/inscriptions`), id);
  const flash = useFlash();

  async function presence(inscription, presente) {
    try {
      await api.post(`/api/organisateur/inscriptions/${inscription.id}/presence`, { presente });
    } catch (e) {
      flash.err(e.message);
    }
    recharger();
  }

  if (erreur) return <ErreurPage erreur={erreur} />;
  if (!data) return <Chargement />;
  const e = data.evenement;

  return (
    <>
      <p><Link to="/organisateur/evenements">← Retour à mes événements à organiser</Link></p>
      <h1>Inscriptions — {e.titre}</h1>
      <p className="meta">{dateHeure(e.dateDebut)} · {data.confirmees.length} / {e.capaciteMax} confirmés</p>

      <h2><span className="badge b-vert">Confirmés</span> ({data.confirmees.length})</h2>
      {data.confirmees.length === 0 ? <div className="vide carte-blanche">Aucune inscription confirmée.</div> : (
        <table>
          <thead><tr><th>Nom</th><th>Courriel</th><th>Inscrit(e) le</th><th>Présence confirmée</th></tr></thead>
          <tbody>
            {data.confirmees.map((i) => (
              <tr key={i.id}>
                <td>{i.nom}</td>
                <td>{i.courriel}</td>
                <td>{dateHeure(i.dateInscription)}</td>
                <td>
                  <label style={{ display: 'inline-flex', gap: '.4rem', alignItems: 'center', fontWeight: 'normal', margin: 0 }}>
                    <input type="checkbox" checked={i.presenceConfirmee} onChange={(ev) => presence(i, ev.target.checked)} /> Présent(e)
                  </label>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      <h2 style={{ marginTop: '2rem' }}><span className="badge b-orange">Liste d'attente</span> ({data.enAttente.length})</h2>
      {data.enAttente.length === 0 ? <div className="vide carte-blanche">Personne en liste d'attente.</div> : (
        <table>
          <thead><tr><th>Rang</th><th>Nom</th><th>Courriel</th><th>Inscrit(e) le</th></tr></thead>
          <tbody>
            {data.enAttente.map((i, n) => (
              <tr key={i.id}><td>{n + 1}</td><td>{i.nom}</td><td>{i.courriel}</td><td>{dateHeure(i.dateInscription)}</td></tr>
            ))}
          </tbody>
        </table>
      )}
    </>
  );
}

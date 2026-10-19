import { Link } from 'react-router-dom';
import { api } from '../../api.js';
import { useFlash } from '../../flash.jsx';
import { useChargement } from '../../hooks.js';
import MenuAdmin from '../../components/MenuAdmin.jsx';
import { Chargement, ErreurPage } from '../../components/Commun.jsx';

/** Gestion des clubs (fonctionnalité 14) : liste, activation / désactivation. */
export default function Clubs() {
  const { data, erreur, recharger } = useChargement(() => api.get('/api/admin/clubs'), 'clubs');
  const flash = useFlash();

  async function basculer(c) {
    try {
      const r = await api.post(`/api/admin/clubs/${c.id}/actif`);
      flash.ok(`Le club « ${r.nom} » a été ${r.actif ? 'activé' : 'désactivé'}.`);
    } catch (e) {
      flash.err(e.message);
    }
    recharger();
  }

  if (erreur) return <ErreurPage erreur={erreur} />;
  if (!data) return <Chargement />;
  return (
    <>
      <h1>Administration — Clubs</h1>
      <MenuAdmin />
      <p><Link className="btn" to="/admin/clubs/nouveau">＋ Créer un club</Link></p>
      <table>
        <thead><tr><th>Nom</th><th>Contact</th><th>Responsable</th><th>Statut</th><th>Actions</th></tr></thead>
        <tbody>
          {data.map((c) => (
            <tr key={c.id}>
              <td><Link to={`/clubs/${c.id}`}>{c.nom}</Link></td>
              <td>{c.courrielContact}</td>
              <td>{c.responsableNom}</td>
              <td>{c.actif ? <span className="badge b-vert">Actif</span> : <span className="badge b-rouge">Inactif</span>}</td>
              <td>
                <div className="actions">
                  <Link className="btn sec petit" to={`/admin/clubs/${c.id}/modifier`}>Modifier</Link>
                  <button type="button" className={`btn petit ${c.actif ? 'danger' : 'sec'}`} onClick={() => basculer(c)}>{c.actif ? 'Désactiver' : 'Activer'}</button>
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </>
  );
}

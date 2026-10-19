import { api } from '../../api.js';
import { useChargement } from '../../hooks.js';
import MenuAdmin from '../../components/MenuAdmin.jsx';
import { Chargement, ErreurPage } from '../../components/Commun.jsx';
import { nombre } from '../../utils.js';

/** Vue d'ensemble (fonctionnalité 16) : tous les chiffres sont calculés par la base de données (COUNT, AVG, GROUP BY). */
export default function Dashboard() {
  const { data, erreur } = useChargement(() => api.get('/api/admin/stats'), 'stats');
  if (erreur) return <ErreurPage erreur={erreur} />;
  if (!data) return <Chargement />;
  return (
    <>
      <h1>Administration — Vue d'ensemble</h1>
      <MenuAdmin />

      <h2>Événements par statut</h2>
      <div className="kpi">
        <div className="carte-blanche"><div className="valeur">{data.planifies}</div>Planifiés</div>
        <div className="carte-blanche"><div className="valeur">{data.annules}</div>Annulés</div>
        <div className="carte-blanche"><div className="valeur">{data.termines}</div>Terminés</div>
      </div>

      <div className="kpi">
        <div className="carte-blanche">
          <div className="valeur">{data.tauxRemplissage != null ? `${nombre(data.tauxRemplissage, 1)} %` : '—'}</div>
          Taux moyen de remplissage<br /><span className="meta">(événements terminés)</span>
        </div>
        <div className="carte-blanche">
          <div className="valeur">{data.noteMoyenne != null ? `${nombre(data.noteMoyenne, 2)} / 5` : '—'}</div>
          Note moyenne générale<br /><span className="meta">({data.nbAvis} avis)</span>
        </div>
      </div>

      <h2>Les cinq clubs les plus actifs</h2>
      {data.clubsActifs.length === 0 ? <div className="vide carte-blanche">Aucune donnée.</div> : (
        <table>
          <thead><tr><th>#</th><th>Club</th><th>Événements organisés</th></tr></thead>
          <tbody>
            {data.clubsActifs.map((c, i) => <tr key={c.nom}><td>{i + 1}</td><td>{c.nom}</td><td>{c.nbEvenements}</td></tr>)}
          </tbody>
        </table>
      )}
    </>
  );
}

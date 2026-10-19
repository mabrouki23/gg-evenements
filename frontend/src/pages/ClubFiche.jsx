import { Link, useParams } from 'react-router-dom';
import { api } from '../api.js';
import { useChargement } from '../hooks.js';
import { Chargement, ErreurPage } from '../components/Commun.jsx';
import { dateHeure } from '../utils.js';

/** Fiche publique d'un club (lien depuis le nom du club dans le détail d'un événement). */
export default function ClubFiche() {
  const { id } = useParams();
  const { data, erreur, chargement } = useChargement(() => api.get(`/api/clubs/${id}`), id);
  if (erreur) return <ErreurPage erreur={erreur} />;
  if (chargement && !data) return <Chargement />;
  if (!data) return null;
  const { club, evenements } = data;
  return (
    <div style={{ maxWidth: 800, margin: '0 auto' }}>
      <p><Link to="/evenements">← Retour à la liste des événements</Link></p>
      <div className="carte-blanche">
        <h1>{club.nom}</h1>
        {!club.actif && <span className="badge b-gris">Club inactif</span>}
        {club.description && <p style={{ whiteSpace: 'pre-line' }}>{club.description}</p>}
        <p>✉️ Contact : <a href={`mailto:${club.courrielContact}`}>{club.courrielContact}</a></p>
        <p>👤 Responsable : {club.responsableNom}</p>
      </div>
      <h2>Événements à venir</h2>
      {evenements.length === 0 && <div className="vide carte-blanche">Aucun événement à venir pour ce club.</div>}
      {evenements.map((e) => (
        <div className="carte-blanche" key={e.id}>
          <Link to={`/evenements/${e.id}`}><strong>{e.titre}</strong></Link>
          <span className="meta"> — {dateHeure(e.dateDebut)}</span>
        </div>
      ))}
    </div>
  );
}

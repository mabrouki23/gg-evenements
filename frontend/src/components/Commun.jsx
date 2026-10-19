import { Link } from 'react-router-dom';
import { imageDe, dateHeure } from '../utils.js';

export function Chargement() {
  return <p className="vide">Chargement…</p>;
}

/** Erreur de chargement d'une page : message de l'API (404, 403, ...) et lien de retour. */
export function ErreurPage({ erreur }) {
  const statut = erreur && erreur.status;
  const titre = statut === 404 ? 'Introuvable' : statut === 403 ? 'Accès refusé' : 'Une erreur est survenue';
  return (
    <div className="carte-blanche vide" role="alert">
      <h1>{statut ? `${statut} — ${titre}` : titre}</h1>
      <p>{erreur && erreur.message}</p>
      <Link className="btn" to="/evenements">← Retour aux événements</Link>
    </div>
  );
}

/** Champ de formulaire avec son message d'erreur affiché à côté (jamais un message générique en haut de page). */
export function Champ({ id, label, erreur, aide, children }) {
  return (
    <div className="champ">
      <label htmlFor={id}>{label}</label>
      {children}
      {aide && <div className="aide">{aide}</div>}
      {erreur && <div className="erreur" role="alert">{erreur}</div>}
    </div>
  );
}

/** Indicateur de places : badge (« N places restantes » / « Liste d'attente » / « Complet ») + barre de remplissage. */
export function Places({ evenement: e }) {
  const pct = e.capaciteMax > 0 ? Math.min(100, Math.round((e.nbConfirmees * 100) / e.capaciteMax)) : 0;
  let badge;
  if (e.placesRestantes > 0) {
    badge = (
      <span className="badge b-vert">
        {e.placesRestantes} {e.placesRestantes === 1 ? 'place restante' : 'places restantes'}
      </span>
    );
  } else if (e.nbEnAttente > 0) {
    badge = <span className="badge b-orange">Liste d'attente</span>;
  } else {
    badge = <span className="badge b-rouge">Complet</span>;
  }
  return (
    <div>
      {badge}
      <div className={`barre ${e.placesRestantes <= 0 ? 'pleine' : ''}`} style={{ marginTop: '.3rem' }}>
        <span style={{ width: `${pct}%` }} />
      </div>
    </div>
  );
}

export function Affiche({ evenement, className }) {
  return <img className={className} src={imageDe(evenement)} alt={`Affiche : ${evenement.titre}`} />;
}

export function BadgeStatut({ evenement: e }) {
  if (e.annule) return <span className="badge b-rouge">Annulé</span>;
  if (e.termine) return <span className="badge b-gris">Terminé</span>;
  return <span className="badge b-bleu">Planifié</span>;
}

/** Carte d'un événement dans la liste : seule action = « Voir le détail » (aucune inscription directe). */
export function CarteEvenement({ evenement: e }) {
  return (
    <article className="evt">
      <Affiche evenement={e} />
      <div className="corps">
        <h3>{e.titre}</h3>
        <div>
          <span className="pastille" style={{ background: e.categorie.couleur }} />
          <span>{e.categorie.nom}</span>
        </div>
        <div className="meta">🏛️ {e.club.nom}</div>
        <div className="meta">📍 {e.lieu.nom}</div>
        <div className="meta">🕒 {dateHeure(e.dateDebut)}</div>
        <Places evenement={e} />
        <div style={{ marginTop: 'auto', paddingTop: '.6rem' }}>
          <Link className="btn" to={`/evenements/${e.id}`}>Voir le détail</Link>
        </div>
      </div>
    </article>
  );
}

export function Pagination({ page, totalPages, onChange }) {
  if (totalPages <= 1) return null;
  const pages = Array.from({ length: totalPages }, (_, i) => i);
  return (
    <nav className="pagination" aria-label="Pagination">
      {page > 0 && <a href="#precedent" onClick={(ev) => { ev.preventDefault(); onChange(page - 1); }}>‹ Précédent</a>}
      {pages.map((i) =>
        i === page ? (
          <span key={i} className="courante" aria-current="page">{i + 1}</span>
        ) : (
          <a key={i} href={`#page-${i + 1}`} onClick={(ev) => { ev.preventDefault(); onChange(i); }}>{i + 1}</a>
        )
      )}
      {page < totalPages - 1 && <a href="#suivant" onClick={(ev) => { ev.preventDefault(); onChange(page + 1); }}>Suivant ›</a>}
    </nav>
  );
}

import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { api } from '../api.js';
import { useChargement } from '../hooks.js';
import { CarteEvenement, Chargement, ErreurPage, Pagination } from '../components/Commun.jsx';

/**
 * Liste des événements à venir (fonctionnalité 2) et recherche avancée (fonctionnalité 7) :
 * même résultat, mêmes cartes ; la recherche ajoute le texte du titre et la plage de dates.
 * Les critères vivent dans l'adresse (?categorie=1&club=2&tri=places&page=1), donc la page est partageable.
 */
export default function Evenements({ avance = false }) {
  const [params, setParams] = useSearchParams();
  const get = (k) => params.get(k) || '';
  const [brouillon, setBrouillon] = useState({ q: get('q'), du: get('du'), au: get('au') });
  const ref = useChargement(() => api.get('/api/reference'), 'reference');
  const cle = params.toString() + (avance ? '|avance' : '');
  const liste = useChargement(
    () =>
      api.get('/api/evenements', {
        q: avance ? get('q') : undefined,
        du: avance ? get('du') : undefined,
        au: avance ? get('au') : undefined,
        categorie: get('categorie'),
        club: get('club'),
        tri: get('tri') || 'date',
        page: get('page') || 0,
      }),
    cle
  );

  useEffect(() => {
    setBrouillon({ q: get('q'), du: get('du'), au: get('au') });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [params]);

  function maj(changements) {
    const suivant = new URLSearchParams(params);
    Object.entries(changements).forEach(([k, v]) => (v === '' || v == null ? suivant.delete(k) : suivant.set(k, v)));
    if (!('page' in changements)) suivant.delete('page'); // un changement de critère revient à la 1re page
    setParams(suivant);
  }

  function appliquer(ev) {
    ev.preventDefault();
    maj({ q: brouillon.q.trim(), du: brouillon.du, au: brouillon.au });
  }

  const categories = (ref.data && ref.data.categories) || [];
  const clubs = (ref.data && ref.data.clubs) || [];

  return (
    <>
      <h1>{avance ? 'Recherche avancée' : 'Événements à venir'}</h1>

      <form className="filtres carte-blanche" onSubmit={appliquer}>
        {avance && (
          <div className="champ" style={{ flex: 2, minWidth: 220 }}>
            <label htmlFor="q">Titre contient</label>
            <input id="q" type="text" value={brouillon.q} onChange={(e) => setBrouillon({ ...brouillon, q: e.target.value })} placeholder="Ex. : tournoi, soirée…" />
          </div>
        )}
        <div className="champ">
          <label htmlFor="categorie">Catégorie</label>
          <select id="categorie" value={get('categorie')} onChange={(e) => maj({ categorie: e.target.value })}>
            <option value="">Toutes</option>
            {categories.map((c) => <option key={c.id} value={c.id}>{c.nom}</option>)}
          </select>
        </div>
        <div className="champ">
          <label htmlFor="club">Club organisateur</label>
          <select id="club" value={get('club')} onChange={(e) => maj({ club: e.target.value })}>
            <option value="">Tous</option>
            {clubs.map((c) => <option key={c.id} value={c.id}>{c.nom}</option>)}
          </select>
        </div>
        {avance && (
          <>
            <div className="champ">
              <label htmlFor="du">Du</label>
              <input id="du" type="date" value={brouillon.du} onChange={(e) => setBrouillon({ ...brouillon, du: e.target.value })} />
            </div>
            <div className="champ">
              <label htmlFor="au">Au</label>
              <input id="au" type="date" value={brouillon.au} onChange={(e) => setBrouillon({ ...brouillon, au: e.target.value })} />
            </div>
          </>
        )}
        <div className="champ">
          <label htmlFor="tri">Trier par</label>
          <select id="tri" value={get('tri') || 'date'} onChange={(e) => maj({ tri: e.target.value })}>
            <option value="date">Date de début</option>
            <option value="titre">Titre (A → Z)</option>
            <option value="places">Places restantes (croissant)</option>
          </select>
        </div>
        <div className="champ actions">
          {avance && <button type="submit" className="btn">Rechercher</button>}
          <button type="button" className="btn sec" onClick={() => setParams({})}>
            {avance ? 'Réinitialiser les filtres' : 'Réinitialiser'}
          </button>
        </div>
      </form>

      {liste.erreur && <ErreurPage erreur={liste.erreur} />}
      {liste.chargement && !liste.data && <Chargement />}
      {liste.data && (
        <>
          <p className="meta">
            {liste.data.totalElements} {liste.data.totalElements === 1 ? 'événement trouvé' : 'événements trouvés'}
          </p>
          {liste.data.contenu.length === 0 && <div className="vide carte-blanche">Aucun événement à venir ne correspond à ces critères.</div>}
          <div className="grille">
            {liste.data.contenu.map((e) => <CarteEvenement key={e.id} evenement={e} />)}
          </div>
          <Pagination page={liste.data.page} totalPages={liste.data.totalPages} onChange={(p) => maj({ page: p })} />
        </>
      )}
    </>
  );
}

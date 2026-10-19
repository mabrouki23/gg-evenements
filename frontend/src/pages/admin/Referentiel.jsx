import { useState } from 'react';
import { api } from '../../api.js';
import { useFlash } from '../../flash.jsx';
import { useChargement } from '../../hooks.js';
import MenuAdmin from '../../components/MenuAdmin.jsx';
import { Champ, Chargement, ErreurPage } from '../../components/Commun.jsx';

/**
 * CRUD complet (fonctionnalité 15) réutilisé pour les catégories et pour les lieux :
 * liste, création, modification et suppression. La suppression d'un élément déjà utilisé est refusée par
 * le serveur avec un message clair (affiché tel quel), jamais une erreur de contrainte de base de données.
 */
export default function Referentiel({ titre, nomSingulier, endpoint, champs, colonnes }) {
  const { data, erreur, recharger } = useChargement(() => api.get(endpoint), endpoint);
  const flash = useFlash();
  const vide = Object.fromEntries(champs.map((c) => [c.nom, c.defaut ?? '']));
  const [edition, setEdition] = useState(null); // null = fermé, 'nouveau' ou l'id en modification
  const [valeurs, setValeurs] = useState(vide);
  const [erreurs, setErreurs] = useState({});

  function ouvrir(ligne) {
    setErreurs({});
    if (ligne) {
      setEdition(ligne.id);
      setValeurs(Object.fromEntries(champs.map((c) => [c.nom, ligne[c.nom] ?? ''])));
    } else {
      setEdition('nouveau');
      setValeurs(vide);
    }
  }

  async function soumettre(ev) {
    ev.preventDefault();
    setErreurs({});
    const corps = { ...valeurs };
    champs.filter((c) => c.type === 'number').forEach((c) => { corps[c.nom] = valeurs[c.nom] === '' ? null : Number(valeurs[c.nom]); });
    try {
      if (edition === 'nouveau') await api.post(endpoint, corps);
      else await api.put(`${endpoint}/${edition}`, corps);
      flash.ok(`${nomSingulier[0].toUpperCase()}${nomSingulier.slice(1)} enregistré(e).`);
      setEdition(null);
      recharger();
    } catch (e) {
      if (Object.keys(e.errors).length > 0) setErreurs(e.errors);
      else flash.err(e.message);
    }
  }

  async function supprimer(ligne) {
    if (!window.confirm(`Supprimer cet élément (${nomSingulier}) ?`)) return;
    try {
      await api.del(`${endpoint}/${ligne.id}`);
      flash.ok(`${nomSingulier[0].toUpperCase()}${nomSingulier.slice(1)} supprimé(e).`);
    } catch (e) {
      flash.err(e.message); // ex. « Impossible de supprimer la catégorie ... : elle est utilisée par N événement(s). »
    }
    recharger();
  }

  if (erreur) return <ErreurPage erreur={erreur} />;
  if (!data) return <Chargement />;

  return (
    <>
      <h1>Administration — {titre}</h1>
      <MenuAdmin />

      {edition === null && <p><button type="button" className="btn" onClick={() => ouvrir(null)}>＋ Créer</button></p>}

      {edition !== null && (
        <div className="carte-blanche" style={{ maxWidth: 560 }}>
          <h2>{edition === 'nouveau' ? `Créer (${nomSingulier})` : `Modifier (${nomSingulier})`}</h2>
          <form onSubmit={soumettre} noValidate>
            {champs.map((c) => (
              <Champ key={c.nom} id={c.nom} label={c.label} erreur={erreurs[c.nom]}>
                <input id={c.nom} type={c.type} min={c.type === 'number' ? 1 : undefined} maxLength={c.maxLength}
                       value={valeurs[c.nom]} onChange={(ev) => setValeurs({ ...valeurs, [c.nom]: ev.target.value })} />
              </Champ>
            ))}
            <div className="actions">
              <button type="submit" className="btn">Enregistrer</button>
              <button type="button" className="btn sec" onClick={() => setEdition(null)}>Annuler</button>
            </div>
          </form>
        </div>
      )}

      <table>
        <thead><tr>{colonnes.map((c) => <th key={c.titre}>{c.titre}</th>)}<th>Actions</th></tr></thead>
        <tbody>
          {data.map((ligne) => (
            <tr key={ligne.id}>
              {colonnes.map((c) => <td key={c.titre}>{c.rendu(ligne)}</td>)}
              <td>
                <div className="actions">
                  <button type="button" className="btn sec petit" onClick={() => ouvrir(ligne)}>Modifier</button>
                  <button type="button" className="btn danger petit" onClick={() => supprimer(ligne)}>Supprimer</button>
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </>
  );
}

export function Categories() {
  return (
    <Referentiel
      titre="Catégories"
      nomSingulier="catégorie"
      endpoint="/api/admin/categories"
      champs={[
        { nom: 'nom', label: 'Nom *', type: 'text', maxLength: 50 },
        { nom: 'description', label: 'Description', type: 'text', maxLength: 150 },
        { nom: 'couleur', label: 'Couleur *', type: 'color', defaut: '#1F3864' },
      ]}
      colonnes={[
        { titre: 'Couleur', rendu: (c) => (<><span className="pastille" style={{ background: c.couleur, width: '1.2rem', height: '1.2rem' }} /> <span className="meta">{c.couleur}</span></>) },
        { titre: 'Nom', rendu: (c) => c.nom },
        { titre: 'Description', rendu: (c) => c.description },
      ]}
    />
  );
}

export function Lieux() {
  return (
    <Referentiel
      titre="Lieux"
      nomSingulier="lieu"
      endpoint="/api/admin/lieux"
      champs={[
        { nom: 'nom', label: 'Nom *', type: 'text', maxLength: 80 },
        { nom: 'campus', label: 'Campus *', type: 'text', maxLength: 50 },
        { nom: 'adresse', label: 'Adresse *', type: 'text', maxLength: 150 },
        { nom: 'capaciteMax', label: 'Capacité maximale *', type: 'number' },
      ]}
      colonnes={[
        { titre: 'Nom', rendu: (l) => l.nom },
        { titre: 'Campus', rendu: (l) => l.campus },
        { titre: 'Adresse', rendu: (l) => l.adresse },
        { titre: 'Capacité max.', rendu: (l) => l.capaciteMax },
      ]}
    />
  );
}

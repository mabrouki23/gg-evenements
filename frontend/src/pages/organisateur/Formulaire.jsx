import { useEffect, useRef, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { api } from '../../api.js';
import { useFlash } from '../../flash.jsx';
import { useChargement } from '../../hooks.js';
import { Champ, Chargement, ErreurPage } from '../../components/Commun.jsx';

const VIDE = { titre: '', description: '', categorieId: '', lieuId: '', dateDebut: '', dateFin: '', capaciteMax: '' };

/**
 * Création (fonctionnalité 10) et modification (fonctionnalité 11) d'un événement.
 * Le club n'apparaît JAMAIS comme champ : le serveur le déduit du compte connecté (et revérifie).
 * Les erreurs de validation renvoyées par l'API sont affichées à côté du champ concerné.
 */
export default function FormulaireEvenement() {
  const { id } = useParams(); // absent = création
  const edition = id !== undefined;
  const navigate = useNavigate();
  const flash = useFlash();
  const fichier = useRef(null);
  const [valeurs, setValeurs] = useState(VIDE);
  const [imageActuelle, setImageActuelle] = useState(null);
  const [erreurs, setErreurs] = useState({});
  const [bandeau, setBandeau] = useState(null);
  const [envoi, setEnvoi] = useState(false);

  const ref = useChargement(() => api.get('/api/reference'), 'reference');
  const club = useChargement(() => (edition ? Promise.resolve(null) : api.get('/api/organisateur/clubs')), 'club-' + id);
  const existant = useChargement(() => (edition ? api.get(`/api/organisateur/evenements/${id}/formulaire`) : Promise.resolve(null)), 'form-' + id);

  useEffect(() => {
    if (existant.data) {
      const f = existant.data;
      setValeurs({
        titre: f.titre, description: f.description, categorieId: f.categorieId, lieuId: f.lieuId,
        dateDebut: f.dateDebut, dateFin: f.dateFin, capaciteMax: f.capaciteMax,
      });
      setImageActuelle(f.imageActuelle);
    }
  }, [existant.data]);

  const maj = (champ) => (ev) => setValeurs({ ...valeurs, [champ]: ev.target.value });

  async function soumettre(ev) {
    ev.preventDefault();
    setErreurs({});
    setBandeau(null);
    setEnvoi(true);
    const fd = new FormData();
    Object.entries(valeurs).forEach(([k, v]) => { if (v !== '' && v != null) fd.append(k, v); });
    if (fichier.current && fichier.current.files[0]) fd.append('affiche', fichier.current.files[0]);
    try {
      if (edition) {
        await api.postForm(`/api/organisateur/evenements/${id}/modifier`, fd);
        flash.ok("L'événement a été modifié.", true);
      } else {
        const e = await api.postForm('/api/organisateur/evenements', fd);
        flash.ok(`L'événement « ${e.titre} » a été créé.`, true);
      }
      navigate('/organisateur/evenements');
    } catch (e) {
      if (Object.keys(e.errors).length > 0) setErreurs(e.errors);
      else setBandeau(e.message);
    } finally {
      setEnvoi(false);
    }
  }

  const erreurChargement = ref.erreur || club.erreur || existant.erreur;
  if (erreurChargement) return <ErreurPage erreur={erreurChargement} />;
  if (!ref.data || (edition ? !existant.data : !club.data)) return <Chargement />;
  if (!edition && !club.data.clubCreation) {
    return (
      <div className="carte-blanche vide">
        <p>{club.data.messageClub}</p>
        <Link className="btn" to="/organisateur/evenements">← Retour à mes événements à organiser</Link>
      </div>
    );
  }

  return (
    <div style={{ maxWidth: 760, margin: '0 auto' }}>
      <p><Link to="/organisateur/evenements">← Retour à mes événements à organiser</Link></p>
      <h1>{edition ? 'Modifier un événement' : 'Créer un événement'}</h1>
      {bandeau && <div className="alerte err" role="alert">{bandeau}</div>}

      <div className="carte-blanche">
        <p className="aide">
          Le club organisateur est déterminé automatiquement
          {!edition && <> : <strong>{club.data.clubCreation.nom}</strong></>} (il ne se choisit jamais dans ce formulaire).
          Les champs marqués * sont obligatoires.
        </p>
        <form onSubmit={soumettre} noValidate>
          <Champ id="titre" label="Titre *" erreur={erreurs.titre}>
            <input id="titre" type="text" maxLength={120} value={valeurs.titre} onChange={maj('titre')} placeholder="Ex. : Tournoi de basketball inter-programmes" title="Un titre court et accrocheur (120 caractères maximum)" />
          </Champ>
          <Champ id="description" label="Description *" erreur={erreurs.description}>
            <textarea id="description" rows={6} maxLength={1000} value={valeurs.description} onChange={maj('description')} placeholder="Décrivez l'événement : programme, public visé, ce qu'il faut apporter…" title="Description complète affichée sur la page de détail" />
          </Champ>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
            <Champ id="categorieId" label="Catégorie *" erreur={erreurs.categorieId}>
              <select id="categorieId" value={valeurs.categorieId} onChange={maj('categorieId')}>
                <option value="">— Choisir une catégorie —</option>
                {ref.data.categories.map((c) => <option key={c.id} value={c.id}>{c.nom}</option>)}
              </select>
            </Champ>
            <Champ id="lieuId" label="Lieu *" erreur={erreurs.lieuId}>
              <select id="lieuId" value={valeurs.lieuId} onChange={maj('lieuId')}>
                <option value="">— Choisir un lieu —</option>
                {ref.data.lieux.map((l) => <option key={l.id} value={l.id}>{l.nom} ({l.campus}, max {l.capaciteMax})</option>)}
              </select>
            </Champ>
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
            <Champ id="dateDebut" label="Début (date et heure) *" erreur={erreurs.dateDebut}>
              <input id="dateDebut" type="datetime-local" value={valeurs.dateDebut} onChange={maj('dateDebut')} />
            </Champ>
            <Champ id="dateFin" label="Fin (date et heure) *" erreur={erreurs.dateFin || erreurs.dateFinValide}>
              <input id="dateFin" type="datetime-local" value={valeurs.dateFin} onChange={maj('dateFin')} />
            </Champ>
          </div>
          <Champ id="capaciteMax" label="Capacité maximale *" erreur={erreurs.capaciteMax}
                 aide="Nombre maximal de personnes confirmées. Au-delà, les inscriptions vont en liste d'attente.">
            <input id="capaciteMax" type="number" min="1" value={valeurs.capaciteMax} onChange={maj('capaciteMax')} placeholder="Ex. : 50" />
          </Champ>
          <Champ id="affiche" label="Image d’affiche (facultatif)" erreur={erreurs.affiche}
                 aide="JPEG, PNG, GIF ou WebP — 5 Mo maximum. Sans image, une image par défaut est utilisée.">
            {imageActuelle && <img src={`/uploads/${imageActuelle}`} alt="Affiche actuelle" style={{ maxWidth: 220, borderRadius: 8, display: 'block', marginBottom: '.4rem' }} />}
            <input id="affiche" type="file" ref={fichier} accept="image/png,image/jpeg,image/gif,image/webp" />
          </Champ>
          <div className="actions">
            <button type="submit" className="btn" disabled={envoi}>{edition ? 'Enregistrer les modifications' : 'Créer l’événement'}</button>
            <Link className="btn sec" to="/organisateur/evenements">Retour</Link>
          </div>
        </form>
      </div>
    </div>
  );
}

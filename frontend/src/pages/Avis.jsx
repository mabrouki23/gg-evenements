import { useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { api } from '../api.js';
import { useFlash } from '../flash.jsx';
import { useChargement } from '../hooks.js';
import { Champ, Chargement, ErreurPage } from '../components/Commun.jsx';

/** Laisser un avis (fonctionnalité 8) : note de 1 à 5 obligatoire, commentaire facultatif de 500 caractères au plus. */
export default function Avis() {
  const { id } = useParams();
  const navigate = useNavigate();
  const flash = useFlash();
  const { data, erreur, chargement } = useChargement(() => api.get(`/api/evenements/${id}`), id);
  const [note, setNote] = useState('');
  const [commentaire, setCommentaire] = useState('');
  const [erreurs, setErreurs] = useState({});
  const [envoi, setEnvoi] = useState(false);

  async function soumettre(ev) {
    ev.preventDefault();
    setErreurs({});
    setEnvoi(true);
    try {
      await api.post(`/api/evenements/${id}/avis`, { note: note === '' ? null : Number(note), commentaire });
      flash.ok('Merci ! Votre avis a été enregistré.', true);
      navigate('/mes-evenements');
    } catch (e) {
      if (Object.keys(e.errors).length > 0) setErreurs(e.errors);
      else {
        flash.err(e.message, true); // règle d'affaires (ex. avis déjà soumis) : retour à « Mes événements »
        navigate('/mes-evenements');
      }
    } finally {
      setEnvoi(false);
    }
  }

  if (erreur) return <ErreurPage erreur={erreur} />;
  if (chargement && !data) return <Chargement />;
  if (!data) return null;

  return (
    <div style={{ maxWidth: 640, margin: '0 auto' }}>
      <p><Link to="/mes-evenements">← Retour à « Mes événements »</Link></p>
      <h1>Laisser un avis</h1>
      <div className="carte-blanche">
        <p>Événement : <strong>{data.evenement.titre}</strong></p>
        <form onSubmit={soumettre} noValidate>
          <Champ id="note" label="Note (de 1 à 5) *" erreur={erreurs.note}>
            <select id="note" value={note} onChange={(e) => setNote(e.target.value)}>
              <option value="">— Choisir —</option>
              {[1, 2, 3, 4, 5].map((n) => <option key={n} value={n}>{n} ★</option>)}
            </select>
          </Champ>
          <Champ id="commentaire" label="Commentaire (facultatif, 500 caractères maximum)" erreur={erreurs.commentaire}>
            <textarea id="commentaire" rows={5} maxLength={500} value={commentaire} onChange={(e) => setCommentaire(e.target.value)} placeholder="Qu'avez-vous pensé de l'événement ?" />
          </Champ>
          <button type="submit" className="btn" disabled={envoi}>Envoyer mon avis</button>
        </form>
      </div>
    </div>
  );
}

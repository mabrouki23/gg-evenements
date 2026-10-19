import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { api } from '../../api.js';
import { useFlash } from '../../flash.jsx';
import { useChargement } from '../../hooks.js';
import MenuAdmin from '../../components/MenuAdmin.jsx';
import { Champ, Chargement, ErreurPage } from '../../components/Commun.jsx';

const VIDE = { nom: '', description: '', courrielContact: '', responsableId: '' };

export default function ClubFormulaire() {
  const { id } = useParams();
  const edition = id !== undefined;
  const navigate = useNavigate();
  const flash = useFlash();
  const [valeurs, setValeurs] = useState(VIDE);
  const [erreurs, setErreurs] = useState({});
  const [bandeau, setBandeau] = useState(null);
  const users = useChargement(() => api.get('/api/admin/utilisateurs'), 'users');
  const club = useChargement(() => (edition ? api.get(`/api/admin/clubs/${id}`) : Promise.resolve(null)), 'club-' + id);

  useEffect(() => {
    if (club.data) {
      setValeurs({ nom: club.data.nom, description: club.data.description || '', courrielContact: club.data.courrielContact, responsableId: club.data.responsableId });
    }
  }, [club.data]);

  const maj = (c) => (ev) => setValeurs({ ...valeurs, [c]: ev.target.value });

  async function soumettre(ev) {
    ev.preventDefault();
    setErreurs({});
    setBandeau(null);
    const corps = { ...valeurs, responsableId: valeurs.responsableId === '' ? null : Number(valeurs.responsableId) };
    try {
      if (edition) {
        await api.put(`/api/admin/clubs/${id}`, corps);
        flash.ok('Le club a été modifié.', true);
      } else {
        const c = await api.post('/api/admin/clubs', corps);
        flash.ok(`Le club « ${c.nom} » a été créé. ${c.responsableNom} a reçu le rôle Organisateur.`, true);
      }
      navigate('/admin/clubs');
    } catch (e) {
      if (Object.keys(e.errors).length > 0) setErreurs(e.errors);
      else setBandeau(e.message);
    }
  }

  if (users.erreur || club.erreur) return <ErreurPage erreur={users.erreur || club.erreur} />;
  if (!users.data || (edition && !club.data)) return <Chargement />;
  const actifs = users.data.filter((u) => u.actif || String(u.id) === String(valeurs.responsableId));

  return (
    <div style={{ maxWidth: 680, margin: '0 auto' }}>
      <h1>{edition ? 'Modifier le club' : 'Créer un club'}</h1>
      <MenuAdmin />
      {bandeau && <div className="alerte err" role="alert">{bandeau}</div>}
      <div className="carte-blanche">
        <form onSubmit={soumettre} noValidate>
          <Champ id="nom" label="Nom *" erreur={erreurs.nom}>
            <input id="nom" type="text" maxLength={80} value={valeurs.nom} onChange={maj('nom')} />
          </Champ>
          <Champ id="description" label="Description" erreur={erreurs.description}>
            <textarea id="description" rows={4} maxLength={300} value={valeurs.description} onChange={maj('description')} />
          </Champ>
          <Champ id="courrielContact" label="Courriel de contact *" erreur={erreurs.courrielContact}>
            <input id="courrielContact" type="email" maxLength={100} value={valeurs.courrielContact} onChange={maj('courrielContact')} />
          </Champ>
          <Champ id="responsableId" label="Responsable *" erreur={erreurs.responsableId} aide="Le responsable reçoit automatiquement le rôle Organisateur.">
            <select id="responsableId" value={valeurs.responsableId} onChange={maj('responsableId')}>
              <option value="">— Choisir un utilisateur —</option>
              {actifs.map((u) => <option key={u.id} value={u.id}>{u.prenom} {u.nom} ({u.courriel})</option>)}
            </select>
          </Champ>
          <div className="actions">
            <button type="submit" className="btn">Enregistrer</button>
            <Link className="btn sec" to="/admin/clubs">Annuler</Link>
          </div>
        </form>
      </div>
    </div>
  );
}

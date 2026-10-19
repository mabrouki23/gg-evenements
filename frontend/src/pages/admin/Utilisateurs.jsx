import { api } from '../../api.js';
import { useAuth } from '../../auth.jsx';
import { useFlash } from '../../flash.jsx';
import { useChargement } from '../../hooks.js';
import MenuAdmin from '../../components/MenuAdmin.jsx';
import { Chargement, ErreurPage } from '../../components/Commun.jsx';

const LIBELLE_ROLE = { ADMIN: 'Administrateur', ORGANISATEUR: 'Organisateur', PUBLIC: 'Utilisateur' };

/** Gestion des utilisateurs (fonctionnalité 13) : désactiver / réactiver, attribuer / retirer le rôle Organisateur. */
export default function Utilisateurs() {
  const { user } = useAuth();
  const { data, erreur, recharger } = useChargement(() => api.get('/api/admin/utilisateurs'), 'users');
  const flash = useFlash();

  async function action(url, succes) {
    try {
      const u = await api.post(url);
      flash.ok(succes(u));
    } catch (e) {
      flash.err(e.message);
    }
    recharger();
  }

  const basculerActif = (u) => {
    if (u.actif && !window.confirm('Désactiver ce compte ? La personne ne pourra plus se connecter (son historique est conservé).')) return;
    action(`/api/admin/utilisateurs/${u.id}/actif`, (r) => `Le compte de ${r.prenom} ${r.nom} a été ${r.actif ? 'réactivé' : 'désactivé'}.`);
  };
  const basculerRole = (u) =>
    action(`/api/admin/utilisateurs/${u.id}/organisateur`, (r) =>
      `Le rôle Organisateur a été ${r.roles.includes('ORGANISATEUR') ? 'attribué à' : 'retiré à'} ${r.prenom} ${r.nom}.`);

  if (erreur) return <ErreurPage erreur={erreur} />;
  if (!data) return <Chargement />;

  return (
    <>
      <h1>Administration — Utilisateurs</h1>
      <MenuAdmin />
      <table>
        <thead><tr><th>Prénom</th><th>Nom</th><th>Courriel</th><th>Rôle(s)</th><th>Statut</th><th>Actions</th></tr></thead>
        <tbody>
          {data.map((u) => (
            <tr key={u.id}>
              <td>{u.prenom}</td><td>{u.nom}</td><td>{u.courriel}</td>
              <td>{u.roles.map((r) => <span key={r} className="badge b-bleu" style={{ marginRight: '.2rem' }}>{LIBELLE_ROLE[r] || r}</span>)}</td>
              <td>{u.actif ? <span className="badge b-vert">Actif</span> : <span className="badge b-rouge">Inactif</span>}</td>
              <td>
                <div className="actions">
                  {u.id !== user.id && (
                    <button type="button" className={`btn petit ${u.actif ? 'danger' : 'sec'}`} onClick={() => basculerActif(u)}>
                      {u.actif ? 'Désactiver' : 'Réactiver'}
                    </button>
                  )}
                  <button type="button" className="btn sec petit" onClick={() => basculerRole(u)}>
                    {u.roles.includes('ORGANISATEUR') ? 'Retirer Organisateur' : 'Attribuer Organisateur'}
                  </button>
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </>
  );
}

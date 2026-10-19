import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth.jsx';

/** Barre de navigation : liens selon le rôle (Public / Organisateur / Administrateur) ; la page courante est surlignée. */
export default function NavBar() {
  const { user, aRole, deconnexion } = useAuth();
  const navigate = useNavigate();
  const classe = ({ isActive }) => (isActive ? 'actif' : undefined);

  async function quitter() {
    await deconnexion(); // aucune confirmation requise
    navigate('/login');
  }

  return (
    <nav className="navbar">
      <NavLink to="/evenements" className="marque">🎓 GG’Événements</NavLink>
      {user && (
        <>
          <NavLink to="/evenements" className={classe}>Événements</NavLink>
          <NavLink to="/mes-evenements" className={classe}>Mes événements</NavLink>
          <NavLink to="/recherche" className={classe}>Recherche avancée</NavLink>
          {aRole('ORGANISATEUR') && <NavLink to="/organisateur/evenements" className={classe}>Mes événements à organiser</NavLink>}
          {aRole('ADMIN') && <NavLink to="/admin/tableau-de-bord" className={classe}>Administration</NavLink>}
          <span className="espace" />
          <span className="qui">{user.nomComplet}</span>
          <button type="button" className="lien" onClick={quitter}>Déconnexion</button>
        </>
      )}
    </nav>
  );
}

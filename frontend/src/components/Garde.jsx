import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from '../auth.jsx';
import { Chargement } from './Commun.jsx';
import { Link } from 'react-router-dom';

/** Pages réservées aux utilisateurs connectés : sinon redirection vers la page de connexion. */
export function RequiertConnexion() {
  const { user, chargement } = useAuth();
  const location = useLocation();
  if (chargement) return <Chargement />;
  if (!user) return <Navigate to="/login" replace state={{ depuis: location.pathname + location.search }} />;
  return <Outlet />;
}

/** Pages réservées à un rôle. Ce contrôle n'est qu'un confort d'affichage : l'API refuse de toute façon (HTTP 403). */
export function RequiertRole({ role }) {
  const { aRole } = useAuth();
  if (!aRole(role)) {
    return (
      <div className="carte-blanche vide">
        <div style={{ fontSize: '3rem' }}>⛔</div>
        <h1>Accès refusé</h1>
        <p>Vous n'avez pas la permission d'accéder à cette page.</p>
        <Link className="btn" to="/evenements">← Retour aux événements</Link>
      </div>
    );
  }
  return <Outlet />;
}

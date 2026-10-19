import { NavLink } from 'react-router-dom';

export default function MenuAdmin() {
  const classe = ({ isActive }) => `btn petit ${isActive ? '' : 'sec'}`;
  return (
    <div style={{ display: 'flex', gap: '.5rem', flexWrap: 'wrap', marginBottom: '1rem' }}>
      <NavLink className={classe} to="/admin/tableau-de-bord">📊 Tableau de bord</NavLink>
      <NavLink className={classe} to="/admin/utilisateurs">👥 Utilisateurs</NavLink>
      <NavLink className={classe} to="/admin/clubs">🏛️ Clubs</NavLink>
      <NavLink className={classe} to="/admin/categories">🏷️ Catégories</NavLink>
      <NavLink className={classe} to="/admin/lieux">📍 Lieux</NavLink>
    </div>
  );
}

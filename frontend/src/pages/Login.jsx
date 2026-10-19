import { useState } from 'react';
import { Navigate, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth.jsx';

export default function Login() {
  const { user, connexion } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [courriel, setCourriel] = useState('');
  const [motDePasse, setMotDePasse] = useState('');
  const [erreur, setErreur] = useState(null);
  const [envoi, setEnvoi] = useState(false);

  if (user) return <Navigate to="/evenements" replace />;

  async function soumettre(ev) {
    ev.preventDefault();
    setErreur(null);
    setEnvoi(true);
    try {
      await connexion(courriel.trim(), motDePasse);
      navigate('/evenements', { replace: true }); // après la connexion : liste des événements à venir
    } catch (e) {
      // INVALID : message générique (sans préciser quel champ est fautif) ; DISABLED : compte désactivé
      setErreur(e.code === 'DISABLED' ? 'Ce compte a été désactivé — contactez la Vie étudiante' : 'Identifiants invalides');
    } finally {
      setEnvoi(false);
    }
  }

  const raisonRedirection = location.state && location.state.depuis;

  return (
    <div className="connexion carte-blanche">
      <h1>Connexion</h1>
      {raisonRedirection && !erreur && <div className="alerte ok">Veuillez vous connecter pour continuer.</div>}
      {erreur && <div className="alerte err" role="alert">{erreur}</div>}
      <form onSubmit={soumettre}>
        <div className="champ">
          <label htmlFor="courriel">Courriel</label>
          <input id="courriel" type="email" required autoFocus value={courriel} onChange={(e) => setCourriel(e.target.value)} placeholder="prenom.nom@exemple.ca" />
        </div>
        <div className="champ">
          <label htmlFor="motDePasse">Mot de passe</label>
          <input id="motDePasse" type="password" required value={motDePasse} onChange={(e) => setMotDePasse(e.target.value)} />
        </div>
        <button type="submit" className="btn" style={{ width: '100%' }} disabled={envoi}>
          {envoi ? 'Connexion…' : 'Se connecter'}
        </button>
      </form>
    </div>
  );
}

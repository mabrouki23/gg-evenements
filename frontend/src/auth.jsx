import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { api, onUnauthorized } from './api.js';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [chargement, setChargement] = useState(true);

  // Au chargement : y a-t-il déjà une session ? (cet appel crée aussi le cookie CSRF)
  useEffect(() => {
    api
      .moi()
      .then(setUser)
      .catch(() => setUser(null))
      .finally(() => setChargement(false));
    onUnauthorized(() => setUser(null)); // session expirée
  }, []);

  const connexion = useCallback(async (courriel, motDePasse) => {
    const moi = await api.connexion(courriel, motDePasse);
    setUser(moi);
    return moi;
  }, []);

  const deconnexion = useCallback(async () => {
    try {
      await api.deconnexion();
    } finally {
      setUser(null);
    }
  }, []);

  const valeur = useMemo(
    () => ({
      user,
      chargement,
      connexion,
      deconnexion,
      aRole: (role) => !!user && user.roles.includes(role),
    }),
    [user, chargement, connexion, deconnexion]
  );
  return <AuthContext.Provider value={valeur}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  return useContext(AuthContext);
}

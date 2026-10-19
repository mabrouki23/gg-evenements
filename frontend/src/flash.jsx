import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { useLocation } from 'react-router-dom';

const FlashContext = createContext(null);

/**
 * Messages de confirmation / d'erreur affichés en haut de page.
 * `persist: true` : le message survit à UNE navigation (ex. « Événement créé » puis retour à la liste).
 */
export function FlashProvider({ children }) {
  const [flash, setFlash] = useState(null);
  const location = useLocation();

  useEffect(() => {
    setFlash((f) => (f && f.persist ? { ...f, persist: false } : null));
  }, [location.pathname]);

  const ok = useCallback((texte, persist = false) => setFlash({ type: 'ok', texte, persist }), []);
  const err = useCallback((texte, persist = false) => setFlash({ type: 'err', texte, persist }), []);
  const effacer = useCallback(() => setFlash(null), []);
  const valeur = useMemo(() => ({ flash, ok, err, effacer }), [flash, ok, err, effacer]);
  return <FlashContext.Provider value={valeur}>{children}</FlashContext.Provider>;
}

export function useFlash() {
  return useContext(FlashContext);
}

export function FlashMessage() {
  const { flash } = useFlash();
  if (!flash) return null;
  return (
    <div className={`alerte ${flash.type === 'ok' ? 'ok' : 'err'}`} role={flash.type === 'ok' ? 'status' : 'alert'}>
      {flash.texte}
    </div>
  );
}

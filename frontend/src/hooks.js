import { useCallback, useEffect, useRef, useState } from 'react';

/** Charge des données au montage (et quand `cle` change). Retourne { data, erreur, chargement, recharger }. */
export function useChargement(fonction, cle = '') {
  const [data, setData] = useState(null);
  const [erreur, setErreur] = useState(null);
  const [chargement, setChargement] = useState(true);
  const derniere = useRef(0);

  const charger = useCallback(() => {
    const numero = ++derniere.current;
    setChargement(true);
    return fonction()
      .then((d) => {
        if (numero === derniere.current) {
          setData(d);
          setErreur(null);
        }
      })
      .catch((e) => {
        if (numero === derniere.current) setErreur(e);
      })
      .finally(() => {
        if (numero === derniere.current) setChargement(false);
      });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [cle]);

  useEffect(() => {
    charger();
  }, [charger]);

  return { data, erreur, chargement, recharger: charger };
}

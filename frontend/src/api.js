// Client HTTP de l'API Spring Boot : session par cookie (JSESSIONID) + jeton CSRF (cookie XSRF-TOKEN -> en-tête X-XSRF-TOKEN).

function lireCookie(nom) {
  const trouve = document.cookie.split('; ').find((c) => c.startsWith(nom + '='));
  return trouve ? decodeURIComponent(trouve.split('=').slice(1).join('=')) : null;
}

/** Erreur renvoyée par l'API : status HTTP, message, erreurs par champ ({ titre: "..." }) et code éventuel (INVALID, DISABLED). */
export class ApiError extends Error {
  constructor(status, corps) {
    super((corps && corps.message) || `Erreur ${status}`);
    this.status = status;
    this.errors = (corps && corps.errors) || {};
    this.code = corps && corps.code;
  }
}

let surNonAutorise = () => {};
/** Appelé quand l'API répond 401 (session expirée) : l'application renvoie alors vers la page de connexion. */
export function onUnauthorized(fn) {
  surNonAutorise = fn;
}

async function appeler(methode, url, { json, formData, formUrl, params, ignore401 } = {}) {
  const headers = { Accept: 'application/json' };
  let body;
  if (json !== undefined) {
    headers['Content-Type'] = 'application/json';
    body = JSON.stringify(json);
  } else if (formData) {
    body = formData; // multipart : le navigateur fixe lui-même le Content-Type
  } else if (formUrl) {
    headers['Content-Type'] = 'application/x-www-form-urlencoded';
    body = new URLSearchParams(formUrl).toString();
  }
  if (methode !== 'GET') {
    const jeton = lireCookie('XSRF-TOKEN');
    if (jeton) headers['X-XSRF-TOKEN'] = jeton;
  }
  let requete = '';
  if (params) {
    const sp = new URLSearchParams();
    Object.entries(params).forEach(([k, v]) => {
      if (v !== undefined && v !== null && v !== '') sp.set(k, v);
    });
    const s = sp.toString();
    if (s) requete = '?' + s;
  }
  const reponse = await fetch(url + requete, { method: methode, headers, body, credentials: 'same-origin' });
  const texte = await reponse.text();
  let corps = null;
  if (texte) {
    try {
      corps = JSON.parse(texte);
    } catch {
      corps = null;
    }
  }
  if (!reponse.ok) {
    if (reponse.status === 401 && !ignore401) surNonAutorise();
    throw new ApiError(reponse.status, corps);
  }
  return corps;
}

export const api = {
  get: (url, params) => appeler('GET', url, { params }),
  post: (url, json) => appeler('POST', url, { json }),
  put: (url, json) => appeler('PUT', url, { json }),
  del: (url) => appeler('DELETE', url),
  postForm: (url, formData) => appeler('POST', url, { formData }),
  /** Utilisateur connecté, ou null (401 attendu quand personne n'est connecté). */
  moi: async () => {
    try {
      return await appeler('GET', '/api/me', { ignore401: true });
    } catch (e) {
      if (e.status === 401) return null;
      throw e;
    }
  },
  connexion: (courriel, motDePasse) =>
    appeler('POST', '/api/login', { formUrl: { courriel, motDePasse }, ignore401: true }),
  deconnexion: () => appeler('POST', '/api/logout', {}),
};

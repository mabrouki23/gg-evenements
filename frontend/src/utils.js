export const IMAGE_PAR_DEFAUT = '/images/defaut.svg';

export function imageDe(evenement) {
  return evenement.imageUrl || IMAGE_PAR_DEFAUT;
}

const optionsDateHeure = { day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' };
const optionsDate = { day: '2-digit', month: 'short', year: 'numeric' };

export function dateHeure(iso) {
  return iso ? new Date(iso).toLocaleString('fr-CA', optionsDateHeure) : '';
}

export function dateSeule(iso) {
  return iso ? new Date(iso).toLocaleDateString('fr-CA', optionsDate) : '';
}

/** Événement planifié et pas encore commencé : modification, inscription et désinscription permises. */
export function estOuvert(evenement) {
  return evenement.statut === 'PLANIFIE' && new Date(evenement.dateDebut) > new Date();
}

export const ETOILES = (note) => '★'.repeat(note) + '☆'.repeat(5 - note);

export function nombre(valeur, decimales = 1) {
  return Number(valeur).toLocaleString('fr-CA', { minimumFractionDigits: decimales, maximumFractionDigits: decimales });
}

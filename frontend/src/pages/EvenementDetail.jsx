import { useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { api } from '../api.js';
import { useFlash } from '../flash.jsx';
import { useChargement } from '../hooks.js';
import { Affiche, Chargement, ErreurPage, Places } from '../components/Commun.jsx';
import { dateHeure, dateSeule, ETOILES, nombre } from '../utils.js';

/** Détail d'un événement (fonctionnalité 4) : UN seul bouton d'action principal selon la situation de l'utilisateur. */
export default function EvenementDetail() {
  const { id } = useParams();
  const { data, erreur, chargement, recharger } = useChargement(() => api.get(`/api/evenements/${id}`), id);
  const flash = useFlash();
  const [envoi, setEnvoi] = useState(false);

  async function sinscrire() {
    setEnvoi(true);
    try {
      const r = await api.post(`/api/evenements/${id}/inscription`);
      if (r.statut === 'CONFIRMEE') flash.ok('Votre inscription est confirmée. À bientôt !');
      else flash.ok("L'événement est complet : vous avez été placé(e) en liste d'attente. Vous serez automatiquement confirmé(e) si une place se libère.");
    } catch (e) {
      flash.err(e.message);
    } finally {
      setEnvoi(false);
      recharger();
    }
  }

  async function seDesinscrire() {
    if (!window.confirm('Voulez-vous vraiment vous désinscrire de cet événement ?')) return;
    setEnvoi(true);
    try {
      await api.del(`/api/evenements/${id}/inscription`);
      flash.ok('Vous avez été désinscrit(e) de cet événement.');
    } catch (e) {
      flash.err(e.message);
    } finally {
      setEnvoi(false);
      recharger();
    }
  }

  if (erreur) return <ErreurPage erreur={erreur} />;
  if (chargement && !data) return <Chargement />;
  if (!data) return null;
  const e = data.evenement;

  return (
    <>
      <p><Link to="/evenements">← Retour à la liste des événements</Link></p>
      {e.annule && <div className="alerte annule">Cet événement a été annulé</div>}

      <div className="detail">
        <section>
          <Affiche evenement={e} className="affiche" />
          <div className="carte-blanche" style={{ marginTop: '1rem' }}>
            <h1>{e.titre}</h1>
            <p style={{ whiteSpace: 'pre-line' }}>{e.description}</p>
          </div>

          {data.avis.length > 0 && (
            <div className="carte-blanche">
              <h2>Avis des participants</h2>
              <p>
                <strong>Note moyenne : </strong>
                <span className="etoiles">★</span> <strong>{nombre(data.moyenne, 1)}</strong> / 5{' '}
                <span className="meta">({data.avis.length} avis)</span>
              </p>
              {data.avis.map((a) => (
                <div key={a.id} style={{ borderTop: '1px solid #e2e8f0', padding: '.6rem 0' }}>
                  <strong>{a.auteur}</strong> <span className="etoiles">{ETOILES(a.note)}</span>{' '}
                  <span className="meta">{dateSeule(a.date)}</span>
                  {a.commentaire && <div>{a.commentaire}</div>}
                </div>
              ))}
            </div>
          )}
        </section>

        <aside>
          <div className="carte-blanche">
            <p>
              <span className="pastille" style={{ background: e.categorie.couleur }} />
              <strong>{e.categorie.nom}</strong>{' '}
              {e.annule && <span className="badge b-rouge">Annulé</span>}
              {e.termine && !e.annule && <span className="badge b-gris">Terminé</span>}
            </p>
            <p>🏛️ Club : <Link to={`/clubs/${e.club.id}`}>{e.club.nom}</Link></p>
            <p>📍 <strong>{e.lieu.nom}</strong><br /><span className="meta">{e.lieu.campus} — {e.lieu.adresse}</span></p>
            <p>🕒 Début : <strong>{dateHeure(e.dateDebut)}</strong><br />🏁 Fin : <strong>{dateHeure(e.dateFin)}</strong></p>
            <hr />
            <p>
              Capacité maximale : <strong>{e.capaciteMax}</strong><br />
              Inscriptions confirmées : <strong>{e.nbConfirmees}</strong><br />
              En liste d'attente : <strong>{e.nbEnAttente}</strong>
            </p>
            <Places evenement={e} />

            {data.monStatut && (
              <div className={`carte-statut-inscription ${data.monStatut === 'CONFIRMEE' ? 'carte-statut-confirme' : 'carte-statut-attente'}`} style={{ marginTop: '1rem' }}>
                <div className="statut-contenu">
                  <strong>{data.monStatut === 'CONFIRMEE' ? 'Inscription confirmée' : "En liste d'attente"}</strong>
                </div>
              </div>
            )}

            <div style={{ marginTop: '1rem' }}>
              {data.peutSinscrire && (
                <button type="button" className="btn" style={{ width: '100%' }} onClick={sinscrire} disabled={envoi}>S'inscrire</button>
              )}
              {data.peutSeDesinscrire && (
                <button type="button" className="btn danger" style={{ width: '100%' }} onClick={seDesinscrire} disabled={envoi}>Se désinscrire</button>
              )}
            </div>
          </div>
        </aside>
      </div>
    </>
  );
}

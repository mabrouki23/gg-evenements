import { Navigate, Outlet, Route, Routes } from 'react-router-dom';
import { FlashMessage } from './flash.jsx';
import NavBar from './components/NavBar.jsx';
import { RequiertConnexion, RequiertRole } from './components/Garde.jsx';
import { Link } from 'react-router-dom';
import Login from './pages/Login.jsx';
import Evenements from './pages/Evenements.jsx';
import EvenementDetail from './pages/EvenementDetail.jsx';
import MesEvenements from './pages/MesEvenements.jsx';
import Avis from './pages/Avis.jsx';
import ClubFiche from './pages/ClubFiche.jsx';
import OrganisateurEvenements from './pages/organisateur/Evenements.jsx';
import FormulaireEvenement from './pages/organisateur/Formulaire.jsx';
import Inscriptions from './pages/organisateur/Inscriptions.jsx';
import Dashboard from './pages/admin/Dashboard.jsx';
import Utilisateurs from './pages/admin/Utilisateurs.jsx';
import Clubs from './pages/admin/Clubs.jsx';
import ClubFormulaire from './pages/admin/ClubFormulaire.jsx';
import { Categories, Lieux } from './pages/admin/Referentiel.jsx';

/** Gabarit commun : barre de navigation selon le rôle, messages, contenu, pied de page. */
function Gabarit() {
  return (
    <>
      <NavBar />
      <main className="conteneur">
        <FlashMessage />
        <Outlet />
      </main>
      <footer>Cégep Gérald-Godin — Vie étudiante · GG’Événements</footer>
    </>
  );
}

function NonTrouve() {
  return (
    <div className="carte-blanche vide">
      <div style={{ fontSize: '3rem' }}>🔍</div>
      <h1>Page introuvable</h1>
      <p>La page demandée n'existe pas (ou n'existe plus).</p>
      <Link className="btn" to="/evenements">← Retour aux événements</Link>
    </div>
  );
}

export default function App() {
  return (
    <Routes>
      <Route element={<Gabarit />}>
        <Route path="/login" element={<Login />} />
        <Route element={<RequiertConnexion />}>
          <Route path="/" element={<Navigate to="/evenements" replace />} />
          <Route path="/evenements" element={<Evenements />} />
          <Route path="/recherche" element={<Evenements avance />} />
          <Route path="/evenements/:id" element={<EvenementDetail />} />
          <Route path="/evenements/:id/avis" element={<Avis />} />
          <Route path="/mes-evenements" element={<MesEvenements />} />
          <Route path="/clubs/:id" element={<ClubFiche />} />

          <Route element={<RequiertRole role="ORGANISATEUR" />}>
            <Route path="/organisateur/evenements" element={<OrganisateurEvenements />} />
            <Route path="/organisateur/evenements/nouveau" element={<FormulaireEvenement />} />
            <Route path="/organisateur/evenements/:id/modifier" element={<FormulaireEvenement />} />
            <Route path="/organisateur/evenements/:id/inscriptions" element={<Inscriptions />} />
          </Route>

          <Route element={<RequiertRole role="ADMIN" />}>
            <Route path="/admin" element={<Navigate to="/admin/tableau-de-bord" replace />} />
            <Route path="/admin/tableau-de-bord" element={<Dashboard />} />
            <Route path="/admin/utilisateurs" element={<Utilisateurs />} />
            <Route path="/admin/clubs" element={<Clubs />} />
            <Route path="/admin/clubs/nouveau" element={<ClubFormulaire />} />
            <Route path="/admin/clubs/:id/modifier" element={<ClubFormulaire />} />
            <Route path="/admin/categories" element={<Categories />} />
            <Route path="/admin/lieux" element={<Lieux />} />
          </Route>
        </Route>
        <Route path="*" element={<NonTrouve />} />
      </Route>
    </Routes>
  );
}

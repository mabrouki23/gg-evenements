#!/bin/bash
# Test de fumée (2e partie) : rôles ADMIN / ORGANISATEUR, validation, 403 inter-clubs, avis, promotion. Exécuter après smoke.sh.
B=${1:-http://localhost:8080}
FAIL=0
ok(){ echo "OK   $1"; }
ko(){ echo "FAIL $1"; FAIL=1; }
chk(){ [ "$2" = "$3" ] && ok "$1 ($2)" || ko "$1 : attendu $3, obtenu $2"; }
js(){ python3 -c "import sys,json;d=json.load(sys.stdin);print($1)"; }
login(){ # $1=jar $2=courriel $3=mdp
  rm -f $1; curl -s -o /dev/null -c $1 -b $1 $B/api/csrf
  T=$(grep XSRF-TOKEN $1 | awk '{print $7}' | tail -1)
  curl -s -o /dev/null -w "%{http_code}" -c $1 -b $1 -X POST -H "X-XSRF-TOKEN: $T" --data-urlencode "courriel=$2" --data-urlencode "motDePasse=$3" $B/api/login; }
tk(){ grep XSRF-TOKEN $1 | awk '{print $7}' | tail -1; }
# req JAR args... -> code ; corps dans /tmp/b2
req(){ local j=$1; shift; curl -s -o /tmp/b2 -w "%{http_code}" -b $j -c $j "$@"; }
mut(){ local j=$1; shift; curl -s -o /tmp/b2 -w "%{http_code}" -b $j -c $j -H "X-XSRF-TOKEN: $(tk $j)" "$@"; }
A=/tmp/ja; O1=/tmp/jo1; O2=/tmp/jo2; E3=/tmp/je3; E1=/tmp/je1

echo "== DISABLED (diagnostic)"
chk "login inactif" "$(login /tmp/jx inactif@ggevenements.ca 'Etudiant123!')" 401; cat /tmp/b2 2>/dev/null; echo
curl -s -c /tmp/jx -b /tmp/jx -X POST -H "X-XSRF-TOKEN: $(tk /tmp/jx)" --data-urlencode "courriel=inactif@ggevenements.ca" --data-urlencode "motDePasse=Etudiant123!" $B/api/login; echo

echo "== ADMIN"
chk "login admin" "$(login $A admin@ggevenements.ca 'Admin123!')" 200
chk "admin: utilisateurs" "$(req $A $B/api/admin/utilisateurs)" 200
echo "   utilisateurs: $(js 'len(d)' < /tmp/b2)  | rôles admin: $(js '[u["roles"] for u in d if u["courriel"].startswith("admin")]' < /tmp/b2)"
chk "admin: stats" "$(req $A $B/api/admin/stats)" 200; cat /tmp/b2; echo
chk "admin: clubs" "$(req $A $B/api/admin/clubs)" 200
chk "admin interdit au module organisateur" "$(req $A $B/api/organisateur/evenements)" 403
chk "catégorie invalide" "$(mut $A -X POST -H 'Content-Type: application/json' -d '{"nom":"","couleur":"rouge"}' $B/api/admin/categories)" 400; cat /tmp/b2; echo
chk "catégorie créée" "$(mut $A -X POST -H 'Content-Type: application/json' -d '{"nom":"Jeux vidéo","description":"Tournois","couleur":"#112233"}' $B/api/admin/categories)" 201
NEWCAT=$(js 'd["id"]' < /tmp/b2)
chk "doublon de catégorie" "$(mut $A -X POST -H 'Content-Type: application/json' -d '{"nom":"jeux vidéo","couleur":"#112233"}' $B/api/admin/categories)" 422
chk "suppression catégorie inutilisée" "$(mut $A -X DELETE $B/api/admin/categories/$NEWCAT)" 204
CATUSED=$(curl -s -b $A $B/api/admin/categories | js 'd[0]["id"]')
chk "suppression catégorie utilisée refusée" "$(mut $A -X DELETE $B/api/admin/categories/$CATUSED)" 422; cat /tmp/b2; echo
chk "lieu créé" "$(mut $A -X POST -H 'Content-Type: application/json' -d '{"nom":"Salle test","campus":"Pavillon test","adresse":"Local 1","capaciteMax":10}' $B/api/admin/lieux)" 201
NEWLIEU=$(js 'd["id"]' < /tmp/b2)
chk "lieu modifié" "$(mut $A -X PUT -H 'Content-Type: application/json' -d '{"nom":"Salle test","campus":"Pavillon test","adresse":"Local 2","capaciteMax":12}' $B/api/admin/lieux/$NEWLIEU)" 200
chk "lieu supprimé" "$(mut $A -X DELETE $B/api/admin/lieux/$NEWLIEU)" 204
chk "admin ne peut pas se désactiver" "$(mut $A -X POST $B/api/admin/utilisateurs/$(curl -s -b $A $B/api/me | js 'd["id"]')/actif)" 422
E5=$(curl -s -b $A $B/api/admin/utilisateurs | js '[u["id"] for u in d if u["courriel"].startswith("etudiant5")][0]')
chk "club créé (responsable etudiant5)" "$(mut $A -X POST -H 'Content-Type: application/json' -d "{\"nom\":\"Club photo\",\"description\":\"Photo\",\"courrielContact\":\"photo@ggevenements.ca\",\"responsableId\":$E5}" $B/api/admin/clubs)" 201
NEWCLUB=$(js 'd["id"]' < /tmp/b2)
curl -s -b $A $B/api/admin/utilisateurs | js '[u["roles"] for u in d if u["courriel"].startswith("etudiant5")][0]' | grep -q ORGANISATEUR && ok "responsable → rôle ORGANISATEUR automatique" || ko "rôle automatique"
chk "club désactivé" "$(mut $A -X POST $B/api/admin/clubs/$NEWCLUB/actif)" 200
curl -s -b $A $B/api/reference | js '[c["nom"] for c in d["clubs"]]' | grep -q "Club photo" && ko "club inactif encore dans les listes" || ok "club désactivé absent des listes déroulantes"
chk "retrait rôle bloqué (responsable de club)" "$(mut $A -X POST $B/api/admin/utilisateurs/$E5/organisateur)" 422

echo "== ORGANISATEUR 1 (2 clubs)"
chk "login org1" "$(login $O1 organisateur@ggevenements.ca 'Organisateur123!')" 200
chk "mes clubs" "$(req $O1 $B/api/organisateur/clubs)" 200
echo "   clubs: $(js '[c["nom"] for c in d["clubs"]]' < /tmp/b2) | clubCreation=$(js 'd["clubCreation"]' < /tmp/b2) | message=$(js 'd["messageClub"]' < /tmp/b2)"
CLUBS=$(cat /tmp/b2)
CAT=$(curl -s -b $O1 $B/api/reference | js 'd["categories"][0]["id"]'); LIEU=$(curl -s -b $O1 $B/api/reference | js 'd["lieux"][0]["id"]')
ev(){ # titre debut fin capacite [extra]
  mut $O1 -X POST -F "titre=$1" -F "description=Une description" -F "categorieId=$CAT" -F "lieuId=$LIEU" -F "dateDebut=$2" -F "dateFin=$3" -F "capaciteMax=$4" "${@:5}" $B/api/organisateur/evenements; }
chk "création sans club actif choisi" "$(ev 'Test A' 2030-01-10T18:00 2030-01-10T20:00 20)" 422; cat /tmp/b2; echo
CLUBID=$(echo "$CLUBS" | js '[c["id"] for c in d["clubs"] if c["nom"].startswith("Club d")][0]')
OTHERCLUB=$(curl -s -b $A $B/api/admin/clubs | js '[c["id"] for c in d if c["nom"]=="Club de plein air"][0]')
chk "club actif d'un AUTRE responsable refusé" "$(mut $O1 -X POST -H 'Content-Type: application/json' -d "{\"id\":$OTHERCLUB}" $B/api/organisateur/club-actif)" 403
chk "club actif choisi" "$(mut $O1 -X POST -H 'Content-Type: application/json' -d "{\"id\":$CLUBID}" $B/api/organisateur/club-actif)" 200
chk "création : fin avant début + titre vide" "$(ev '' 2030-01-10T20:00 2030-01-10T18:00 20)" 400; cat /tmp/b2; echo
chk "création : capacité > lieu" "$(ev 'Test B' 2030-01-10T18:00 2030-01-10T20:00 5000)" 422; cat /tmp/b2; echo
chk "création valide" "$(ev 'Atelier de test' 2030-01-10T18:00 2030-01-10T20:00 20)" 201
NEWEV=$(js 'd["id"]' < /tmp/b2); echo "   club de l'événement créé: $(js 'd["club"]["nom"]' < /tmp/b2) | statut: $(js 'd["statut"]' < /tmp/b2)"
printf '\x89PNG\r\n\x1a\n\x00\x00\x00\rIHDR\x00\x00\x00\x01\x00\x00\x00\x01\x08\x06\x00\x00\x00\x1f\x15\xc4\x89' > /tmp/x.png
chk "création avec affiche PNG" "$(ev 'Avec affiche' 2030-02-10T18:00 2030-02-10T20:00 20 -F 'affiche=@/tmp/x.png;type=image/png')" 201
IMG=$(js 'd["imageUrl"]' < /tmp/b2); echo "   imageUrl=$IMG"
chk "affiche téléversée accessible (connecté)" "$(req $O1 $B$IMG)" 200
chk "affiche inaccessible sans connexion" "$(curl -s -o /dev/null -w '%{http_code}' $B$IMG)" 401
echo "text" > /tmp/x.txt
chk "affiche non image refusée" "$(ev 'Mauvais fichier' 2030-03-10T18:00 2030-03-10T20:00 20 -F 'affiche=@/tmp/x.txt;type=text/plain')" 422; cat /tmp/b2; echo
chk "formulaire de modification" "$(req $O1 $B/api/organisateur/evenements/$NEWEV/formulaire)" 200
chk "modification" "$(mut $O1 -X POST -F 'titre=Atelier modifié' -F 'description=Nouvelle desc' -F "categorieId=$CAT" -F "lieuId=$LIEU" -F 'dateDebut=2030-01-11T18:00' -F 'dateFin=2030-01-11T20:00' -F 'capaciteMax=25' $B/api/organisateur/evenements/$NEWEV/modifier)" 200

echo "== ORGANISATEUR 2 : 403 sur un événement d'un autre club"
chk "login org2" "$(login $O2 organisateur2@ggevenements.ca 'Organisateur123!')" 200
for p in "GET:formulaire" "GET:inscriptions"; do m=${p%%:*}; r=${p##*:}; chk "org2 $r (autre club)" "$(req $O2 $B/api/organisateur/evenements/$NEWEV/$r)" 403; done
chk "org2 modifier (autre club)" "$(mut $O2 -X POST -F 'titre=Piraté' -F 'description=x' -F "categorieId=$CAT" -F "lieuId=$LIEU" -F 'dateDebut=2030-01-11T18:00' -F 'dateFin=2030-01-11T20:00' -F 'capaciteMax=5' $B/api/organisateur/evenements/$NEWEV/modifier)" 403
chk "org2 annuler (autre club)" "$(mut $O2 -X POST $B/api/organisateur/evenements/$NEWEV/annuler)" 403

echo "== Gestion des inscriptions, capacité minimale, annulation"
JEUX=$(curl -s -b $O1 $B/api/organisateur/evenements | js '[e["id"] for e in d if e["titre"].startswith("Soirée jeux")][0]')
chk "inscriptions de la soirée jeux" "$(req $O1 $B/api/organisateur/evenements/$JEUX/inscriptions)" 200
echo "   confirmées: $(js '[i["nom"] for i in d["confirmees"]]' < /tmp/b2) | attente: $(js '[i["nom"] for i in d["enAttente"]]' < /tmp/b2)"
INSC=$(js 'd["confirmees"][0]["id"]' < /tmp/b2)
chk "présence confirmée" "$(mut $O1 -X POST -H 'Content-Type: application/json' -d '{"presente":true}' $B/api/organisateur/inscriptions/$INSC/presence)" 204
chk "capacité sous les confirmés refusée" "$(mut $O1 -X POST -F 'titre=Soirée jeux de société' -F 'description=x' -F "categorieId=$CAT" -F "lieuId=$LIEU" -F 'dateDebut=2030-01-11T18:00' -F 'dateFin=2030-01-11T20:00' -F 'capaciteMax=1' $B/api/organisateur/evenements/$JEUX/modifier)" 422; cat /tmp/b2; echo
chk "annuler l'événement de test" "$(mut $O1 -X POST $B/api/organisateur/evenements/$NEWEV/annuler)" 204
chk "détail après annulation" "$(req $O1 $B/api/evenements/$NEWEV)" 200
echo "   annulé=$(js 'd["evenement"]["annule"]' < /tmp/b2) peutSinscrire=$(js 'd["peutSinscrire"]' < /tmp/b2)"
chk "inscription à un événement annulé" "$(login $E1 etudiant1@ggevenements.ca 'Etudiant123!' >/dev/null; mut $E1 -X POST $B/api/evenements/$NEWEV/inscription)" 422

echo "== Avis et promotion de la liste d'attente (étudiant 3)"
chk "login etudiant3" "$(login $E3 etudiant3@ggevenements.ca 'Etudiant123!')" 200
chk "mes événements" "$(req $E3 $B/api/mes-evenements)" 200
echo "   à venir: $(js '[(l["evenement"]["titre"],l["statut"]) for l in d["aVenir"]]' < /tmp/b2)"
echo "   passés: $(js '[(l["evenement"]["titre"],l["peutLaisserAvis"]) for l in d["passes"]]' < /tmp/b2)"
js '[l["statut"] for l in d["aVenir"] if l["evenement"]["titre"].startswith("Soirée jeux")][0]' < /tmp/b2 | grep -q CONFIRMEE && ok "étudiant3 promu depuis la liste d'attente (CONFIRMEE)" || ko "promotion"
PAST=$(js '[l["evenement"]["id"] for l in d["passes"] if l["peutLaisserAvis"]][0]' < /tmp/b2)
chk "avis : note invalide" "$(mut $E3 -X POST -H 'Content-Type: application/json' -d '{"note":7,"commentaire":"x"}' $B/api/evenements/$PAST/avis)" 400; cat /tmp/b2; echo
chk "avis valide" "$(mut $E3 -X POST -H 'Content-Type: application/json' -d '{"note":4,"commentaire":"Très bien"}' $B/api/evenements/$PAST/avis)" 201
chk "avis en double refusé" "$(mut $E3 -X POST -H 'Content-Type: application/json' -d '{"note":5}' $B/api/evenements/$PAST/avis)" 422
chk "détail d'un événement terminé" "$(req $E3 $B/api/evenements/$PAST)" 200
echo "   avis: $(js 'len(d["avis"])' < /tmp/b2) | moyenne: $(js 'd["moyenne"]' < /tmp/b2)"
chk "fiche d'un club" "$(req $E3 $B/api/clubs/$CLUBID)" 200
chk "route inconnue /api/zzz (connecté)" "$(req $E3 $B/api/zzz)" 404
echo "FIN smoke2: FAIL=$FAIL"

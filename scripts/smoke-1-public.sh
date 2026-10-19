#!/bin/bash
# Test de fumée de l'API : usage  smoke.sh http://localhost:8099
B=${1:-http://localhost:8080}
# Attendre que le serveur réponde puis que les données de démonstration soient insérées
for i in $(seq 1 60); do curl -s -o /dev/null $B/api/csrf && break; sleep 1; done; sleep 4
J=/tmp/jar_$$.txt
ok(){ echo "OK   $1"; }
ko(){ echo "FAIL $1"; FAIL=1; }
chk(){ [ "$2" = "$3" ] && ok "$1 ($2)" || ko "$1 : attendu $3, obtenu $2"; }
tok(){ grep XSRF-TOKEN $J | awk '{print $7}' | tail -1; }
req(){ curl -s -o /tmp/body_$$ -w "%{http_code}" -b $J -c $J "$@"; }
FAIL=0
rm -f $J
chk "me sans connexion" "$(req $B/api/me)" 401
chk "liste sans connexion" "$(req $B/api/evenements)" 401
chk "CSRF cookie" "$(req $B/api/csrf)" 200
chk "login sans CSRF refusé" "$(req -X POST $B/api/login -d 'courriel=etudiant1@ggevenements.ca&motDePasse=Etudiant123!')" 403
chk "mauvais mot de passe" "$(req -X POST $B/api/login -H "X-XSRF-TOKEN: $(tok)" -d 'courriel=etudiant1@ggevenements.ca&motDePasse=nope')" 401
grep -q INVALID /tmp/body_$$ && ok "code INVALID" || ko "code INVALID"
chk "compte désactivé + bon mdp" "$(req -X POST $B/api/login -H "X-XSRF-TOKEN: $(tok)" -d 'courriel=inactif@ggevenements.ca&motDePasse=Etudiant123!')" 401
grep -q DISABLED /tmp/body_$$ && ok "code DISABLED" || { ko "code DISABLED"; cat /tmp/body_$$; echo; }
chk "compte désactivé + mauvais mdp" "$(req -X POST $B/api/login -H "X-XSRF-TOKEN: $(tok)" -d 'courriel=inactif@ggevenements.ca&motDePasse=zzz')" 401
grep -q INVALID /tmp/body_$$ && ok "pas de fuite d'état du compte" || ko "fuite d'état"
chk "login étudiant" "$(req -X POST $B/api/login -H "X-XSRF-TOKEN: $(tok)" -d 'courriel=etudiant1@ggevenements.ca&motDePasse=Etudiant123!')" 200
cat /tmp/body_$$; echo
chk "me" "$(req $B/api/me)" 200
chk "reference" "$(req $B/api/reference)" 200
chk "liste" "$(req "$B/api/evenements")" 200
python3 - <<PY
import json;d=json.load(open('/tmp/body_$$'));print("   page",d['page'],"/",d['totalPages'],"total",d['totalElements'],"par page",len(d['contenu']))
PY
chk "tri par places" "$(req "$B/api/evenements?tri=places")" 200
chk "recherche titre" "$(req "$B/api/evenements?q=robots")" 200
grep -q "robots" /tmp/body_$$ && ok "recherche trouve 'robots'" || ko "recherche"
chk "liste page 2" "$(req "$B/api/evenements?page=1")" 200
chk "détail 404" "$(req $B/api/evenements/999999)" 404
chk "admin interdit à un étudiant" "$(req $B/api/admin/utilisateurs)" 403
chk "organisateur interdit à un étudiant" "$(req $B/api/organisateur/evenements)" 403
# événement complet : "Soirée jeux de société"
ID=$(curl -s -b $J "$B/api/evenements?q=jeux" | python3 -c "import sys,json;print(json.load(sys.stdin)['contenu'][0]['id'])")
echo "   id soirée jeux = $ID"
chk "détail" "$(req $B/api/evenements/$ID)" 200
python3 - <<PY
import json;d=json.load(open('/tmp/body_$$'));e=d['evenement'];print("   confirmés",e['nbConfirmees'],"attente",e['nbEnAttente'],"places",e['placesRestantes'],"| moi",d['monStatut'],"s'inscrire",d['peutSinscrire'],"se désinscrire",d['peutSeDesinscrire'])
PY
chk "déjà inscrit (doublon)" "$(req -X POST -H "X-XSRF-TOKEN: $(tok)" $B/api/evenements/$ID/inscription)" 422
chk "désinscription" "$(req -X DELETE -H "X-XSRF-TOKEN: $(tok)" $B/api/evenements/$ID/inscription)" 204
chk "mes événements" "$(req $B/api/mes-evenements)" 200
chk "réinscription (liste d'attente ou confirmé)" "$(req -X POST -H "X-XSRF-TOKEN: $(tok)" $B/api/evenements/$ID/inscription)" 201
cat /tmp/body_$$; echo
chk "DELETE sans CSRF refusé" "$(curl -s -o /dev/null -w '%{http_code}' -b $J -X DELETE $B/api/evenements/$ID/inscription)" 403
chk "logout" "$(req -X POST -H "X-XSRF-TOKEN: $(tok)" $B/api/logout)" 204
chk "me après logout" "$(req $B/api/me)" 401
echo "FIN smoke: FAIL=$FAIL"

# Guide de prise en main — Les Écrans de Villeneuve

Ce guide accompagne le projet. Il ne remplace pas la lecture du sujet : les règles B1 à B16 et les contraintes C1 à C6 du sujet font foi.

## 1. Installer et ouvrir

1. Installer un JDK 21.
2. Installer IntelliJ IDEA.
3. Ouvrir le dossier `ecrans-villeneuve` (et non le seul dossier `code/vente`).
4. Dans IntelliJ : `File > Project Structure > Project SDK` : choisir Java 21.
5. Ouvrir la fenêtre Maven et cliquer sur `Reload All Maven Projects`.
6. Lancer les tests depuis `code/vente` : `mvn test`.

Le projet utilise Spring Boot 3.5.0. Il faut une connexion internet lors du premier lancement pour télécharger les dépendances Maven.

## 2. Comprendre le métier en une minute

Un client choisit des fauteuils pour une séance. Le système bloque ces fauteuils pendant 10 minutes (B2). Il calcule le prix maintenant et le fige dans les billets (B6). Le client paie chez PayVia. PayVia appelle notre webhook. La commande devient payée et les fauteuils deviennent vendus. Si le client ne paie pas, le planificateur libère les places et expire la commande.

La règle la plus importante est B1 : deux clients ne doivent jamais acheter le même fauteuil pour la même séance.

## 3. Les trois grandes parties du code

- `domaine/` : les règles métier. `Seance` protège l'état des fauteuils ; `Commande` protège le contenu et le statut de l'achat. Cette partie ne connaît ni Spring, ni HTTP, ni PostgreSQL.
- `application/` : les cas d'usage et les ports. Un service orchestre les opérations, mais laisse les règles au domaine.
- `adapter/` : les connexions au monde extérieur. REST, webhook PayVia, SortiesFlash, stockage mémoire, paiement simulé.
- `config/` : la composition root qui assemble les objets.

## 4. Les étapes codées

### Étapes 1 et 2 — Les agrégats

`Seance.bloquer` vérifie d'abord que la vente est ouverte, que toutes les places existent et qu'elles sont libres. Il ne modifie la carte qu'après toutes les vérifications : c'est le « tout ou rien ». `Seance.vendre` transforme un blocage en vente et autorise le paiement tardif si personne n'a repris les places. `libererBlocagesExpires` libère les blocages échus.

`Commande.creer` refuse une commande vide, de plus de 10 billets ou contenant deux fois le même fauteuil. `total` additionne les montants figés. `payer` et `expirer` appliquent des transitions de statut et enregistrent des événements.

### Étapes 3 à 5 — Les cas d'usage

`ReserverDesPlacesService` charge la séance, calcule les tarifs, demande au domaine de bloquer les places, crée/enregistre la commande puis demande le lien de paiement.

`ConfirmerPaiementService` charge commande et séance, demande à la séance de vendre les fauteuils et à la commande de passer à PAYEE. Il enregistre puis publie les événements. Les notifications répétées d'une commande déjà payée sont ignorées (B14). Si le paiement arrive après expiration et que les places ont été reprises, la commande passe à A_REMBOURSER (B13).

`ExpirerBlocagesService` parcourt les séances, libère les blocages échus et expire les commandes encore en attente.

### Étape 6 — La dépendance inversée

Le service de réservation dépend du port `GrilleTarifaire`, pas de l'adapter concret `GrilleTarifaireFixe`. Ainsi, le cœur exprime ce dont il a besoin sans connaître l'implémentation. Le même port pourrait être branché sur une API ou une base sans changer le service.

### Étape 7 — Spring Boot

- `ReservationController` reçoit le JSON, le traduit en objets du domaine et appelle le port entrant `ReserverDesPlaces`.
- `WebhookPayViaController` ne fait que recevoir la notification et la confier à l'ACL `WebhookPayVia`.
- `ExpirationPlanifiee` appelle le port `ExpirerBlocages` toutes les minutes.
- `GestionErreurs` transforme les erreurs métier en codes HTTP : 409 pour conflit de places, 404 pour ressource absente, 422 pour règle métier, 400 pour saisie invalide.
- Swagger est disponible à `http://localhost:8080/swagger-ui.html`.

### Étape 8 — SortiesFlash

Le partenaire envoie son propre JSON. `AclSortiesFlash` traduit `show_ref`, `seats` et les types de billets dans notre vocabulaire. Le contrôleur appelle ensuite le port `EnregistrerVentePartenaire`. Le montant payé chez le partenaire est conservé dans `VentePartenaireEnregistree` pour le rapprochement et la déclaration CNC.

## 5. Tester manuellement dans Swagger

Démarrer `VenteApplication`, puis ouvrir Swagger.

Exemple de réservation :
```json
{
  "billets": [
    {"place": "A1", "categorie": "PLEIN"},
    {"place": "A2", "categorie": "ENFANT"}
  ]
}
```
Appel : `POST /seances/S42/reservations`. Résultat attendu : HTTP 201 et un total de 18,00 €.

Copier le champ `commande`, puis appeler `POST /webhooks/payvia` :
```json
{
  "event": "payment.succeeded",
  "paymentId": "pv_1",
  "reference": "REMPLACER_PAR_LA_COMMANDE",
  "amount": 1800,
  "currency": "EUR",
  "occurredAt": "2026-11-06T20:05:00Z"
}
```
Résultat attendu : HTTP 204. Réserver de nouveau A1 doit donner HTTP 409.

Le test partenaire utilise le format SortiesFlash :
`POST /partner/v2/orders` avec `show_ref` égal à `VIL-aaaammjj-2030-S3`, où `aaaammjj` est la date de demain, `seats` est une liste de places et `ticket_types` contient autant de valeurs (`ADULT`, `STUDENT`, `CHILD`).

## 6. Préparer l'oral

Fil conducteur suggéré :
1. Le problème : risque de double vente sur 5 canaux et logiciel historique en fin de support.
2. Le cœur métier : le plan de salle `Seance` protège B1 ; `Commande` suit le paiement.
3. Le choix architectural : DDD et hexagonal seulement là où la valeur et le risque le justifient.
4. La démonstration : réservation, webhook de paiement, tentative de réservation de la même place (409), vente partenaire.
5. Les limites assumées : persistance mémoire pédagogique, transaction PostgreSQL et outbox à finaliser en production, scan offline, idempotence durable et remboursement partenaire à contractualiser.

### Questions probables

**Pourquoi deux agrégats ?** Parce que `Seance` protège la disponibilité des fauteuils et `Commande` le cycle de vie de l'achat. La commande référence la séance par son identifiant, pas par un objet mutable.

**Pourquoi le blocage de 10 minutes ?** Pour empêcher un autre canal de vendre le même fauteuil pendant le paiement, sans le retenir indéfiniment (B2).

**Pourquoi le prix est-il dans le billet ?** B6 garantit au client le prix calculé au moment du choix, même si le marketing modifie la grille ensuite.

**Pourquoi une ACL pour SortiesFlash ?** Son langage externe ne doit pas contaminer le domaine. Si son format change, seule l'ACL doit changer.

**Pourquoi un port ?** Le port décrit ce dont l'application a besoin ; l'adapter fournit la façon technique de le faire. Le cœur n'est pas dépendant d'une base ou d'un fournisseur.

**Pourquoi la transaction entre séance et commande ?** La confirmation doit rendre cohérents l'état vendu des fauteuils et le statut payé de la commande. Sinon un blocage pourrait expirer entre les deux opérations.

**Que signifie 409 ?** Le client a demandé une place qui n'est plus disponible ; ce n'est pas une panne serveur, mais un conflit métier.

## 7. Limites à annoncer honnêtement

La base de données PostgreSQL de production, les migrations, la transaction réellement atomique entre repositories, l'outbox, l'envoi effectif des billets, le stockage offline des douchettes et le contexte complet des abonnements ne sont pas implémentés dans cette version pédagogique. Le sujet demande de les concevoir et certains restent hors des étapes de code obligatoires. Ne prétends pas qu'ils fonctionnent déjà.

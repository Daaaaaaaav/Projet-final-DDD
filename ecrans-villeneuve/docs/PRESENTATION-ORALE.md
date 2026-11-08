# Trame de présentation orale — Les Écrans de Villeneuve

À adapter à la durée annoncée par l'enseignant. Ne pas lire mot à mot : utiliser cette trame pour expliquer avec ses propres mots.

## 1. Introduction — le problème

« Le cinéma a huit salles et vend ses places sur plusieurs canaux : site, mobile, bornes, guichets et partenaires. L'ancien logiciel ne sera plus maintenu après 2027. Le problème métier le plus critique est la double vente : deux clients ne doivent jamais obtenir le même fauteuil pour une séance. Le pic d'une avant-première peut atteindre 420 places en moins de cinq minutes. »

## 2. Notre domaine et notre découpage

« Nous avons distingué plusieurs bounded contexts. La Programmation décrit un film projeté dans une salle à un horaire. Dans le contexte Vente, une séance représente surtout un plan de salle commercialisé. Ce vocabulaire différent est important : il évite de mélanger les responsabilités. Vente est notre Core Domain, car c'est là que se trouve l'invariant de disponibilité et le risque concurrentiel. »

## 3. Architecture

« Nous avons choisi un monolithe modulaire Spring Boot avec une architecture hexagonale. Le domaine reste en Java pur. Les contrôleurs HTTP, le webhook PayVia et le planificateur sont des adapters entrants. Les ports secondaires isolent le stockage, la tarification, le paiement et la publication d'événements. Cela permet de changer la technique sans modifier les règles métier. »

## 4. Agrégats et concurrence

« L'agrégat Seance protège l'état de chaque fauteuil : libre, bloqué ou vendu. L'agrégat Commande suit le panier de un à dix billets et son statut. La commande référence la séance par identifiant. Le prix est copié dans le billet au moment de la réservation pour respecter B6. Le blocage dure dix minutes. »

« Le squelette pédagogique utilise un verrou optimiste sur la séance entière. Pour la production, nous recommandons un verrou par place avec une contrainte unique sur le couple séance-place, car deux clients achetant des fauteuils différents ne devraient pas se bloquer mutuellement. Les réservations multi-places seraient réalisées dans une transaction, en verrouillant les places dans l'ordre pour éviter les interblocages. »

## 5. Démonstration

1. Lancer l'application Spring Boot.
2. Ouvrir Swagger à `http://localhost:8080/swagger-ui.html`.
3. Appeler `POST /seances/S42/reservations` avec A1 et A2.
4. Montrer le total et le lien de paiement.
5. Appeler `POST /webhooks/payvia` avec le numéro de commande reçu.
6. Réserver A1 une seconde fois : montrer le HTTP 409.
7. Appeler `POST /partner/v2/orders` pour montrer l'ACL SortiesFlash et la vente partenaire.

## 6. Limites et conclusion

« Le code fourni et complété démontre les règles principales, les ports et adapters, l'API, PayVia et l'ACL SortiesFlash. Pour une mise en production, il reste à brancher PostgreSQL avec une transaction réelle et une outbox, les notifications réelles, le contrôle offline des douchettes et les contextes complets Abonnements et Tarification. Nous avons rendu ces limites visibles plutôt que de prétendre qu'elles sont déjà résolues. »

## Questions à réviser

- Pourquoi le domaine ne doit-il pas importer Spring ?
- Quelle différence entre blocage et vente ?
- Pourquoi une commande contient-elle au maximum dix places ?
- Pourquoi la vente partenaire ne passe-t-elle pas par PayVia ?
- À quoi sert `VentePartenaireEnregistree` ?
- Que fait le système quand un paiement arrive après l'expiration ?
- Pourquoi le code de démonstration garde-t-il un verrou par séance alors que la décision de production recommande un verrou par place ?

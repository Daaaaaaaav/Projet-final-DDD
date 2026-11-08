# Les Écrans de Villeneuve — projet final

Projet Java 21 / Spring Boot de billetterie de cinéma.

## Contenu
- `code/vente/` : cœur métier, ports/adapters, API REST et intégration SortiesFlash.
- `docs/DAT-Ecrans-de-Villeneuve.docx` et `.pdf` : document d'architecture.
- `docs/diagrammes/` : sources `.dot` et exports `.png`.
- `docs/GUIDE-DEBUTANT.md` : explications et préparation à l'oral.
- `docs/PRESENTATION-ORALE.md` : trame de présentation et questions à réviser.

## Lancer
Prérequis : JDK 21 et Maven 3.9+.
Depuis `code/vente/` :
```bash
mvn test
mvn spring-boot:run
```
Swagger : http://localhost:8080/swagger-ui.html

## À compléter avant rendu
- Remplacer « équipe / membres » dans le DAT.
- Vérifier les choix et hypothèses avec les membres de l'équipe.
- Lancer `mvn test` avec Maven local et corriger tout écart éventuel.
- Commit/push, créer le tag `v1.0`, exporter et déposer le PDF.

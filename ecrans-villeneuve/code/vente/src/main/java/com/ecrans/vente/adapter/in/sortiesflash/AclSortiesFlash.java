
package com.ecrans.vente.adapter.in.sortiesflash;

import com.ecrans.vente.application.exception.SeanceInconnue;
import com.ecrans.vente.application.port.in.*;
import com.ecrans.vente.domaine.commande.Categorie;
import com.ecrans.vente.domaine.commun.Montant;

import java.util.*;

public class AclSortiesFlash {

 private final CorrespondanceSeances correspondances;

 public AclSortiesFlash(CorrespondanceSeances c) {
  correspondances = c;
 }

 public VentePartenaire traduire(CommandeSortiesFlash c) {

  // 1. Vérifier les listes avant toute autre opération
  if (c.seats() == null || c.ticket_types() == null
          || c.seats().size() != c.ticket_types().size()) {
   throw new IllegalArgumentException(
           "Le nombre de ticket_types doit correspondre au nombre de seats"
   );
  }

  // 2. Rechercher la séance partenaire
  var seance = correspondances.trouver(c.show_ref())
          .orElseThrow(() -> new SeanceInconnue(
                  new com.ecrans.vente.domaine.commun.SeanceId(c.show_ref())
          ));

  // 3. Traduire les billets
  List<DemandeBillet> billets = new ArrayList<>();

  for (int i = 0; i < c.seats().size(); i++) {

   Categorie cat = switch (c.ticket_types().get(i)) {
    case "ADULT" -> Categorie.PLEIN;
    case "STUDENT" -> Categorie.REDUIT;
    case "CHILD" -> Categorie.ENFANT;
    default -> throw new IllegalArgumentException(
            "Type de billet SortiesFlash inconnu : "
                    + c.ticket_types().get(i)
    );
   };

   billets.add(new DemandeBillet(
           com.ecrans.vente.domaine.commun.PlaceId.de(c.seats().get(i)),
           cat
   ));
  }

  // 4. Construire la vente partenaire
  return new VentePartenaire(
          "SortiesFlash",
          c.partner_order_id(),
          seance,
          billets,
          Montant.centimes(c.paid_amount_cents())
  );
 }
}

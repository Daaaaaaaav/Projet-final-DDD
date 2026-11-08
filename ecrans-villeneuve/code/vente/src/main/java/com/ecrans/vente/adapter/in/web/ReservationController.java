package com.ecrans.vente.adapter.in.web;
import com.ecrans.vente.application.port.in.*;
import com.ecrans.vente.domaine.commande.Categorie;
import com.ecrans.vente.domaine.commun.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.stream.Collectors;
@RestController
public class ReservationController {
 private final ReserverDesPlaces reserver;
 public ReservationController(ReserverDesPlaces reserver) { this.reserver = reserver; }
 @PostMapping("/seances/{seance}/reservations")
 @ResponseStatus(HttpStatus.CREATED)
 public ReservationReponse reserver(@PathVariable String seance, @RequestBody ReservationRequete requete) {
  var billets = requete.billets().stream().map(b -> new DemandeBillet(PlaceId.de(b.place()), Categorie.valueOf(b.categorie()))).toList();
  Reservation r = reserver.executer(new DemandeReservation(new SeanceId(seance), billets));
  return new ReservationReponse(r.commande().valeur(), r.total().centimes(), r.total().toString(), r.urlPaiement(), r.payerAvant());
 }
}

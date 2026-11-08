package com.ecrans.vente.application.service;
import com.ecrans.vente.application.exception.SeanceInconnue;
import com.ecrans.vente.application.port.in.*;
import com.ecrans.vente.application.port.out.*;
import com.ecrans.vente.domaine.commande.*;
import com.ecrans.vente.domaine.commun.*;
import com.ecrans.vente.domaine.evenement.VentePartenaireEnregistree;
import com.ecrans.vente.domaine.seance.Seance;
import java.time.*;
import java.util.stream.Collectors;

public class EnregistrerVentePartenaireService implements EnregistrerVentePartenaire {
 private final Seances seances; private final Commandes commandes; private final GrilleTarifaire grille;
 private final PublicateurEvenements publicateur; private final Clock horloge;
 public EnregistrerVentePartenaireService(Seances s, Commandes c, GrilleTarifaire g, PublicateurEvenements p, Clock h) {
  seances=s; commandes=c; grille=g; publicateur=p; horloge=h;
 }
 public CommandeId executer(VentePartenaire vente) {
  LocalDateTime maintenant=LocalDateTime.now(horloge); CommandeId id=CommandeId.nouvelle();
  var billets=vente.billets().stream().map(b->new Billet(b.place(),new Tarif(b.categorie(),grille.prix(vente.seance(),b.categorie())))).toList();
  Commande commande=Commande.creer(id,vente.seance(),billets,maintenant);
  Seance seance=seances.parId(vente.seance()).orElseThrow(()->new SeanceInconnue(vente.seance()));
  var places=billets.stream().map(Billet::place).collect(Collectors.toSet());
  seance.bloquer(places,id,maintenant);
  seance.vendre(id,places,maintenant);
  commande.payer(maintenant);
  seances.enregistrer(seance); commandes.enregistrer(commande);
  commande.retirerEvenements().forEach(publicateur::publier);
  publicateur.publier(new VentePartenaireEnregistree(id,vente.partenaire(),vente.referencePartenaire(),vente.montantPaye(),maintenant));
  return id;
 }
}

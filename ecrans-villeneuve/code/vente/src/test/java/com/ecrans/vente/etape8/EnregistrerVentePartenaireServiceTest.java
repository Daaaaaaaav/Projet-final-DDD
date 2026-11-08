package com.ecrans.vente.etape8;
import com.ecrans.vente.adapter.out.evenement.PublicateurEnMemoire;
import com.ecrans.vente.adapter.out.memoire.*;
import com.ecrans.vente.adapter.out.tarification.GrilleTarifaireFixe;
import com.ecrans.vente.application.port.in.*;
import com.ecrans.vente.application.service.EnregistrerVentePartenaireService;
import com.ecrans.vente.domaine.commande.Categorie;
import com.ecrans.vente.domaine.commun.*;
import com.ecrans.vente.domaine.evenement.VentePartenaireEnregistree;
import com.ecrans.vente.domaine.seance.Seance;
import org.junit.jupiter.api.Test;
import java.time.*; import java.util.List;
import static org.assertj.core.api.Assertions.*;
class EnregistrerVentePartenaireServiceTest {
 @Test void enregistreUneVenteDejaPayeeEtConserveLeMontantPartenaire() {
  var clock=Clock.fixed(Instant.parse("2026-11-05T12:00:00Z"),ZoneOffset.UTC);
  var seances=new SeancesEnMemoire(); var commandes=new CommandesEnMemoire(); var pub=new PublicateurEnMemoire();
  var id=new SeanceId("S42");
  seances.enregistrer(Seance.ouverte(id,LocalDateTime.of(2026,11,6,20,30),List.of(PlaceId.de("A1"))));
  var service=new EnregistrerVentePartenaireService(seances,commandes,new GrilleTarifaireFixe(),pub,clock);
  var commande=service.executer(new VentePartenaire("SortiesFlash","SF-1",id,List.of(new DemandeBillet(PlaceId.de("A1"),Categorie.PLEIN)),Montant.centimes(999)));
  assertThat(commandes.parId(commande).orElseThrow().statut().name()).isEqualTo("PAYEE");
  assertThat(seances.parId(id).orElseThrow().etat(PlaceId.de("A1")).statut().name()).isEqualTo("VENDUE");
  assertThat(pub.publies()).anySatisfy(e -> { assertThat(e).isInstanceOf(VentePartenaireEnregistree.class); assertThat(((VentePartenaireEnregistree)e).montantPaye().centimes()).isEqualTo(999); });
 }
}

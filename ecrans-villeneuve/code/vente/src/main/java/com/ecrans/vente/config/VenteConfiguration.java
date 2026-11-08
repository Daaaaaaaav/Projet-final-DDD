package com.ecrans.vente.config;

import com.ecrans.vente.application.port.in.*;
import com.ecrans.vente.adapter.in.sortiesflash.*;
import java.time.format.DateTimeFormatter;
import com.ecrans.vente.adapter.in.payvia.WebhookPayVia;
import com.ecrans.vente.domaine.commun.*;
import com.ecrans.vente.domaine.seance.Seance;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.CommandLineRunner;
import java.time.*;
import java.util.*;

@Configuration
public class VenteConfiguration {
 @Bean Clock horloge() { return Clock.systemDefaultZone(); }
 @Bean ApplicationVente applicationVente(Clock horloge) { return new ApplicationVente(horloge); }
 @Bean ReserverDesPlaces reserverDesPlaces(ApplicationVente app) { return app.reserverDesPlaces; }
 @Bean ConfirmerPaiement confirmerPaiement(ApplicationVente app) { return app.confirmerPaiement; }
 @Bean ExpirerBlocages expirerBlocages(ApplicationVente app) { return app.expirerBlocages; }
 @Bean WebhookPayVia webhookPayVia(ApplicationVente app) { return app.webhookPayVia; }
 @Bean EnregistrerVentePartenaire enregistrerVentePartenaire(ApplicationVente app) { return app.enregistrerVentePartenaire; }
 @Bean CorrespondanceSeances correspondanceSeances() { return new CorrespondanceSeances(); }
 @Bean AclSortiesFlash aclSortiesFlash(CorrespondanceSeances c) { return new AclSortiesFlash(c); }
 @Bean CommandLineRunner correspondancePartenaire(CorrespondanceSeances c) {
  return args -> c.associer("VIL-" + LocalDate.now().plusDays(1).format(DateTimeFormatter.BASIC_ISO_DATE) + "-2030-S3", new SeanceId("S42"));
 }
 @Bean CommandLineRunner seanceDeDemonstration(ApplicationVente app) {
  return args -> {
   List<PlaceId> plan = new ArrayList<>();
   for(char rang='A'; rang<='H'; rang++) for(int n=1;n<=12;n++) plan.add(new PlaceId(rang,n));
   app.seances.enregistrer(Seance.ouverte(new SeanceId("S42"), LocalDate.now().plusDays(1).atTime(20,30), plan));
  };
 }
}

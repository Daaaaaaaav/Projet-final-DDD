package com.ecrans.vente.application.port.in;
import com.ecrans.vente.domaine.commun.*;
import java.util.List;
public record VentePartenaire(String partenaire, String referencePartenaire, SeanceId seance,
                              List<DemandeBillet> billets, Montant montantPaye) {
 public VentePartenaire { billets = List.copyOf(billets); }
}

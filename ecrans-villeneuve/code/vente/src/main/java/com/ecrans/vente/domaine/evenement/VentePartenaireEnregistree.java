package com.ecrans.vente.domaine.evenement;
import com.ecrans.vente.domaine.commun.*;
import java.time.LocalDateTime;
public record VentePartenaireEnregistree(CommandeId commande, String partenaire, String referencePartenaire,
                                         Montant montantPaye, LocalDateTime survenuLe) implements EvenementVente {}

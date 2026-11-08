package com.ecrans.vente.application.service;

import com.ecrans.vente.application.port.out.GrilleTarifaire;
import com.ecrans.vente.application.exception.SeanceInconnue;
import com.ecrans.vente.application.port.in.DemandeBillet;
import com.ecrans.vente.application.port.in.DemandeReservation;
import com.ecrans.vente.application.port.in.Reservation;
import com.ecrans.vente.application.port.in.ReserverDesPlaces;
import com.ecrans.vente.application.port.out.Commandes;
import com.ecrans.vente.application.port.out.ConflitDeVersion;
import com.ecrans.vente.application.port.out.Paiements;
import com.ecrans.vente.application.port.out.Seances;
import com.ecrans.vente.domaine.commande.Billet;
import com.ecrans.vente.domaine.commande.Commande;
import com.ecrans.vente.domaine.commande.Tarif;
import com.ecrans.vente.domaine.commun.CommandeId;
import com.ecrans.vente.domaine.commun.PlaceId;
import com.ecrans.vente.domaine.seance.Seance;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Orchestration seulement : charger, faire décider le domaine, enregistrer, puis appeler le PSP.
 *
 * Choix d'agrégats (ADR-0002, option B) : Seance et Commande sont écrits dans la même transaction.
 * L'appel au PSP est fait APRÈS : on ne garde pas une transaction ouverte pendant un appel réseau.
 */
public class ReserverDesPlacesService implements ReserverDesPlaces {

    /** Bonus : nombre d'essais quand une autre vente a modifié la séance en même temps. */
    static final int ESSAIS_MAX = 3;

    private final Seances seances;
    private final Commandes commandes;
    private final GrilleTarifaire grille;
    private final Paiements paiements;
    private final Clock horloge;

    public ReserverDesPlacesService(Seances seances, Commandes commandes, GrilleTarifaire grille,
                                    Paiements paiements, Clock horloge) {
        this.seances = seances;
        this.commandes = commandes;
        this.grille = grille;
        this.paiements = paiements;
        this.horloge = horloge;
    }

    @Override
    public Reservation executer(DemandeReservation demande) {
        LocalDateTime maintenant = LocalDateTime.now(horloge);
        CommandeId id = CommandeId.nouvelle();
        List<Billet> billets = demande.billets().stream()
                .map(b -> new Billet(b.place(), new Tarif(b.categorie(), grille.prix(demande.seance(), b.categorie()))))
                .toList();

        for (int essai = 1; essai <= ESSAIS_MAX; essai++) {
            Seance seance = seances.parId(demande.seance())
                    .orElseThrow(() -> new SeanceInconnue(demande.seance()));
            LocalDateTime payerAvant = seance.bloquer(
                    billets.stream().map(Billet::place).collect(Collectors.toSet()), id, maintenant);
            Commande commande = Commande.creer(id, demande.seance(), billets, payerAvant);
            try {
                seances.enregistrer(seance);
                commandes.enregistrer(commande);
                String url = paiements.demanderPaiement(id, commande.total());
                return new Reservation(id, commande.total(), url, payerAvant);
            } catch (ConflitDeVersion conflit) {
                if (essai == ESSAIS_MAX) throw conflit;
                // Une autre caisse a enregistré entre notre lecture et notre écriture : on recharge.
            }
        }
        throw new IllegalStateException("Nombre maximal de tentatives atteint");
    }
}

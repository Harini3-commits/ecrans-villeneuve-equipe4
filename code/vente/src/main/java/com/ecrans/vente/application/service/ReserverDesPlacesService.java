package com.ecrans.vente.application.service;



import com.ecrans.vente.application.exception.SeanceInconnue;
import com.ecrans.vente.application.port.in.DemandeBillet;
import com.ecrans.vente.application.port.in.DemandeReservation;
import com.ecrans.vente.application.port.in.Reservation;
import com.ecrans.vente.application.port.in.ReserverDesPlaces;
import com.ecrans.vente.application.port.out.Commandes;
import com.ecrans.vente.application.port.out.ConflitDeVersion;
import com.ecrans.vente.application.port.out.GrilleTarifaire;
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
import java.util.LinkedHashSet;
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
        CommandeId commandeId = CommandeId.nouvelle();


        List<Billet> billets = demande.billets().stream()
                .map(demandeBillet -> new Billet(
                        demandeBillet.place(),
                        new Tarif(
                                demandeBillet.categorie(),
                                grille.prix(demande.seance(), demandeBillet.categorie())
                        )
                ))
                .collect(Collectors.toList());


        Seance seance = seances.parId(demande.seance())
                .orElseThrow(() -> new SeanceInconnue(demande.seance()));


        Set<PlaceId> placesDemandees = demande.billets().stream()
                .map(DemandeBillet::place)
                .collect(Collectors.toCollection(LinkedHashSet::new));


        LocalDateTime payerAvant = seance.bloquer(placesDemandees, commandeId, maintenant);


        Commande commande = Commande.creer(commandeId, demande.seance(), billets, payerAvant);


        seances.enregistrer(seance);
        commandes.enregistrer(commande);


        String urlPaiement = paiements.demanderPaiement(commandeId, commande.total());


        return new Reservation(commandeId, commande.total(), urlPaiement, payerAvant);
    }
}
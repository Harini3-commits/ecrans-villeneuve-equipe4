package com.ecrans.vente.application.service;

import com.ecrans.vente.application.exception.SeanceInconnue;
import com.ecrans.vente.application.port.in.DemandeBillet;
import com.ecrans.vente.application.port.in.EnregistrerVentePartenaire;
import com.ecrans.vente.application.port.in.VentePartenaire;
import com.ecrans.vente.application.port.out.Commandes;
import com.ecrans.vente.application.port.out.GrilleTarifaire;
import com.ecrans.vente.application.port.out.PublicateurEvenements;
import com.ecrans.vente.application.port.out.Seances;
import com.ecrans.vente.domaine.commande.Billet;
import com.ecrans.vente.domaine.commande.Commande;
import com.ecrans.vente.domaine.commande.Tarif;
import com.ecrans.vente.domaine.commun.CommandeId;
import com.ecrans.vente.domaine.commun.PlaceId;
import com.ecrans.vente.domaine.evenement.EvenementVente;
import com.ecrans.vente.domaine.evenement.VentePartenaireEnregistree;
import com.ecrans.vente.domaine.seance.Seance;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Enregistre une vente déjà payée chez un partenaire.
 * Le client a déjà payé chez le partenaire : il n'y a donc pas de blocage de dix minutes.
 * La vente respecte néanmoins B1 à B4 : les places doivent être disponibles.
 */
public class EnregistrerVentePartenaireService implements EnregistrerVentePartenaire {

    private final Seances seances;
    private final Commandes commandes;
    private final GrilleTarifaire grille;
    private final PublicateurEvenements publicateur;
    private final Clock horloge;

    public EnregistrerVentePartenaireService(Seances seances, Commandes commandes,
                                             GrilleTarifaire grille,
                                             PublicateurEvenements publicateur,
                                             Clock horloge) {
        this.seances = seances;
        this.commandes = commandes;
        this.grille = grille;
        this.publicateur = publicateur;
        this.horloge = horloge;
    }

    @Override
    public CommandeId executer(VentePartenaire vente) {
        LocalDateTime maintenant = LocalDateTime.now(horloge);
        CommandeId commandeId = CommandeId.nouvelle();

        List<Billet> billets = vente.billets().stream()
                .map(demandeBillet -> new Billet(
                        demandeBillet.place(),
                        new Tarif(
                                demandeBillet.categorie(),
                                grille.prix(vente.seance(), demandeBillet.categorie())
                        )
                ))
                .collect(Collectors.toList());

        Seance seance = seances.parId(vente.seance())
                .orElseThrow(() -> new SeanceInconnue(vente.seance()));

        Set<PlaceId> placesDemandees = vente.billets().stream()
                .map(DemandeBillet::place)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        LocalDateTime finDuBlocage = seance.bloquer(placesDemandees, commandeId, maintenant);

        Commande commande = Commande.creer(commandeId, vente.seance(), billets, finDuBlocage);

        seance.vendre(commandeId, placesDemandees, maintenant);
        commande.payer(maintenant);

        seances.enregistrer(seance);
        commandes.enregistrer(commande);

        for (EvenementVente evenement : commande.retirerEvenements()) {
            publicateur.publier(evenement);
        }

        publicateur.publier(new VentePartenaireEnregistree(
                commandeId,
                vente.partenaire(),
                vente.referencePartenaire(),
                vente.montantPaye(),
                maintenant
        ));

        return commandeId;
    }
}
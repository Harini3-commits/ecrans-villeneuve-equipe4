package com.ecrans.vente.adapter.in.sortiesflash;

import com.ecrans.vente.application.port.in.DemandeBillet;
import com.ecrans.vente.application.port.in.VentePartenaire;
import com.ecrans.vente.domaine.commande.Categorie;
import com.ecrans.vente.domaine.commun.Montant;
import com.ecrans.vente.domaine.commun.PlaceId;
import com.ecrans.vente.domaine.commun.SeanceId;

import java.util.ArrayList;
import java.util.List;

/**
 * ACL SortiesFlash : traduit le format du partenaire dans le langage de la Vente.
 */
public class AclSortiesFlash {

    private final CorrespondanceSeances correspondanceSeances;

    public AclSortiesFlash(CorrespondanceSeances correspondanceSeances) {
        this.correspondanceSeances = correspondanceSeances;
    }

    public VentePartenaire traduire(CommandeSortiesFlash commande) {
        SeanceId seance = correspondanceSeances.traduire(commande.show_ref());

        if (commande.seats().size() != commande.ticket_types().size()) {
            throw new IllegalArgumentException(
                    "Le nombre de places ne correspond pas au nombre de types de billets."
            );
        }

        List<DemandeBillet> billets = new ArrayList<>();

        for (int i = 0; i < commande.seats().size(); i++) {
            billets.add(new DemandeBillet(
                    PlaceId.de(commande.seats().get(i)),
                    traduireCategorie(commande.ticket_types().get(i))
            ));
        }

        return new VentePartenaire(
                "SortiesFlash",
                commande.partner_order_id(),
                seance,
                billets,
                Montant.centimes(commande.paid_amount_cents())
        );
    }

    private Categorie traduireCategorie(String categorieSortiesFlash) {
        return switch (categorieSortiesFlash) {
            case "ADULT" -> Categorie.PLEIN;
            case "STUDENT" -> Categorie.REDUIT;
            case "CHILD" -> Categorie.ENFANT;
            default -> throw new IllegalArgumentException(
                    "Type de billet SortiesFlash inconnu : " + categorieSortiesFlash
            );
        };
    }
}
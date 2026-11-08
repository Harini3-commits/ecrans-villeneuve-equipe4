package com.ecrans.vente.adapter.in.sortiesflash;

import com.ecrans.vente.application.port.in.EnregistrerVentePartenaire;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Point d'entrée HTTP de SortiesFlash.
 * Le contrôleur traduit seulement le JSON en objet ACL, puis délègue au port.
 */
@RestController
@RequestMapping("/partner/v2")
public class SortiesFlashController {

    private final AclSortiesFlash aclSortiesFlash;
    private final EnregistrerVentePartenaire enregistrerVentePartenaire;

    public SortiesFlashController(AclSortiesFlash aclSortiesFlash,
                                  EnregistrerVentePartenaire enregistrerVentePartenaire) {
        this.aclSortiesFlash = aclSortiesFlash;
        this.enregistrerVentePartenaire = enregistrerVentePartenaire;
    }

    @PostMapping("/orders")
    @ResponseStatus(HttpStatus.CREATED)
    public ReponseSortiesFlash enregistrer(@RequestBody CommandeSortiesFlash commande) {
        String commandeId = enregistrerVentePartenaire
                .executer(aclSortiesFlash.traduire(commande))
                .valeur();

        return new ReponseSortiesFlash(
                commande.partner_order_id(),
                commandeId,
                "CONFIRMED"
        );
    }

    public record ReponseSortiesFlash(
            String partner_order_id,
            String cinema_order_id,
            String status
    ) {
    }
}
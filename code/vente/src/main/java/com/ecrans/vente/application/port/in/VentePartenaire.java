package com.ecrans.vente.application.port.in;

import com.ecrans.vente.domaine.commun.Montant;
import com.ecrans.vente.domaine.commun.SeanceId;

import java.util.List;

/**
 * Vente déjà payée chez un partenaire, traduite dans le langage de la Vente.
 */
public record VentePartenaire(
        String partenaire,
        String referencePartenaire,
        SeanceId seance,
        List<DemandeBillet> billets,
        Montant montantPaye
) {
}
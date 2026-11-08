package com.ecrans.vente.domaine.evenement;

import com.ecrans.vente.domaine.commun.CommandeId;
import com.ecrans.vente.domaine.commun.Montant;

import java.time.LocalDateTime;

/**
 * Une vente déjà payée chez un partenaire a été enregistrée.
 * Le montant payé chez le partenaire est conservé pour la déclaration CNC.
 */
public record VentePartenaireEnregistree(
        CommandeId commande,
        String partenaire,
        String referencePartenaire,
        Montant montantPaye,
        LocalDateTime survenuLe
) implements EvenementVente {
}
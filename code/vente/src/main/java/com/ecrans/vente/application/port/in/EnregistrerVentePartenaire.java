package com.ecrans.vente.application.port.in;

import com.ecrans.vente.domaine.commun.CommandeId;

/**
 * Port primaire : enregistre une vente déjà payée chez un partenaire.
 */
public interface EnregistrerVentePartenaire {

    CommandeId executer(VentePartenaire vente);
}
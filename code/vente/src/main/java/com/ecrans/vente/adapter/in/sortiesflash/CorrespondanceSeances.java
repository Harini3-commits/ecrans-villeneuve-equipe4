package com.ecrans.vente.adapter.in.sortiesflash;

import com.ecrans.vente.application.exception.SeanceInconnue;
import com.ecrans.vente.domaine.commun.SeanceId;

import java.util.HashMap;
import java.util.Map;

/**
 * Correspondance entre l'identifiant de séance SortiesFlash et l'identifiant interne.
 * En production, la Programmation alimenterait cette table.
 */
public class CorrespondanceSeances {

    private final Map<String, SeanceId> correspondances = new HashMap<>();

    public void ajouter(String referenceSortiesFlash, SeanceId seanceInterne) {
        correspondances.put(referenceSortiesFlash, seanceInterne);
    }

    public SeanceId traduire(String referenceSortiesFlash) {
        SeanceId seance = correspondances.get(referenceSortiesFlash);

        if (seance == null) {
            throw new SeanceInconnue(new SeanceId(referenceSortiesFlash));
        }

        return seance;
    }
}
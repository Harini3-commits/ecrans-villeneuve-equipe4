package com.ecrans.vente.adapter.in.web;

import com.ecrans.vente.application.port.in.DemandeBillet;
import com.ecrans.vente.application.port.in.DemandeReservation;
import com.ecrans.vente.application.port.in.Reservation;
import com.ecrans.vente.application.port.in.ReserverDesPlaces;
import com.ecrans.vente.domaine.commande.Categorie;
import com.ecrans.vente.domaine.commun.PlaceId;
import com.ecrans.vente.domaine.commun.SeanceId;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/seances")
public class ReservationController {

    private final ReserverDesPlaces reserverDesPlaces;

    public ReservationController(ReserverDesPlaces reserverDesPlaces) {
        this.reserverDesPlaces = reserverDesPlaces;
    }

    @PostMapping("/{seance}/reservations")
    @ResponseStatus(HttpStatus.CREATED)
    public ReponseReservation reserver(@PathVariable String seance, @RequestBody RequeteReservation requete) {
        if (requete == null || requete.billets() == null) {
            throw new IllegalArgumentException("La liste des billets est obligatoire");
        }
        List<DemandeBillet> billets = requete.billets().stream()
                .map(ReservationController::versDemandeBillet)
                .toList();
        Reservation reservation = reserverDesPlaces.executer(
                new DemandeReservation(new SeanceId(seance), billets));
        return new ReponseReservation(reservation.commande().valeur(), reservation.total().centimes(),
                reservation.urlPaiement(), reservation.payerAvant());
    }

    private static DemandeBillet versDemandeBillet(BilletRequete billet) {
        if (billet == null || billet.place() == null || !billet.place().matches("[A-Za-z][0-9]+")
                || billet.categorie() == null) {
            throw new IllegalArgumentException("Billet invalide : une place et une catégorie sont requises");
        }
        return new DemandeBillet(PlaceId.de(billet.place()), Categorie.valueOf(billet.categorie()));
    }

    public record RequeteReservation(List<BilletRequete> billets) {
    }

    public record BilletRequete(String place, String categorie) {
    }

    public record ReponseReservation(String commande, long totalCentimes, String urlPaiement,
                                     LocalDateTime payerAvant) {
    }
}

package com.ecrans.vente.config;

import com.ecrans.vente.adapter.in.payvia.WebhookPayVia;
import com.ecrans.vente.adapter.in.sortiesflash.AclSortiesFlash;
import com.ecrans.vente.adapter.in.sortiesflash.CorrespondanceSeances;
import com.ecrans.vente.adapter.in.sortiesflash.SortiesFlashController;
import com.ecrans.vente.adapter.out.evenement.PublicateurEnMemoire;
import com.ecrans.vente.adapter.out.memoire.CommandesEnMemoire;
import com.ecrans.vente.adapter.out.memoire.SeancesEnMemoire;
import com.ecrans.vente.adapter.out.paiement.PaiementsSimules;
import com.ecrans.vente.adapter.out.tarification.GrilleTarifaireFixe;
import com.ecrans.vente.application.port.in.ConfirmerPaiement;
import com.ecrans.vente.application.port.in.EnregistrerVentePartenaire;
import com.ecrans.vente.application.port.in.ExpirerBlocages;
import com.ecrans.vente.application.port.in.ReserverDesPlaces;
import com.ecrans.vente.application.port.out.Commandes;
import com.ecrans.vente.application.port.out.Paiements;
import com.ecrans.vente.application.port.out.PublicateurEvenements;
import com.ecrans.vente.application.port.out.Seances;
import com.ecrans.vente.application.service.ConfirmerPaiementService;
import com.ecrans.vente.application.service.EnregistrerVentePartenaireService;
import com.ecrans.vente.application.service.ExpirerBlocagesService;
import com.ecrans.vente.application.service.ReserverDesPlacesService;
import com.ecrans.vente.domaine.commun.PlaceId;
import com.ecrans.vente.domaine.commun.SeanceId;
import com.ecrans.vente.domaine.seance.Seance;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class SpringVenteConfiguration {

    @Bean
    Clock horlogeVente() {
        return Clock.systemDefaultZone();
    }

    @Bean
    SeancesEnMemoire seancesEnMemoire() {
        return new SeancesEnMemoire();
    }

    @Bean
    CommandesEnMemoire commandesEnMemoire() {
        return new CommandesEnMemoire();
    }

    @Bean
    GrilleTarifaireFixe grilleTarifaire() {
        return new GrilleTarifaireFixe();
    }

    @Bean
    Paiements paiements() {
        return new PaiementsSimules();
    }

    @Bean
    PublicateurEvenements publicateurEvenements() {
        return new PublicateurEnMemoire();
    }

    @Bean
    ReserverDesPlaces reserverDesPlaces(Seances seances, Commandes commandes, GrilleTarifaireFixe grille,
                                        Paiements paiements, Clock horloge) {
        return new ReserverDesPlacesService(seances, commandes, grille, paiements, horloge);
    }

    @Bean
    ConfirmerPaiement confirmerPaiement(Seances seances, Commandes commandes,
                                        PublicateurEvenements publicateur, Clock horloge) {
        return new ConfirmerPaiementService(seances, commandes, publicateur, horloge);
    }

    @Bean
    ExpirerBlocages expirerBlocages(Seances seances, Commandes commandes,
                                    PublicateurEvenements publicateur, Clock horloge) {
        return new ExpirerBlocagesService(seances, commandes, publicateur, horloge);
    }

    @Bean
    EnregistrerVentePartenaire enregistrerVentePartenaire(Seances seances, Commandes commandes,
                                                           GrilleTarifaireFixe grille,
                                                           PublicateurEvenements publicateur,
                                                           Clock horloge) {
        return new EnregistrerVentePartenaireService(
                seances,
                commandes,
                grille,
                publicateur,
                horloge
        );
    }

    @Bean
    CorrespondanceSeances correspondanceSeances() {
        return new CorrespondanceSeances();
    }

    @Bean
    AclSortiesFlash aclSortiesFlash(CorrespondanceSeances correspondanceSeances) {
        return new AclSortiesFlash(correspondanceSeances);
    }

    @Bean
    SortiesFlashController sortiesFlashController(AclSortiesFlash aclSortiesFlash,
                                                  EnregistrerVentePartenaire enregistrerVentePartenaire) {
        return new SortiesFlashController(aclSortiesFlash, enregistrerVentePartenaire);
    }

    @Bean
    WebhookPayVia webhookPayVia(ConfirmerPaiement confirmerPaiement) {
        return new WebhookPayVia(confirmerPaiement);
    }

    @Bean
    ApplicationRunner seanceDeDemonstration(SeancesEnMemoire seances,
                                            CorrespondanceSeances correspondanceSeances) {
        return args -> {
            List<PlaceId> plan = new ArrayList<>();
            for (char rang = 'A'; rang <= 'G'; rang++) {
                for (int numero = 1; numero <= 10; numero++) {
                    plan.add(new PlaceId(rang, numero));
                }
            }
            seances.enregistrer(Seance.ouverte(new SeanceId("S42"),
                    LocalDate.of(2026, 11, 12).atTime(LocalTime.of(20, 30)), plan));

            LocalDate demain = LocalDate.now().plusDays(1);
            String referenceSortiesFlash = "VIL-"
                    + demain.format(DateTimeFormatter.BASIC_ISO_DATE)
                    + "-2030-S3";

            correspondanceSeances.ajouter(referenceSortiesFlash, new SeanceId("S42"));
        };
    }
}
package com.ecrans.vente.etape4;

import com.ecrans.vente.adapter.out.evenement.PublicateurEnMemoire;
import com.ecrans.vente.adapter.out.memoire.CommandesEnMemoire;
import com.ecrans.vente.application.port.out.Seances;
import com.ecrans.vente.application.service.ConfirmerPaiementService;
import com.ecrans.vente.domaine.commande.Billet;
import com.ecrans.vente.domaine.commande.Categorie;
import com.ecrans.vente.domaine.commande.Commande;
import com.ecrans.vente.domaine.commande.StatutCommande;
import com.ecrans.vente.domaine.commande.Tarif;
import com.ecrans.vente.domaine.commun.CommandeId;
import com.ecrans.vente.domaine.commun.Montant;
import com.ecrans.vente.domaine.commun.PlaceId;
import com.ecrans.vente.domaine.commun.SeanceId;
import com.ecrans.vente.domaine.seance.Seance;
import com.ecrans.vente.application.port.out.Seances;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class ConfirmerPaiementServiceTest {

    @Test
    void uneNotificationDupliqueeNeReconfirmePasLaCommande() {
        CommandeId commandeId = new CommandeId("C-test");
        SeanceId seanceId = new SeanceId("S-test");
        PlaceId place = PlaceId.de("G7");
        Montant montant = Montant.centimes(1150);
        LocalDateTime maintenant = LocalDateTime.of(2026, 11, 12, 20, 9);

        Commande commande = Commande.creer(commandeId, seanceId,
                List.of(new Billet(place, new Tarif(Categorie.PLEIN, montant))),
                maintenant.plusMinutes(3));
        commande.payer(maintenant);
        CommandesEnMemoire commandes = new CommandesEnMemoire();
        commandes.enregistrer(commande);
        Seances seances = new Seances() {
            @Override
            public Optional<Seance> parId(SeanceId id) {
                throw new AssertionError("Une commande déjà payée ne doit pas recharger sa séance");
            }

            @Override
            public List<Seance> enVente() {
                return List.of();
            }

            @Override
            public void enregistrer(Seance seance) {
                throw new AssertionError("Une commande déjà payée ne doit pas modifier sa séance");
            }
        };
        PublicateurEnMemoire publicateur = new PublicateurEnMemoire();
        Clock horloge = Clock.fixed(Instant.parse("2026-11-12T20:09:00Z"), ZoneOffset.UTC);
        ConfirmerPaiementService service = new ConfirmerPaiementService(seances, commandes, publicateur, horloge);

        assertThatCode(() -> service.executer(commandeId)).doesNotThrowAnyException();
        assertThatCode(() -> service.executer(commandeId)).doesNotThrowAnyException();

        assertThat(commandes.parId(commandeId).orElseThrow().statut()).isEqualTo(StatutCommande.PAYEE);
        assertThat(publicateur.publies()).isEmpty();
    }
}

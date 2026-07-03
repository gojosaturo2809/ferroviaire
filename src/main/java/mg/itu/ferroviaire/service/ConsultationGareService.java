package mg.itu.ferroviaire.service;

import mg.itu.ferroviaire.entity.*;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Module 3 - F3.2 : Moteur de requetes temporelles par gare. */
@Service
public class ConsultationGareService {

    private final DonneesFactices donnees;

    public ConsultationGareService(DonneesFactices donnees) {
        this.donnees = donnees;
    }

    public static class FluxGare {
        public final Voyage voyage;
        public final ArretVoyage arret;
        public final Train train;
        public final Trajet trajet;
        public final String typeFlux; // "Depart", "Arrivee", "Transit"

        public FluxGare(Voyage voyage, ArretVoyage arret, Train train, Trajet trajet, String typeFlux) {
            this.voyage = voyage;
            this.arret = arret;
            this.train = train;
            this.trajet = trajet;
            this.typeFlux = typeFlux;
        }
    }

    /** F1.2 : tous les departs prevus et tous les trajets en transit a une gare a partir de l'heure t. */
    public List<FluxGare> getFluxGareAPartirDe(Integer gareId, LocalTime t) {
        List<FluxGare> flux = new ArrayList<>();

        for (Voyage voyage : donnees.getVoyages()) {
            for (ArretVoyage arret : voyage.getArrets()) {
                if (!arret.getGareId().equals(gareId)) continue;

                LocalTime heureRef = arret.getHeureDepart() != null ? arret.getHeureDepart() : arret.getHeureArrivee();
                if (heureRef == null || (t != null && heureRef.isBefore(t))) continue;

                String type;
                boolean estPremier = arret.getOrdreArret() == 1;
                boolean estDernier = arret.getOrdreArret() == voyage.getArrets().size();
                if (estPremier) {
                    type = "Départ";
                } else if (estDernier) {
                    type = "Arrivée";
                } else {
                    type = "Transit";
                }

                Train train = donnees.getTrainParId(voyage.getTrainId()).orElse(null);
                Trajet trajet = donnees.getTrajetParId(voyage.getTrajetId()).orElse(null);
                flux.add(new FluxGare(voyage, arret, train, trajet, type));
            }
        }

        flux.sort(Comparator.comparing(f -> f.arret.getHeureDepart() != null
                ? f.arret.getHeureDepart() : f.arret.getHeureArrivee()));

        return flux;
    }
}

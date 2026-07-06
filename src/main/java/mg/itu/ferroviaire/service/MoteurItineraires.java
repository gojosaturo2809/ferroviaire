package mg.itu.ferroviaire.service;



import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import mg.itu.ferroviaire.entity.ArretVoyage;
import mg.itu.ferroviaire.entity.Gare;
import mg.itu.ferroviaire.entity.PropositionItineraire;
import mg.itu.ferroviaire.entity.SegmentItineraire;
import mg.itu.ferroviaire.entity.Train;
import mg.itu.ferroviaire.entity.Trajet;
import mg.itu.ferroviaire.entity.Voyage;

/**
 * Module 2 - Moteur d'itineraires (F2.1, F2.3).
 * Calcule systematiquement les trajets directs ET les trajets avec escale(s),
 * en validant que le train de correspondance part APRES l'arrivee du precedent.
 */
@Service
public class MoteurItineraires {

    private final DonneesFactices donnees;

    public MoteurItineraires(DonneesFactices donnees) {
        this.donnees = donnees;
    }

    /**
     * Recherche tous les itineraires possibles entre une gare de depart et une gare d'arrivee,
     * a partir d'une heure de depart donnee, avec au maximum une escale (correspondance).
     */
    public List<PropositionItineraire> rechercherItineraires(Integer gareDepartId, Integer gareArriveeId,
                                                               LocalTime heureMin) {
        List<PropositionItineraire> resultats = new ArrayList<>();

        // 1. Trajets directs : un voyage qui dessert a la fois la gare de depart et d'arrivee, dans l'ordre
        for (Voyage voyage : donnees.getVoyages()) {
            ArretVoyage arretDepart = trouverArret(voyage, gareDepartId);
            ArretVoyage arretArrivee = trouverArret(voyage, gareArriveeId);
            if (arretDepart != null && arretArrivee != null
                    && arretDepart.getOrdreArret() < arretArrivee.getOrdreArret()) {
                LocalTime hDep = effectiveDepart(arretDepart);
                if (heureMin == null || !hDep.isBefore(heureMin)) {
                    SegmentItineraire seg = construireSegment(voyage, arretDepart, arretArrivee);
                    resultats.add(new PropositionItineraire(List.of(seg), List.of()));
                }
            }
        }

        // 2. Trajets avec une escale : voyage A (depart -> gare X), puis voyage B (gare X -> arrivee)
        //    avec validation : heure_depart(B, X) > heure_arrivee(A, X)
        for (Voyage voyageA : donnees.getVoyages()) {
            ArretVoyage arretDepartA = trouverArret(voyageA, gareDepartId);
            if (arretDepartA == null) continue;
            LocalTime hDepA = effectiveDepart(arretDepartA);
            if (heureMin != null && hDepA.isBefore(heureMin)) continue;

            for (ArretVoyage arretEscaleA : voyageA.getArrets()) {
                if (arretEscaleA.getOrdreArret() <= arretDepartA.getOrdreArret()) continue;
                if (arretEscaleA.getGareId().equals(gareDepartId)) continue;
                if (arretEscaleA.getHeureArrivee() == null) continue; // terminus du voyage A, pas une escale utile
                Integer gareEscaleId = arretEscaleA.getGareId();
                if (gareEscaleId.equals(gareArriveeId)) continue; // ce serait un trajet direct, deja traite

                for (Voyage voyageB : donnees.getVoyages()) {
                    if (voyageB.getId().equals(voyageA.getId())) continue;
                    ArretVoyage arretEscaleB = trouverArret(voyageB, gareEscaleId);
                    ArretVoyage arretArriveeB = trouverArret(voyageB, gareArriveeId);
                    if (arretEscaleB == null || arretArriveeB == null) continue;
                    if (arretEscaleB.getOrdreArret() >= arretArriveeB.getOrdreArret()) continue;
                    if (arretEscaleB.getHeureDepart() == null) continue;

                    // Validation de la correspondance (regle F2.3) : Train B part apres arrivee Train A
                    if (!arretEscaleB.getHeureDepart().isAfter(arretEscaleA.getHeureArrivee())) continue;

                    Duration attente = Duration.between(arretEscaleA.getHeureArrivee(), arretEscaleB.getHeureDepart());

                    SegmentItineraire segA = construireSegment(voyageA, arretDepartA, arretEscaleA);
                    SegmentItineraire segB = construireSegment(voyageB, arretEscaleB, arretArriveeB);
                    resultats.add(new PropositionItineraire(List.of(segA, segB), List.of(attente)));
                }
            }
        }

        // Tri par heure de depart puis duree totale
        resultats.sort(Comparator
                .comparing((PropositionItineraire p) -> p.getSegments().get(0).getHeureDepart())
                .thenComparing(PropositionItineraire::getDureeTotale));

        return filtrerPropositionsDominees(resultats);
    }

    /**
     * Ecarte les propositions strictement dominees : a meme heure de depart et meme arrivee finale,
     * si une proposition arrive plus tard qu'une autre sans offrir d'avantage, elle est redondante
     * (ex: une escale qui mene au meme train d'arrivee tardif qu'un trajet direct deja propose).
     */
    private List<PropositionItineraire> filtrerPropositionsDominees(List<PropositionItineraire> propositions) {
        List<PropositionItineraire> retenues = new ArrayList<>();
        for (PropositionItineraire candidate : propositions) {
            LocalTime depCandidate = candidate.getSegments().get(0).getHeureDepart();
            LocalTime arrCandidate = candidate.getSegments().get(candidate.getSegments().size() - 1).getHeureArrivee();

            boolean dominee = retenues.stream().anyMatch(existante -> {
                LocalTime depExistante = existante.getSegments().get(0).getHeureDepart();
                LocalTime arrExistante = existante.getSegments().get(existante.getSegments().size() - 1).getHeureArrivee();
                // dominee si une proposition deja retenue part en meme temps ou plus tard,
                // et arrive en meme temps ou plus tot (strictement mieux sur au moins un critere)
                boolean departAuMoinsAussiTard = !depExistante.isBefore(depCandidate);
                boolean arriveeAuMoinsAussiTot = !arrExistante.isAfter(arrCandidate);
                boolean strictementMieux = depExistante.isAfter(depCandidate) || arrExistante.isBefore(arrCandidate);
                return departAuMoinsAussiTard && arriveeAuMoinsAussiTot && strictementMieux;
            });

            if (!dominee) {
                retenues.add(candidate);
            }
        }
        return retenues;
    }

    private LocalTime effectiveDepart(ArretVoyage arret) {
        return arret.getHeureDepart() != null ? arret.getHeureDepart() : arret.getHeureArrivee();
    }

    private ArretVoyage trouverArret(Voyage voyage, Integer gareId) {
        return voyage.getArrets().stream()
                .filter(a -> a.getGareId().equals(gareId))
                .findFirst()
                .orElse(null);
    }

    private SegmentItineraire construireSegment(Voyage voyage, ArretVoyage depart, ArretVoyage arrivee) {
        Gare gareDepart = donnees.getGareParId(depart.getGareId()).orElseThrow();
        Gare gareArrivee = donnees.getGareParId(arrivee.getGareId()).orElseThrow();
        Train train = donnees.getTrainParId(voyage.getTrainId()).orElseThrow();
        Trajet trajet = donnees.getTrajetParId(voyage.getTrajetId()).orElseThrow();

        double distance = Math.abs(gareArrivee.getPkOrdre() - gareDepart.getPkOrdre()); // approx via PK (cf. ST_Length en prod)

        return new SegmentItineraire(
                gareDepart.getId(),
                gareArrivee.getId(),
                gareDepart.getNom(),
                gareArrivee.getNom(),
                effectiveDepart(depart),
                arrivee.getHeureArrivee(),
                train.getMarque(),
                trajet.getNomLigne(),
                distance
        );
    }
}

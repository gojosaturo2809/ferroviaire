package mg.itu.ferroviaire.service;


import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import mg.itu.ferroviaire.entity.ArretVoyage;
import mg.itu.ferroviaire.entity.Gare;
import mg.itu.ferroviaire.entity.SegmentVoie;
import mg.itu.ferroviaire.entity.Train;
import mg.itu.ferroviaire.entity.Trajet;
import mg.itu.ferroviaire.entity.Voyage;

/**
 * Service de donnees factices simulant la base PostGIS du schema fourni.
 * Reseau modelise : ligne FCE Fianarantsoa - Manakara (cote Est de Madagascar),
 * avec coordonnees GPS approximatives reelles pour un rendu Leaflet credible.
 */
@Service
public class DonneesFactices {

    private final List<Gare> gares = new ArrayList<>();
    private final List<Train> trains = new ArrayList<>();
    private final List<SegmentVoie> segments = new ArrayList<>();
    private final List<Trajet> trajets = new ArrayList<>();
    private final List<Voyage> voyages = new ArrayList<>();

    public DonneesFactices() {
        initGares();
        initTrains();
        initSegments();
        initTrajets();
        initVoyages();
    }

    private void initGares() {
        gares.add(new Gare(1, "Fianarantsoa", -21.4530, 47.0857, Gare.Statut.PRINCIPALE, 0));
        gares.add(new Gare(2, "Alakamisy", -21.4870, 47.1920, Gare.Statut.HALTE, 18));
        gares.add(new Gare(3, "Andrambovato", -21.5450, 47.3680, Gare.Statut.HALTE, 42));
        gares.add(new Gare(4, "Sahambavy", -21.4180, 47.1980, Gare.Statut.HALTE, 22));
        gares.add(new Gare(5, "Tolongoina", -21.6230, 47.5680, Gare.Statut.PRINCIPALE, 86));
        gares.add(new Gare(6, "Manampatrana", -21.7390, 47.7340, Gare.Statut.HALTE, 112));
        gares.add(new Gare(7, "Mahabako", -21.9120, 47.9430, Gare.Statut.HALTE, 142));
        gares.add(new Gare(8, "Manakara", -22.1450, 48.0120, Gare.Statut.PRINCIPALE, 163));
    }

    private void initTrains() {
        trains.add(new Train(1, "FCE-101 \"Vohitra\"", 40, Train.TypeTrain.VOYAGEURS));
        trains.add(new Train(2, "FCE-102 \"Namorona\"", 40, Train.TypeTrain.VOYAGEURS));
        trains.add(new Train(3, "FCE-Micheline 7", 35, Train.TypeTrain.MICHELINE));
        trains.add(new Train(4, "FCE-Fret 22", 30, Train.TypeTrain.FRET));
    }

    private void initSegments() {
        segments.add(new SegmentVoie(1, "FCE-01", 1, 2, 18.4,
                trace(-21.4530, 47.0857, -21.4870, 47.1920)));
        segments.add(new SegmentVoie(2, "FCE-02", 2, 3, 24.1,
                trace(-21.4870, 47.1920, -21.5450, 47.3680)));
        segments.add(new SegmentVoie(3, "FCE-03", 3, 5, 44.3,
                trace(-21.5450, 47.3680, -21.6230, 47.5680)));
        segments.add(new SegmentVoie(4, "FCE-04", 5, 6, 26.7,
                trace(-21.6230, 47.5680, -21.7390, 47.7340)));
        segments.add(new SegmentVoie(5, "FCE-05", 6, 7, 30.5,
                trace(-21.7390, 47.7340, -21.9120, 47.9430)));
        segments.add(new SegmentVoie(6, "FCE-06", 7, 8, 21.2,
                trace(-21.9120, 47.9430, -22.1450, 48.0120)));
        segments.add(new SegmentVoie(7, "FCE-S1", 1, 4, 22.0,
                trace(-21.4530, 47.0857, -21.4180, 47.1980)));
    }

    /** Genere un trace avec un leger meandre pour simuler une voie ferree reelle (et non une ligne droite). */
    private List<double[]> trace(double lat1, double lng1, double lat2, double lng2) {
        List<double[]> pts = new ArrayList<>();
        int steps = 6;
        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;
            double lat = lat1 + (lat2 - lat1) * t;
            double lng = lng1 + (lng2 - lng1) * t;
            double meandre = Math.sin(t * Math.PI) * 0.012 * (((lat1 + lng1) % 2 == 0) ? 1 : -1);
            pts.add(new double[]{lat + meandre * 0.4, lng + meandre});
        }
        return pts;
    }

    private void initTrajets() {
        trajets.add(new Trajet(1, "Fianarantsoa - Manakara", 1, 8));
        trajets.add(new Trajet(2, "Manakara - Fianarantsoa", 8, 1));
        trajets.add(new Trajet(3, "Fianarantsoa - Sahambavy", 1, 4));
    }

    private void initVoyages() {
        LocalDate aujourdHui = LocalDate.now();

        // Voyage 1 : train direct complet Fianarantsoa -> Manakara (desservant toutes les gares)
        List<ArretVoyage> arrets1 = new ArrayList<>();
        arrets1.add(new ArretVoyage(1, 1, 1, null, LocalTime.of(7, 0), 1));
        arrets1.add(new ArretVoyage(2, 1, 2, LocalTime.of(7, 35), LocalTime.of(7, 45), 2));
        arrets1.add(new ArretVoyage(3, 1, 3, LocalTime.of(8, 30), LocalTime.of(8, 40), 3));
        arrets1.add(new ArretVoyage(4, 1, 5, LocalTime.of(10, 5), LocalTime.of(10, 25), 4));
        arrets1.add(new ArretVoyage(5, 1, 6, LocalTime.of(11, 15), LocalTime.of(11, 25), 5));
        arrets1.add(new ArretVoyage(6, 1, 7, LocalTime.of(12, 20), LocalTime.of(12, 30), 6));
        arrets1.add(new ArretVoyage(7, 1, 8, LocalTime.of(13, 20), null, 7));
        voyages.add(new Voyage(1, 1, 1, aujourdHui, arrets1));

        // Voyage 2 : micheline rapide Fianarantsoa -> Tolongoina seulement (escale necessaire pour Manakara)
        List<ArretVoyage> arrets2 = new ArrayList<>();
        arrets2.add(new ArretVoyage(8, 2, 1, null, LocalTime.of(6, 30), 1));
        arrets2.add(new ArretVoyage(9, 2, 3, LocalTime.of(7, 50), LocalTime.of(7, 55), 2));
        arrets2.add(new ArretVoyage(10, 2, 5, LocalTime.of(9, 10), null, 3));
        voyages.add(new Voyage(2, 3, 1, aujourdHui, arrets2));

        // Voyage 3 : train Tolongoina -> Manakara, en correspondance avec le voyage 2
        List<ArretVoyage> arrets3 = new ArrayList<>();
        arrets3.add(new ArretVoyage(11, 3, 5, null, LocalTime.of(9, 40), 1));
        arrets3.add(new ArretVoyage(12, 3, 6, LocalTime.of(10, 30), LocalTime.of(10, 40), 2));
        arrets3.add(new ArretVoyage(13, 3, 7, LocalTime.of(11, 35), LocalTime.of(11, 45), 3));
        arrets3.add(new ArretVoyage(14, 3, 8, LocalTime.of(12, 35), null, 4));
        voyages.add(new Voyage(3, 2, 1, aujourdHui, arrets3));

        // Voyage 4 : train du soir, Fianarantsoa -> Manakara, plus lent (autre option directe)
        List<ArretVoyage> arrets4 = new ArrayList<>();
        arrets4.add(new ArretVoyage(15, 4, 1, null, LocalTime.of(13, 0), 1));
        arrets4.add(new ArretVoyage(16, 4, 2, LocalTime.of(13, 40), LocalTime.of(13, 50), 2));
        arrets4.add(new ArretVoyage(17, 4, 3, LocalTime.of(14, 45), LocalTime.of(14, 55), 3));
        arrets4.add(new ArretVoyage(18, 4, 5, LocalTime.of(16, 30), LocalTime.of(16, 50), 4));
        arrets4.add(new ArretVoyage(19, 4, 6, LocalTime.of(17, 45), LocalTime.of(17, 55), 5));
        arrets4.add(new ArretVoyage(20, 4, 7, LocalTime.of(19, 0), LocalTime.of(19, 10), 6));
        arrets4.add(new ArretVoyage(21, 4, 8, LocalTime.of(20, 5), null, 7));
        voyages.add(new Voyage(4, 1, 1, aujourdHui, arrets4));

        // Voyage 5 : navette locale Fianarantsoa <-> Sahambavy (ligne secondaire, theiere)
        List<ArretVoyage> arrets5 = new ArrayList<>();
        arrets5.add(new ArretVoyage(22, 5, 1, null, LocalTime.of(9, 0), 1));
        arrets5.add(new ArretVoyage(23, 5, 4, LocalTime.of(9, 35), null, 2));
        voyages.add(new Voyage(5, 3, 3, aujourdHui, arrets5));
    }

    public List<Gare> getGares() {
        return gares;
    }

    public Optional<Gare> getGareParId(Integer id) {
        return gares.stream().filter(g -> g.getId().equals(id)).findFirst();
    }

    public List<Train> getTrains() {
        return trains;
    }

    public Optional<Train> getTrainParId(Integer id) {
        return trains.stream().filter(t -> t.getId().equals(id)).findFirst();
    }

    public List<SegmentVoie> getSegments() {
        return segments;
    }

    public List<Trajet> getTrajets() {
        return trajets;
    }

    public Optional<Trajet> getTrajetParId(Integer id) {
        return trajets.stream().filter(t -> t.getId().equals(id)).findFirst();
    }

    public List<Voyage> getVoyages() {
        return voyages;
    }

    public Optional<Voyage> getVoyageParId(Integer id) {
        return voyages.stream().filter(v -> v.getId().equals(id)).findFirst();
    }
}

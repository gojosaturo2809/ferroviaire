package mg.itu.ferroviaire.entity;

import java.time.Duration;
import java.util.List;

/** Une proposition d'itineraire complete : soit direct (1 segment), soit avec escale(s) (N segments). */
public class PropositionItineraire {

    private List<SegmentItineraire> segments;
    private List<Duration> tempsAttenteEscales; // taille = segments.size() - 1

    public PropositionItineraire(List<SegmentItineraire> segments, List<Duration> tempsAttenteEscales) {
        this.segments = segments;
        this.tempsAttenteEscales = tempsAttenteEscales;
    }

    public List<SegmentItineraire> getSegments() {
        return segments;
    }

    public List<Duration> getTempsAttenteEscales() {
        return tempsAttenteEscales;
    }

    public boolean isDirect() {
        return segments.size() == 1;
    }

    public int getNombreEscales() {
        return segments.size() - 1;
    }

    public double getDistanceTotaleKm() {
        return segments.stream().mapToDouble(SegmentItineraire::getDistanceKm).sum();
    }

    public Duration getDureeTotale() {
        return Duration.between(segments.get(0).getHeureDepart(), segments.get(segments.size() - 1).getHeureArrivee());
    }

    public String getDureeTotaleFormatee() {
        return formaterDuree(getDureeTotale());
    }

    public static String formaterDuree(Duration d) {
        long h = d.toHours();
        long m = d.toMinutesPart();
        if (h == 0) {
            return m + " min";
        }
        return h + " h " + (m > 0 ? m + " min" : "");
    }
}

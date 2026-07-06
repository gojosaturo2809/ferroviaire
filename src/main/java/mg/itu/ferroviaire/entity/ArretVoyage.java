package mg.itu.ferroviaire.entity;

import java.time.Duration;
import java.time.LocalTime;

public class ArretVoyage {

    private Integer id;
    private Integer voyageId;
    private Integer gareId;
    private LocalTime heureArrivee;
    private LocalTime heureDepart;
    private int ordreArret;

    public ArretVoyage(Integer id, Integer voyageId, Integer gareId, LocalTime heureArrivee,
                        LocalTime heureDepart, int ordreArret) {
        this.id = id;
        this.voyageId = voyageId;
        this.gareId = gareId;
        this.heureArrivee = heureArrivee;
        this.heureDepart = heureDepart;
        this.ordreArret = ordreArret;
    }

    public Integer getId() {
        return id;
    }

    public Integer getVoyageId() {
        return voyageId;
    }

    public Integer getGareId() {
        return gareId;
    }

    public LocalTime getHeureArrivee() {
        return heureArrivee;
    }

    public LocalTime getHeureDepart() {
        return heureDepart;
    }

    public int getOrdreArret() {
        return ordreArret;
    }

    /** Duree de pause en gare = heure_depart - heure_arrivee (F3.1 / F1.3) */
    public Duration getDureeArret() {
        if (heureArrivee == null || heureDepart == null) {
            return Duration.ZERO;
        }
        return Duration.between(heureArrivee, heureDepart);
    }

    public String getDureeArretFormatee() {
        Duration d = getDureeArret();
        long min = d.toMinutes();
        return min <= 0 ? "Sans arrêt" : min + " min";
    }
}

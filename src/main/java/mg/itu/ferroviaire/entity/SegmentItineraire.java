package mg.itu.ferroviaire.entity;

import java.time.Duration;
import java.time.LocalTime;

/** Represente l'utilisation d'un voyage (un train) sur une portion de l'itineraire de l'usager. */
public class SegmentItineraire {

    private int gareDepartId;
    private int gareArriveeId;
    private String gareDepartNom;
    private String gareArriveeNom;
    private LocalTime heureDepart;
    private LocalTime heureArrivee;
    private String trainMarque;
    private String nomLigne;
    private double distanceKm;

    public SegmentItineraire(int gareDepartId, int gareArriveeId, String gareDepartNom, String gareArriveeNom,
                              LocalTime heureDepart, LocalTime heureArrivee, String trainMarque, String nomLigne,
                              double distanceKm) {
        this.gareDepartId = gareDepartId;
        this.gareArriveeId = gareArriveeId;
        this.gareDepartNom = gareDepartNom;
        this.gareArriveeNom = gareArriveeNom;
        this.heureDepart = heureDepart;
        this.heureArrivee = heureArrivee;
        this.trainMarque = trainMarque;
        this.nomLigne = nomLigne;
        this.distanceKm = distanceKm;
    }

    public int getGareDepartId() {
        return gareDepartId;
    }

    public int getGareArriveeId() {
        return gareArriveeId;
    }

    public String getGareDepartNom() {
        return gareDepartNom;
    }

    public String getGareArriveeNom() {
        return gareArriveeNom;
    }

    public LocalTime getHeureDepart() {
        return heureDepart;
    }

    public LocalTime getHeureArrivee() {
        return heureArrivee;
    }

    public String getTrainMarque() {
        return trainMarque;
    }

    public String getNomLigne() {
        return nomLigne;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public Duration getDureeTrajet() {
        return Duration.between(heureDepart, heureArrivee);
    }
}

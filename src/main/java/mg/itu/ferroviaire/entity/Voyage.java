package mg.itu.ferroviaire.entity;

import java.time.LocalDate;
import java.util.List;

public class Voyage {

    private Integer id;
    private Integer trainId;
    private Integer trajetId;
    private LocalDate dateVoyage;
    private List<ArretVoyage> arrets;

    public Voyage(Integer id, Integer trainId, Integer trajetId, LocalDate dateVoyage, List<ArretVoyage> arrets) {
        this.id = id;
        this.trainId = trainId;
        this.trajetId = trajetId;
        this.dateVoyage = dateVoyage;
        this.arrets = arrets;
    }

    public Integer getId() {
        return id;
    }

    public Integer getTrainId() {
        return trainId;
    }

    public Integer getTrajetId() {
        return trajetId;
    }

    public LocalDate getDateVoyage() {
        return dateVoyage;
    }

    public List<ArretVoyage> getArrets() {
        return arrets;
    }
}

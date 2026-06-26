package mg.itu.ferroviaire.entity;


import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "voyage")
public class Voyage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "train_id")
    private Train train;

    @Column(name = "heure_de_depart")
    private LocalDateTime heureDeDepart;

    @ManyToOne
    @JoinColumn(name = "gare_depart_id")
    private Gare gareDepart;

    @ManyToOne
    @JoinColumn(name = "gare_arrive_id")
    private Gare gareArrive;

    public Voyage() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Train getTrain() {
        return train;
    }

    public void setTrain(Train train) {
        this.train = train;
    }

    public LocalDateTime getHeureDeDepart() {
        return heureDeDepart;
    }

    public void setHeureDeDepart(LocalDateTime heureDeDepart) {
        this.heureDeDepart = heureDeDepart;
    }

    public Gare getGareDepart() {
        return gareDepart;
    }

    public void setGareDepart(Gare gareDepart) {
        this.gareDepart = gareDepart;
    }

    public Gare getGareArrive() {
        return gareArrive;
    }

    public void setGareArrive(Gare gareArrive) {
        this.gareArrive = gareArrive;
    }
}
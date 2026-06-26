package mg.itu.ferroviaire.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "trajet")
public class Trajet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "gare_depart_id")
    private Gare gareDepart;

    @ManyToOne
    @JoinColumn(name = "gare_arrive_id")
    private Gare gareArrive;

    public Trajet() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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
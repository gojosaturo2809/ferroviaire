package mg.itu.ferroviaire.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "voyage_detail")
public class VoyageDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "voyage_id")
    private Voyage voyage;

    @ManyToOne
    @JoinColumn(name = "trajet_id")
    private Trajet trajet;

    public VoyageDetail() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Voyage getVoyage() {
        return voyage;
    }

    public void setVoyage(Voyage voyage) {
        this.voyage = voyage;
    }

    public Trajet getTrajet() {
        return trajet;
    }

    public void setTrajet(Trajet trajet) {
        this.trajet = trajet;
    }
}
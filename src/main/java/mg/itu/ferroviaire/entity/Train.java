package mg.itu.ferroviaire.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "train")
public class Train {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String marque;

    @Column(nullable = false)
    private Integer vitesse;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_train", nullable = false)
    private TypeTrain typeTrain;

    public Train() {
    }

    public Train(Integer id, String marque, Integer vitesse, TypeTrain typeTrain) {
        this.id = id;
        this.marque = marque;
        this.vitesse = vitesse;
        this.typeTrain = typeTrain;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMarque() {
        return marque;
    }

    public void setMarque(String marque) {
        this.marque = marque;
    }

    public Integer getVitesse() {
        return vitesse;
    }

    public void setVitesse(Integer vitesse) {
        this.vitesse = vitesse;
    }

    public TypeTrain getTypeTrain() {
        return typeTrain;
    }

    public void setTypeTrain(TypeTrain typeTrain) {
        this.typeTrain = typeTrain;
    }
}
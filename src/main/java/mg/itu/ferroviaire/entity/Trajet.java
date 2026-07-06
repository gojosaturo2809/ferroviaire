package mg.itu.ferroviaire.entity;

public class Trajet {

    private Integer id;
    private String nomLigne;
    private Integer gareOrigineId;
    private Integer gareTerminusId;

    public Trajet(Integer id, String nomLigne, Integer gareOrigineId, Integer gareTerminusId) {
        this.id = id;
        this.nomLigne = nomLigne;
        this.gareOrigineId = gareOrigineId;
        this.gareTerminusId = gareTerminusId;
    }

    public Integer getId() {
        return id;
    }

    public String getNomLigne() {
        return nomLigne;
    }

    public Integer getGareOrigineId() {
        return gareOrigineId;
    }

    public Integer getGareTerminusId() {
        return gareTerminusId;
    }
}

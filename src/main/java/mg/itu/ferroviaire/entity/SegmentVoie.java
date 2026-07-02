package mg.itu.ferroviaire.entity;

import java.util.List;

public class SegmentVoie {

    private Integer id;
    private String codeSegment;
    private Integer gareDepartId;
    private Integer gareArriveeId;
    private double longueurKm;
    private List<double[]> trace; // points [lat, lng] formant la LINESTRING reelle

    public SegmentVoie(Integer id, String codeSegment, Integer gareDepartId, Integer gareArriveeId,
                        double longueurKm, List<double[]> trace) {
        this.id = id;
        this.codeSegment = codeSegment;
        this.gareDepartId = gareDepartId;
        this.gareArriveeId = gareArriveeId;
        this.longueurKm = longueurKm;
        this.trace = trace;
    }

    public Integer getId() {
        return id;
    }

    public String getCodeSegment() {
        return codeSegment;
    }

    public Integer getGareDepartId() {
        return gareDepartId;
    }

    public Integer getGareArriveeId() {
        return gareArriveeId;
    }

    public double getLongueurKm() {
        return longueurKm;
    }

    public List<double[]> getTrace() {
        return trace;
    }
}

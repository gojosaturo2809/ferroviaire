package mg.itu.ferroviaire.entity;

public class Train {

    public enum TypeTrain {
        VOYAGEURS("Voyageurs"),
        FRET("Fret"),
        MICHELINE("Micheline");

        private final String libelle;

        TypeTrain(String libelle) {
            this.libelle = libelle;
        }

        public String getLibelle() {
            return libelle;
        }
    }

    private Integer id;
    private String marque;
    private double vitesseMax;
    private TypeTrain typeTrain;

    public Train(Integer id, String marque, double vitesseMax, TypeTrain typeTrain) {
        this.id = id;
        this.marque = marque;
        this.vitesseMax = vitesseMax;
        this.typeTrain = typeTrain;
    }

    public Integer getId() {
        return id;
    }

    public String getMarque() {
        return marque;
    }

    public double getVitesseMax() {
        return vitesseMax;
    }

    public TypeTrain getTypeTrain() {
        return typeTrain;
    }
}

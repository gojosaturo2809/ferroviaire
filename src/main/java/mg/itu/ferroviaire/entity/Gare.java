package mg.itu.ferroviaire.entity;

public class Gare {

    public enum Statut {
        PRINCIPALE("Gare principale"),
        HALTE("Halte"),
        TRI("Gare de triage");

        private final String libelle;

        Statut(String libelle) {
            this.libelle = libelle;
        }

        public String getLibelle() {
            return libelle;
        }
    }

    private Integer id;
    private String nom;
    private double latitude;
    private double longitude;
    private Statut statut;
    private double pkOrdre; // position kilometrique le long de la ligne, pour l'ordre d'affichage

    public Gare(Integer id, String nom, double latitude, double longitude, Statut statut, double pkOrdre) {
        this.id = id;
        this.nom = nom;
        this.latitude = latitude;
        this.longitude = longitude;
        this.statut = statut;
        this.pkOrdre = pkOrdre;
    }

    public Integer getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public Statut getStatut() {
        return statut;
    }

    public double getPkOrdre() {
        return pkOrdre;
    }
}

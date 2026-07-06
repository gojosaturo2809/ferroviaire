package mg.itu.ferroviaire.repository;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import mg.itu.ferroviaire.entity.ArretVoyage;
import mg.itu.ferroviaire.entity.Voyage;

@Repository
public class VoyageRepository {

    private final JdbcTemplate jdbc;

    public VoyageRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<ArretVoyage> ARRET_MAPPER = (rs, rowNum) -> new ArretVoyage(
            rs.getInt("id"),
            rs.getInt("voyage_id"),
            rs.getInt("gare_id"),
            toLocalTime(rs.getTime("heure_arrivee")),
            toLocalTime(rs.getTime("heure_depart")),
            rs.getInt("ordre_arret")
    );

    private static LocalTime toLocalTime(Time t) {
        return t != null ? t.toLocalTime() : null;
    }

    private List<ArretVoyage> findArretsByVoyageId(Integer voyageId) {
        return jdbc.query(
                "SELECT id, voyage_id, gare_id, heure_arrivee, heure_depart, ordre_arret " +
                        "FROM arret_voyage WHERE voyage_id = ? ORDER BY ordre_arret",
                ARRET_MAPPER, voyageId);
    }

    private Voyage mapVoyage(Integer id, Integer trainId, Integer trajetId, LocalDate date) {
        return new Voyage(id, trainId, trajetId, date, findArretsByVoyageId(id));
    }

    public List<Voyage> findAll() {
        return jdbc.query("SELECT id, train_id, trajet_id, date_voyage FROM voyage", (rs, rowNum) ->
                mapVoyage(rs.getInt("id"), rs.getInt("train_id"), rs.getInt("trajet_id"),
                        rs.getDate("date_voyage").toLocalDate()));
    }

    public Optional<Voyage> findById(Integer id) {
        List<Voyage> res = jdbc.query(
                "SELECT id, train_id, trajet_id, date_voyage FROM voyage WHERE id = ?",
                (rs, rowNum) -> mapVoyage(rs.getInt("id"), rs.getInt("train_id"), rs.getInt("trajet_id"),
                        rs.getDate("date_voyage").toLocalDate()),
                id);
        return res.stream().findFirst();
    }
}

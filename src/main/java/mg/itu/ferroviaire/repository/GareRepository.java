package mg.itu.ferroviaire.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import mg.itu.ferroviaire.entity.Gare;

@Repository
public class GareRepository {

    private final JdbcTemplate jdbc;

    public GareRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final String SELECT_BASE =
            "SELECT id, nom, ST_Y(point) AS lat, ST_X(point) AS lng, statut, pk_ordre FROM gare";

    private static final RowMapper<Gare> MAPPER = (rs, rowNum) -> new Gare(
            rs.getInt("id"),
            rs.getString("nom"),
            rs.getDouble("lat"),
            rs.getDouble("lng"),
            Gare.Statut.valueOf(rs.getString("statut")),
            rs.getDouble("pk_ordre")
    );

    public List<Gare> findAll() {
        return jdbc.query(SELECT_BASE + " ORDER BY pk_ordre", MAPPER);
    }

    public Optional<Gare> findById(Integer id) {
        List<Gare> res = jdbc.query(SELECT_BASE + " WHERE id = ?", MAPPER, id);
        return res.stream().findFirst();
    }
}

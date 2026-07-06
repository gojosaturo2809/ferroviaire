package mg.itu.ferroviaire.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import mg.itu.ferroviaire.entity.Trajet;

@Repository
public class TrajetRepository {

    private final JdbcTemplate jdbc;

    public TrajetRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final String SELECT_BASE =
            "SELECT id, nom_ligne, gare_depart_id, gare_arrive_id FROM trajet";

    private static final RowMapper<Trajet> MAPPER = (rs, rowNum) -> new Trajet(
            rs.getInt("id"),
            rs.getString("nom_ligne"),
            rs.getInt("gare_depart_id"),
            rs.getInt("gare_arrive_id")
    );

    public List<Trajet> findAll() {
        return jdbc.query(SELECT_BASE, MAPPER);
    }

    public Optional<Trajet> findById(Integer id) {
        List<Trajet> res = jdbc.query(SELECT_BASE + " WHERE id = ?", MAPPER, id);
        return res.stream().findFirst();
    }
}

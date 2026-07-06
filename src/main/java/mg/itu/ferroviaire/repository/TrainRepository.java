package mg.itu.ferroviaire.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import mg.itu.ferroviaire.entity.Train;

@Repository
public class TrainRepository {

    private final JdbcTemplate jdbc;

    public TrainRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final String SELECT_BASE =
            "SELECT id, marque, vitesse, type_train FROM train";

    private static final RowMapper<Train> MAPPER = (rs, rowNum) -> new Train(
            rs.getInt("id"),
            rs.getString("marque"),
            rs.getDouble("vitesse"),
            Train.TypeTrain.valueOf(rs.getString("type_train"))
    );

    public List<Train> findAll() {
        return jdbc.query(SELECT_BASE, MAPPER);
    }

    public Optional<Train> findById(Integer id) {
        List<Train> res = jdbc.query(SELECT_BASE + " WHERE id = ?", MAPPER, id);
        return res.stream().findFirst();
    }
}

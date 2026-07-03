package mg.itu.ferroviaire.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import mg.itu.ferroviaire.entity.SegmentVoie;

@Repository
public class SegmentVoieRepository {

    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper = new ObjectMapper();

    public SegmentVoieRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<SegmentVoie> findAll() {
        return jdbc.query(
                "SELECT id, code_segment, gare_depart_id, gare_arrivee_id, longueur_km, " +
                        "ST_AsGeoJSON(trace) AS trace_geojson FROM segment_voie",
                (rs, rowNum) -> new SegmentVoie(
                        rs.getInt("id"),
                        rs.getString("code_segment"),
                        rs.getInt("gare_depart_id"),
                        rs.getInt("gare_arrivee_id"),
                        rs.getDouble("longueur_km"),
                        parseTrace(rs.getString("trace_geojson"))
                ));
    }

    /** GeoJSON LineString coordinates = [lng, lat] ; l'entite attend [lat, lng]. */
    private List<double[]> parseTrace(String geojson) {
        List<double[]> pts = new ArrayList<>();
        try {
            JsonNode coords = mapper.readTree(geojson).get("coordinates");
            for (JsonNode c : coords) {
                pts.add(new double[]{c.get(1).asDouble(), c.get(0).asDouble()});
            }
        } catch (Exception e) {
            throw new IllegalStateException("Trace GeoJSON invalide", e);
        }
        return pts;
    }
}

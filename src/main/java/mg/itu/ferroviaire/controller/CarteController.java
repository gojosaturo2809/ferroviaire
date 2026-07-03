package mg.itu.ferroviaire.controller;


import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import mg.itu.ferroviaire.entity.ArretVoyage;
import mg.itu.ferroviaire.entity.Gare;
import mg.itu.ferroviaire.entity.SegmentVoie;
import mg.itu.ferroviaire.entity.Train;
import mg.itu.ferroviaire.entity.Trajet;
import mg.itu.ferroviaire.entity.Voyage;
import mg.itu.ferroviaire.service.ConsultationGareService;
import mg.itu.ferroviaire.service.DonneesFactices;

@Controller
public class CarteController {

    private final DonneesFactices donnees;
    private final ConsultationGareService consultationGareService;

    public CarteController(DonneesFactices donnees, ConsultationGareService consultationGareService) {
        this.donnees = donnees;
        this.consultationGareService = consultationGareService;
    }

    @GetMapping("/carte")
    public String afficherCarte(Model model) {
        model.addAttribute("gares", donnees.getGares());
        return "carte";
    }

    /** API JSON consommee par Leaflet.js pour dessiner gares + segments de voie (F1.1). */
    @GetMapping("/api/reseau")
    @ResponseBody
    public Map<String, Object> getReseau() {
        List<Map<String, Object>> garesJson = new ArrayList<>();
        for (Gare g : donnees.getGares()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", g.getId());
            m.put("nom", g.getNom());
            m.put("lat", g.getLatitude());
            m.put("lng", g.getLongitude());
            m.put("statut", g.getStatut().name());
            m.put("statutLibelle", g.getStatut().getLibelle());
            garesJson.add(m);
        }

        List<Map<String, Object>> segmentsJson = new ArrayList<>();
        for (SegmentVoie s : donnees.getSegments()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", s.getId());
            m.put("code", s.getCodeSegment());
            m.put("longueurKm", s.getLongueurKm());
            m.put("trace", s.getTrace());
            segmentsJson.add(m);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("gares", garesJson);
        result.put("segments", segmentsJson);
        return result;
    }

    /** F1.2 : consultation d'une gare a un temps t -> departs + transits (en AJAX, appele depuis la carte). */
    @GetMapping("/api/gare/{id}/flux")
    @ResponseBody
    public Map<String, Object> getFluxGare(@PathVariable Integer id,
                                            @RequestParam(required = false) String heure) {
        LocalTime t = (heure != null && !heure.isBlank()) ? LocalTime.parse(heure) : null;
        List<ConsultationGareService.FluxGare> flux = consultationGareService.getFluxGareAPartirDe(id, t);

        Gare gare = donnees.getGareParId(id).orElse(null);

        List<Map<String, Object>> fluxJson = new ArrayList<>();
        for (ConsultationGareService.FluxGare f : flux) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("type", f.typeFlux);
            m.put("trainMarque", f.train != null ? f.train.getMarque() : "—");
            m.put("nomLigne", f.trajet != null ? f.trajet.getNomLigne() : "—");
            m.put("heureArrivee", f.arret.getHeureArrivee() != null ? f.arret.getHeureArrivee().toString() : null);
            m.put("heureDepart", f.arret.getHeureDepart() != null ? f.arret.getHeureDepart().toString() : null);
            m.put("dureeArret", f.arret.getDureeArretFormatee());
            fluxJson.add(m);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("gareNom", gare != null ? gare.getNom() : "Inconnue");
        result.put("heureReference", t != null ? t.toString() : null);
        result.put("flux", fluxJson);
        return result;
    }

    /** F1.3 : habillage temporel complet d'un voyage selectionne, pour affichage sur la carte. */
    @GetMapping("/api/voyage/{id}/habillage")
    @ResponseBody
    public Map<String, Object> getHabillageVoyage(@PathVariable Integer id) {
        Voyage voyage = donnees.getVoyageParId(id).orElse(null);
        if (voyage == null) {
            return Map.of("erreur", "Voyage introuvable");
        }
        Train train = donnees.getTrainParId(voyage.getTrainId()).orElse(null);
        Trajet trajet = donnees.getTrajetParId(voyage.getTrajetId()).orElse(null);

        List<Map<String, Object>> arretsJson = new ArrayList<>();
        for (ArretVoyage a : voyage.getArrets()) {
            Gare g = donnees.getGareParId(a.getGareId()).orElse(null);
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("gareId", a.getGareId());
            m.put("gareNom", g != null ? g.getNom() : "—");
            m.put("lat", g != null ? g.getLatitude() : null);
            m.put("lng", g != null ? g.getLongitude() : null);
            m.put("heureArrivee", a.getHeureArrivee() != null ? a.getHeureArrivee().toString() : null);
            m.put("heureDepart", a.getHeureDepart() != null ? a.getHeureDepart().toString() : null);
            m.put("dureeArret", a.getDureeArretFormatee());
            arretsJson.add(m);
        }

        // Duree par parcelle entre arrets consecutifs (F3.1)
        List<Map<String, Object>> parcellesJson = new ArrayList<>();
        for (int i = 0; i < voyage.getArrets().size() - 1; i++) {
            ArretVoyage actuel = voyage.getArrets().get(i);
            ArretVoyage suivant = voyage.getArrets().get(i + 1);
            LocalTime depart = actuel.getHeureDepart() != null ? actuel.getHeureDepart() : actuel.getHeureArrivee();
            LocalTime arrivee = suivant.getHeureArrivee();
            long minutes = java.time.Duration.between(depart, arrivee).toMinutes();

            Map<String, Object> m = new LinkedHashMap<>();
            m.put("gareDepartId", actuel.getGareId());
            m.put("gareArriveeId", suivant.getGareId());
            m.put("dureeMinutes", minutes);
            m.put("dureeLibelle", "Durée de la parcelle : " + minutes + " min");
            parcellesJson.add(m);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("voyageId", voyage.getId());
        result.put("trainMarque", train != null ? train.getMarque() : "—");
        result.put("nomLigne", trajet != null ? trajet.getNomLigne() : "—");
        result.put("arrets", arretsJson);
        result.put("parcelles", parcellesJson);
        return result;
    }

    /** Liste des voyages disponibles, pour le selecteur "afficher un voyage sur la carte". */
    @GetMapping("/api/voyages")
    @ResponseBody
    public List<Map<String, Object>> getVoyages() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Voyage v : donnees.getVoyages()) {
            Train train = donnees.getTrainParId(v.getTrainId()).orElse(null);
            Trajet trajet = donnees.getTrajetParId(v.getTrajetId()).orElse(null);
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", v.getId());
            m.put("label", (train != null ? train.getMarque() : "Train") + " — "
                    + (trajet != null ? trajet.getNomLigne() : ""));
            result.add(m);
        }
        return result;
    }
}

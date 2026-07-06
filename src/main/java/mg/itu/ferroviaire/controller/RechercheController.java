package mg.itu.ferroviaire.controller;


import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import mg.itu.ferroviaire.entity.Gare;
import mg.itu.ferroviaire.entity.PropositionItineraire;
import mg.itu.ferroviaire.service.DonneesFactices;
import mg.itu.ferroviaire.service.MoteurItineraires;

@Controller
public class RechercheController {

    private final DonneesFactices donnees;
    private final MoteurItineraires moteurItineraires;

    public RechercheController(DonneesFactices donnees, MoteurItineraires moteurItineraires) {
        this.donnees = donnees;
        this.moteurItineraires = moteurItineraires;
    }

    @GetMapping("/recherche")
    public String afficherRecherche(
            @RequestParam(required = false) Integer gareDepart,
            @RequestParam(required = false) Integer gareArrivee,
            @RequestParam(required = false) String heureDepart,
            Model model) {

        List<Gare> gares = donnees.getGares();
        model.addAttribute("gares", gares);
        model.addAttribute("gareDepartSel", gareDepart);
        model.addAttribute("gareArriveeSel", gareArrivee);
        model.addAttribute("heureDepartSel", heureDepart);

        boolean rechercheEffectuee = gareDepart != null && gareArrivee != null;
        model.addAttribute("rechercheEffectuee", rechercheEffectuee);

        if (rechercheEffectuee) {
            if (gareDepart.equals(gareArrivee)) {
                model.addAttribute("erreur", "La gare de départ et la gare d'arrivée doivent être différentes.");
            } else {
                LocalTime t = null;
                if (heureDepart != null && !heureDepart.isBlank()) {
                    try {
                        t = LocalTime.parse(heureDepart);
                    } catch (DateTimeParseException ignored) {
                        // heure mal formee -> on ignore le filtre
                    }
                }
                List<PropositionItineraire> propositions = moteurItineraires.rechercherItineraires(gareDepart, gareArrivee, t);

                List<PropositionItineraire> directs = propositions.stream().filter(PropositionItineraire::isDirect).toList();
                List<PropositionItineraire> avecEscale = propositions.stream().filter(p -> !p.isDirect()).toList();

                model.addAttribute("propositionsDirectes", directs);
                model.addAttribute("propositionsEscale", avecEscale);
                model.addAttribute("aucunResultat", propositions.isEmpty());
            }
        }

        return "recherche";
    }
}

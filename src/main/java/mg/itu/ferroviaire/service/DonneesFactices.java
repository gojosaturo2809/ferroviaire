package mg.itu.ferroviaire.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import mg.itu.ferroviaire.entity.Gare;
import mg.itu.ferroviaire.entity.SegmentVoie;
import mg.itu.ferroviaire.entity.Train;
import mg.itu.ferroviaire.entity.Trajet;
import mg.itu.ferroviaire.entity.Voyage;
import mg.itu.ferroviaire.repository.GareRepository;
import mg.itu.ferroviaire.repository.SegmentVoieRepository;
import mg.itu.ferroviaire.repository.TrainRepository;
import mg.itu.ferroviaire.repository.TrajetRepository;
import mg.itu.ferroviaire.repository.VoyageRepository;

/**
 * Facade d'acces aux donnees du reseau ferroviaire, desormais lues depuis PostgreSQL/PostGIS
 * (anciennement donnees factices en memoire). API publique inchangee pour ne pas impacter
 * les services/controllers consommateurs.
 */
@Service
public class DonneesFactices {

    private final GareRepository gareRepository;
    private final TrainRepository trainRepository;
    private final TrajetRepository trajetRepository;
    private final VoyageRepository voyageRepository;
    private final SegmentVoieRepository segmentVoieRepository;

    public DonneesFactices(GareRepository gareRepository, TrainRepository trainRepository,
                            TrajetRepository trajetRepository, VoyageRepository voyageRepository,
                            SegmentVoieRepository segmentVoieRepository) {
        this.gareRepository = gareRepository;
        this.trainRepository = trainRepository;
        this.trajetRepository = trajetRepository;
        this.voyageRepository = voyageRepository;
        this.segmentVoieRepository = segmentVoieRepository;
    }

    public List<Gare> getGares() {
        return gareRepository.findAll();
    }

    public Optional<Gare> getGareParId(Integer id) {
        return gareRepository.findById(id);
    }

    public List<Train> getTrains() {
        return trainRepository.findAll();
    }

    public Optional<Train> getTrainParId(Integer id) {
        return trainRepository.findById(id);
    }

    public List<SegmentVoie> getSegments() {
        return segmentVoieRepository.findAll();
    }

    public List<Trajet> getTrajets() {
        return trajetRepository.findAll();
    }

    public Optional<Trajet> getTrajetParId(Integer id) {
        return trajetRepository.findById(id);
    }

    public List<Voyage> getVoyages() {
        return voyageRepository.findAll();
    }

    public Optional<Voyage> getVoyageParId(Integer id) {
        return voyageRepository.findById(id);
    }
}

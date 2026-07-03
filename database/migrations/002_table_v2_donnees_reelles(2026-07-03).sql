-- =====================================================
-- GARE : ajout attributs manquants dans le modele Java
-- =====================================================

ALTER TABLE gare ADD COLUMN statut VARCHAR(20) NOT NULL DEFAULT 'HALTE';
ALTER TABLE gare ADD COLUMN pk_ordre DOUBLE PRECISION NOT NULL DEFAULT 0;

-- =====================================================
-- TRAJET : nom de ligne
-- =====================================================

ALTER TABLE trajet ADD COLUMN nom_ligne VARCHAR(150);

-- =====================================================
-- TRAIN : enum aligne sur Train.TypeTrain
-- =====================================================

ALTER TYPE type_train RENAME VALUE 'PASSAGER' TO 'VOYAGEURS';
ALTER TYPE type_train RENAME VALUE 'MARCHANDISE' TO 'FRET';
ALTER TYPE type_train RENAME VALUE 'MIXTE' TO 'MICHELINE';

-- =====================================================
-- VOYAGE : remplace gare_depart/arrive + heure_de_depart
-- par trajet_id + date_voyage (detail porte par arret_voyage)
-- =====================================================

ALTER TABLE voyage DROP CONSTRAINT chk_voyage_gare;
ALTER TABLE voyage DROP CONSTRAINT fk_voyage_depart;
ALTER TABLE voyage DROP CONSTRAINT fk_voyage_arrive;
ALTER TABLE voyage DROP COLUMN gare_depart_id;
ALTER TABLE voyage DROP COLUMN gare_arrive_id;
ALTER TABLE voyage DROP COLUMN heure_de_depart;

ALTER TABLE voyage ADD COLUMN trajet_id INTEGER;
ALTER TABLE voyage ADD CONSTRAINT fk_voyage_trajet FOREIGN KEY (trajet_id) REFERENCES trajet(id);
ALTER TABLE voyage ADD COLUMN date_voyage DATE NOT NULL DEFAULT CURRENT_DATE;

-- =====================================================
-- ARRET_VOYAGE : remplace pause_voyage / voyage_detail
-- (chaque arret = une escale ordonnee d'un voyage, avec heures)
-- =====================================================

DROP TABLE IF EXISTS pause_voyage;
DROP TABLE IF EXISTS voyage_detail;

CREATE TABLE arret_voyage (
    id SERIAL PRIMARY KEY,
    voyage_id INTEGER NOT NULL,
    gare_id INTEGER NOT NULL,
    heure_arrivee TIME,
    heure_depart TIME,
    ordre_arret INTEGER NOT NULL,

    CONSTRAINT fk_arret_voyage
        FOREIGN KEY (voyage_id) REFERENCES voyage(id) ON DELETE CASCADE,
    CONSTRAINT fk_arret_gare
        FOREIGN KEY (gare_id) REFERENCES gare(id)
);

-- =====================================================
-- SEGMENT_VOIE : trace reelle de la voie entre deux gares
-- =====================================================

CREATE TABLE segment_voie (
    id SERIAL PRIMARY KEY,
    code_segment VARCHAR(20) NOT NULL UNIQUE,
    gare_depart_id INTEGER NOT NULL,
    gare_arrivee_id INTEGER NOT NULL,
    longueur_km DOUBLE PRECISION NOT NULL,
    trace GEOMETRY(LineString, 4326) NOT NULL,

    CONSTRAINT fk_segment_depart FOREIGN KEY (gare_depart_id) REFERENCES gare(id),
    CONSTRAINT fk_segment_arrivee FOREIGN KEY (gare_arrivee_id) REFERENCES gare(id)
);

CREATE INDEX idx_segment_trace ON segment_voie USING GIST(trace);

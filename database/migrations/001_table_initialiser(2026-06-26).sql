-- =====================================================
-- EXTENSION POSTGIS
-- =====================================================

CREATE EXTENSION IF NOT EXISTS postgis;

-- =====================================================
-- ENUM
-- =====================================================

CREATE TYPE type_train AS ENUM (
    'PASSAGER',
    'MARCHANDISE',
    'MIXTE'
);

-- =====================================================
-- TABLE TRAIN
-- =====================================================

CREATE TABLE train (
    id SERIAL PRIMARY KEY,
    marque VARCHAR(100) NOT NULL,
    vitesse INTEGER NOT NULL CHECK(vitesse > 0),
    type_train type_train NOT NULL
);

-- =====================================================
-- TABLE GARE
-- =====================================================

CREATE TABLE gare (
    id SERIAL PRIMARY KEY,
    nom VARCHAR(150) NOT NULL UNIQUE,
    point GEOMETRY(Point,4326) NOT NULL
);

-- =====================================================
-- TABLE TRAJET
-- =====================================================

CREATE TABLE trajet (
    id SERIAL PRIMARY KEY,

    gare_depart_id INTEGER NOT NULL,
    gare_arrive_id INTEGER NOT NULL,

    CONSTRAINT fk_trajet_depart
        FOREIGN KEY (gare_depart_id)
        REFERENCES gare(id),

    CONSTRAINT fk_trajet_arrive
        FOREIGN KEY (gare_arrive_id)
        REFERENCES gare(id),

    CONSTRAINT chk_trajet_gare
        CHECK (gare_depart_id <> gare_arrive_id)
);

-- =====================================================
-- TABLE VOYAGE
-- =====================================================

CREATE TABLE voyage (
    id SERIAL PRIMARY KEY,

    train_id INTEGER NOT NULL,

    heure_de_depart TIMESTAMP NOT NULL,

    gare_depart_id INTEGER NOT NULL,

    gare_arrive_id INTEGER NOT NULL,

    CONSTRAINT fk_voyage_train
        FOREIGN KEY (train_id)
        REFERENCES train(id),

    CONSTRAINT fk_voyage_depart
        FOREIGN KEY (gare_depart_id)
        REFERENCES gare(id),

    CONSTRAINT fk_voyage_arrive
        FOREIGN KEY (gare_arrive_id)
        REFERENCES gare(id),

    CONSTRAINT chk_voyage_gare
        CHECK (gare_depart_id <> gare_arrive_id)
);

-- =====================================================
-- TABLE PAUSE_VOYAGE
-- =====================================================

CREATE TABLE pause_voyage (
    id SERIAL PRIMARY KEY,

    voyage_id INTEGER NOT NULL,

    gare_id INTEGER NOT NULL,

    duree INTEGER NOT NULL CHECK (duree > 0),

    CONSTRAINT fk_pause_voyage
        FOREIGN KEY (voyage_id)
        REFERENCES voyage(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_pause_gare
        FOREIGN KEY (gare_id)
        REFERENCES gare(id)
);

-- =====================================================
-- TABLE VOYAGE_DETAIL
-- =====================================================

CREATE TABLE voyage_detail (
    id SERIAL PRIMARY KEY,

    voyage_id INTEGER NOT NULL,

    trajet_id INTEGER NOT NULL,

    CONSTRAINT fk_detail_voyage
        FOREIGN KEY (voyage_id)
        REFERENCES voyage(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_detail_trajet
        FOREIGN KEY (trajet_id)
        REFERENCES trajet(id)
        ON DELETE CASCADE
);

-- =====================================================
-- INDEX SPATIAL
-- =====================================================

CREATE INDEX idx_gare_point
ON gare
USING GIST(point);
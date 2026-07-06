-- =====================================================
-- GARES (ligne FCE Fianarantsoa - Manakara)
-- =====================================================

INSERT INTO gare (id, nom, point, statut, pk_ordre) VALUES
(1, 'Fianarantsoa',   ST_SetSRID(ST_MakePoint(47.0857, -21.4530), 4326), 'PRINCIPALE', 0),
(2, 'Alakamisy',      ST_SetSRID(ST_MakePoint(47.1920, -21.4870), 4326), 'HALTE',      18),
(3, 'Andrambovato',   ST_SetSRID(ST_MakePoint(47.3680, -21.5450), 4326), 'HALTE',      42),
(4, 'Sahambavy',      ST_SetSRID(ST_MakePoint(47.1980, -21.4180), 4326), 'HALTE',      22),
(5, 'Tolongoina',     ST_SetSRID(ST_MakePoint(47.5680, -21.6230), 4326), 'PRINCIPALE', 86),
(6, 'Manampatrana',   ST_SetSRID(ST_MakePoint(47.7340, -21.7390), 4326), 'HALTE',      112),
(7, 'Mahabako',       ST_SetSRID(ST_MakePoint(47.9430, -21.9120), 4326), 'HALTE',      142),
(8, 'Manakara',       ST_SetSRID(ST_MakePoint(48.0120, -22.1450), 4326), 'PRINCIPALE', 163);

SELECT setval('gare_id_seq', 8);

-- =====================================================
-- TRAINS
-- =====================================================

INSERT INTO train (id, marque, vitesse, type_train) VALUES
(1, 'FCE-101 "Vohitra"',   40, 'VOYAGEURS'),
(2, 'FCE-102 "Namorona"',  40, 'VOYAGEURS'),
(3, 'FCE-Micheline 7',     35, 'MICHELINE'),
(4, 'FCE-Fret 22',         30, 'FRET');

SELECT setval('train_id_seq', 4);

-- =====================================================
-- SEGMENTS DE VOIE (traces reelles)
-- =====================================================

INSERT INTO segment_voie (code_segment, gare_depart_id, gare_arrivee_id, longueur_km, trace) VALUES
('FCE-01', 1, 2, 18.4, ST_GeomFromText('LINESTRING(47.085700 -21.453000, 47.097417 -21.461067, 47.110741 -21.468490, 47.126850 -21.474800, 47.146174 -21.479824, 47.168283 -21.483733, 47.192000 -21.487000)', 4326)),
('FCE-02', 2, 3, 24.1, ST_GeomFromText('LINESTRING(47.192000 -21.487000, 47.215333 -21.499067, 47.240274 -21.510490, 47.268000 -21.520800, 47.298941 -21.529824, 47.332667 -21.537733, 47.368000 -21.545000)', 4326)),
('FCE-03', 3, 5, 44.3, ST_GeomFromText('LINESTRING(47.368000 -21.545000, 47.395333 -21.560400, 47.424274 -21.575157, 47.456000 -21.588800, 47.490941 -21.601157, 47.528667 -21.612400, 47.568000 -21.623000)', 4326)),
('FCE-04', 5, 6, 26.7, ST_GeomFromText('LINESTRING(47.568000 -21.623000, 47.589667 -21.644733, 47.612941 -21.665824, 47.639000 -21.685800, 47.668274 -21.704490, 47.700333 -21.722067, 47.734000 -21.739000)', 4326)),
('FCE-05', 6, 7, 30.5, ST_GeomFromText('LINESTRING(47.734000 -21.739000, 47.762833 -21.770233, 47.793274 -21.800824, 47.826500 -21.830300, 47.862941 -21.858490, 47.902167 -21.885567, 47.943000 -21.912000)', 4326)),
('FCE-06', 7, 8, 21.2, ST_GeomFromText('LINESTRING(47.943000 -21.912000, 47.948500 -21.953233, 47.955608 -21.993824, 47.965500 -22.033300, 47.978608 -22.071490, 47.994500 -22.108567, 48.012000 -22.145000)', 4326)),
('FCE-S1', 1, 4, 22.0, ST_GeomFromText('LINESTRING(47.085700 -21.453000, 47.098417 -21.449567, 47.112741 -21.445490, 47.129850 -21.440300, 47.150174 -21.433824, 47.173283 -21.426233, 47.198000 -21.418000)', 4326));

-- =====================================================
-- TRAJETS
-- =====================================================

INSERT INTO trajet (id, nom_ligne, gare_depart_id, gare_arrive_id) VALUES
(1, 'Fianarantsoa - Manakara', 1, 8),
(2, 'Manakara - Fianarantsoa', 8, 1),
(3, 'Fianarantsoa - Sahambavy', 1, 4);

SELECT setval('trajet_id_seq', 3);

-- =====================================================
-- VOYAGES + ARRETS
-- =====================================================

-- Voyage 1 : direct complet Fianarantsoa -> Manakara
INSERT INTO voyage (id, train_id, trajet_id, date_voyage) VALUES (1, 1, 1, CURRENT_DATE);
INSERT INTO arret_voyage (voyage_id, gare_id, heure_arrivee, heure_depart, ordre_arret) VALUES
(1, 1, NULL,      '07:00', 1),
(1, 2, '07:35',   '07:45', 2),
(1, 3, '08:30',   '08:40', 3),
(1, 5, '10:05',   '10:25', 4),
(1, 6, '11:15',   '11:25', 5),
(1, 7, '12:20',   '12:30', 6),
(1, 8, '13:20',   NULL,    7);

-- Voyage 2 : micheline Fianarantsoa -> Tolongoina
INSERT INTO voyage (id, train_id, trajet_id, date_voyage) VALUES (2, 3, 1, CURRENT_DATE);
INSERT INTO arret_voyage (voyage_id, gare_id, heure_arrivee, heure_depart, ordre_arret) VALUES
(2, 1, NULL,      '06:30', 1),
(2, 3, '07:50',   '07:55', 2),
(2, 5, '09:10',   NULL,    3);

-- Voyage 3 : Tolongoina -> Manakara (correspondance du voyage 2)
INSERT INTO voyage (id, train_id, trajet_id, date_voyage) VALUES (3, 2, 1, CURRENT_DATE);
INSERT INTO arret_voyage (voyage_id, gare_id, heure_arrivee, heure_depart, ordre_arret) VALUES
(3, 5, NULL,      '09:40', 1),
(3, 6, '10:30',   '10:40', 2),
(3, 7, '11:35',   '11:45', 3),
(3, 8, '12:35',   NULL,    4);

-- Voyage 4 : train du soir, Fianarantsoa -> Manakara
INSERT INTO voyage (id, train_id, trajet_id, date_voyage) VALUES (4, 1, 1, CURRENT_DATE);
INSERT INTO arret_voyage (voyage_id, gare_id, heure_arrivee, heure_depart, ordre_arret) VALUES
(4, 1, NULL,      '13:00', 1),
(4, 2, '13:40',   '13:50', 2),
(4, 3, '14:45',   '14:55', 3),
(4, 5, '16:30',   '16:50', 4),
(4, 6, '17:45',   '17:55', 5),
(4, 7, '19:00',   '19:10', 6),
(4, 8, '20:05',   NULL,    7);

-- Voyage 5 : navette Fianarantsoa <-> Sahambavy
INSERT INTO voyage (id, train_id, trajet_id, date_voyage) VALUES (5, 3, 3, CURRENT_DATE);
INSERT INTO arret_voyage (voyage_id, gare_id, heure_arrivee, heure_depart, ordre_arret) VALUES
(5, 1, NULL,      '09:00', 1),
(5, 4, '09:35',   NULL,    2);

SELECT setval('voyage_id_seq', 5);

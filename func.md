
## 1. Architecture des Modules

### Module 1 : Cartographie Interactive & Interaction Dynamique
Ce module gère exclusivement le rendu cartographique (via Leaflet ou OpenLayers) et la restitution visuelle des données spatiales et temporelles à l'utilisateur.

* **F1.1 : Affichage du Réseau National**
    * Chargement et rendu vectoriel des voies ferrées (`LINESTRING`) sur un fond de carte adapté (ex: OpenStreetMap ou cartes topographiques du FTM).
    * Affichage des gares sous forme de marqueurs (`POINT`) dotés d'icônes thématiques selon le statut de la gare.
* **F1.2 : Consultation d'une Gare à un Temps $t$ (Modification 1)**
    * Au clic sur une gare, l'utilisateur spécifie une heure ou une date $t$.
    * Le système affiche **l'intégralité des départs prévus** ainsi que **tous les trajets en transit** (trains qui s'y arrêtent ou y passent) à partir de ce repère temporel $t$.
* **F1.3 : Habillage Temporel Global de l'Itinéraire (Modification 3)**
    * Lorsqu'un trajet ou un voyage est sélectionné et affiché sur la carte :
        * **Au-dessus de chaque marqueur Gare :** Affichage d'une étiquette fixe ou d'une infobulle affichant l'heure de passage exacte $t$ et la durée de la pause/arrêt (ex: `14h45 | Arrêt: 20 min`).
        * **Sur chaque segment/parcelle de voie :** Affichage textuel le long de la ligne (`textPath` ou au survol) indiquant la **durée précise de parcours sur ce tronçon spécifique** (ex: `Durée de la parcelle : 35 min`).

---

### Module 2 : Recherche & Moteur d'Itinéraires (Multi-Trains)
Ce module calcule les chemins optimaux et les enchaînements logistiques sur le réseau. Il constitue le "cerveau" algorithmique du SIG.

* **F2.1 : Définition des Points Clés**
    * Sélection interactive ou textuelle d'une gare de départ, d'une gare d'arrivée et d'un horizon temporel de départ.
* **F2.2 : Calcul de la Distance Réelle**
    * Interrogation de la base de données PostGIS pour calculer via `ST_Length` la distance exacte en kilomètres en épousant les courbes réelles de la voie ferrée (et non une ligne droite).
* **F2.3 : Proposition Systématique des Escales (Modification 2)**
    * Le système ne se limite pas aux liaisons directes. Il calcule et propose **systématiquement** deux catégories d'itinéraires à l'utilisateur :
        1. **Trajets Directs :** Si un même train effectue l'ensemble du parcours demandé.
        2. **Trajets avec Escale(s) :** Combinaison de plusieurs trains successifs. Le moteur Node.js valide automatiquement la correspondance en vérifiant que le Train B part de la gare d'escale *après* l'arrivée du Train A, tout en calculant le temps d'attente imposé aux usagers dans la gare de transfert.

---

### Module 3 : Planification & Data Ferroviaire (Backend Spinboot / PostGIS)
Ce module structure la donnée en base, exécute les calculs géospatiaux complexes et expose les API pour le Frontend.

* **F3.1 : Calcul de la Chronologie des Parcelles**
    * Extraction et structuration des fiches horaires de chaque train.
    * Ce sous-module calcule la durée de chaque "petite parcelle" ferroviaire pour le Module 1 en effectuant la différence temporelle : `Durée = (Heure d'arrivée à la Gare Suivante) - (Heure de départ de la Gare Actuelle)`.
* **F3.2 : Moteur de Requêtes Temporelles par Gare**
    * Exécution de requêtes SQL optimisées filtrant la table des arrêts pour extraire les flux croisant une gare donnée à un instant $t$ précis.

NB: utiliser la carte d'open streemap en ligne pour cela
## 2. Schéma de Base de Données Adapté (Markdown)

Voici le schéma relationnel mis à jour pour supporter nativement le calcul des escales, la durée par parcelle et l'affichage au temps $t$ :

```markdown
- train:
    - id (INT, PK)
    - marque (VARCHAR)
    - vitesse_max (FLOAT)
    - type_train (enum)          # Voyageurs, Fret, Micheline

- gare:
    - id (INT, PK)
    - nom (VARCHAR)
    - geometrie (GEOMETRY(POINT, 4326)) # Coordonnées géographiques GPS
    - statut (enum)              # Gare principale, Halte, Tri

- segment_voie:
    - id (INT, PK)
    - code_segment (VARCHAR)     # Identifiant du tronçon (ex: "TCE-01")
    - gare_depart_id (INT, FK -> gare.id)
    - gare_arrivee_id (INT, FK -> gare.id)
    - geometrie (GEOMETRY(LINESTRING, 4326)) # Tracé géométrique réel des rails
    - longueur_km (FLOAT)        # Généré via ST_Length()

- trajet:
    - id (INT, PK)
    - nom_ligne (VARCHAR)        # Ex: "Fianarantsoa-Manakara"
    - gare_origine_id (INT, FK -> gare.id)
    - gare_terminus_id (INT, FK -> gare.id)

- trajet_composition:
    - id (INT, PK)
    - trajet_id (INT, FK -> trajet.id)
    - segment_voie_id (INT, FK -> segment_voie.id)
    - ordre (INT)                # Ordre de succession des parcelles de voie

- voyage:
    - id (INT, PK)
    - train_id (INT, FK -> train.id)
    - trajet_id (INT, FK -> trajet.id)
    - date_voyage (DATE)

- arret_voyage:
    - id (INT, PK)
    - voyage_id (INT, FK -> voyage.id)
    - gare_id (INT, FK -> gare.id)
    - heure_arrivee (TIME)       # Heure d'entrée en gare
    - heure_depart (TIME)        # Heure de sortie de la gare (calcul de la pause : heure_depart - heure_arrivee)
    - ordre_arret (INT)          # Permet de savoir l'enchaînement chronologique des gares
/* ============================================================
   Carte interactive SIG Ferroviaire
   F1.1 : reseau (gares + voies)
   F1.2 : consultation gare a un instant t
   F1.3 : habillage temporel global d'un voyage
   ============================================================ */

const carte = L.map('carteLeaflet', { zoomControl: true }).setView([-21.6, 47.55], 9);

L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
    maxZoom: 18
}).addTo(carte);

const calqueVoies = L.layerGroup().addTo(carte);
const calqueGares = L.layerGroup().addTo(carte);
const calqueHabillage = L.layerGroup().addTo(carte);

const marqueursGares = {}; // id -> L.Marker
let gareSelectionneeId = null;

const segmentsIndex = []; // {id, gareDepartId, gareArriveeId, latlngs, poly}
let grapheVoies = {};     // construit via construireGraphe() (reseau-graphe.js) une fois /api/reseau charge
const calqueTrain = L.layerGroup().addTo(carte);
let animationTrainId = null;
let dernierTrajetActif = null; // {segmentsOrdonnes, dureesMinutes} pour le bouton "Rejouer"

function couleurStatut(statut) {
    if (statut === 'PRINCIPALE') return '#C2622D';
    if (statut === 'TRI') return '#E8B33D';
    return '#5C7E6A'; // HALTE
}

function rayonStatut(statut) {
    return statut === 'PRINCIPALE' ? 9 : 6;
}

function styleDefaut(gare) {
    return {
        radius: rayonStatut(gare.statut),
        fillColor: couleurStatut(gare.statut),
        color: '#F0E9DC',
        weight: 2,
        fillOpacity: 1
    };
}

const styleVoieDefaut = { color: '#5C7E6A', weight: 3, opacity: 0.85, className: 'voie-defaut' };
const styleVoieInactive = { color: '#5C7E6A', weight: 2, opacity: 0.25, className: 'voie-inactive' };
const styleVoieActive = { color: '#E8B33D', weight: 5, opacity: 1, className: 'voie-active' };

/** Remet toutes les voies dans leur etat neutre (aucun trajet en surbrillance). */
function reinitialiserVoies() {
    segmentsIndex.forEach(s => s.poly.setStyle(styleVoieDefaut));
}

/** Met en surbrillance uniquement les segments d'ids donnes, estompe les autres. */
function mettreEnValeurSegments(idsActifs) {
    segmentsIndex.forEach(s => {
        s.poly.setStyle(idsActifs.has(s.id) ? styleVoieActive : styleVoieInactive);
        if (idsActifs.has(s.id)) s.poly.bringToFront();
    });
}

function styleSelectionne(gare) {
    return {
        radius: rayonStatut(gare.statut) + 5,
        fillColor: '#E8B33D',
        color: '#1A2B2E',
        weight: 3,
        fillOpacity: 1
    };
}

function iconeGare(gare) {
    return L.circleMarker([gare.lat, gare.lng], styleDefaut(gare));
}

/** Marque une gare comme selectionnee : restyle l'ancienne, met la nouvelle en evidence + au premier plan. */
function selectionnerGare(gareId, { centrer = false } = {}) {
    if (gareSelectionneeId !== null && marqueursGares[gareSelectionneeId]) {
        const ancien = marqueursGares[gareSelectionneeId];
        ancien.setStyle(styleDefaut(ancien.gareData));
    }

    gareSelectionneeId = gareId ? Number(gareId) : null;
    selectGare.value = gareSelectionneeId ?? '';

    const marqueur = gareSelectionneeId ? marqueursGares[gareSelectionneeId] : null;
    if (marqueur) {
        marqueur.setStyle(styleSelectionne(marqueur.gareData));
        marqueur.bringToFront();
        if (centrer) carte.panTo(marqueur.getLatLng());
    }
}

// -------------------- Legende --------------------

const legende = L.control({ position: 'bottomleft' });
legende.onAdd = function () {
    const div = L.DomUtil.create('div', 'legende-carte');
    div.innerHTML = `
        <div class="legende-carte__titre">Gares</div>
        <div class="legende-carte__item"><span class="legende-carte__pastille" style="background:#C2622D"></span>Principale</div>
        <div class="legende-carte__item"><span class="legende-carte__pastille" style="background:#E8B33D"></span>Triage</div>
        <div class="legende-carte__item"><span class="legende-carte__pastille" style="background:#5C7E6A"></span>Halte</div>
        <div class="legende-carte__item"><span class="legende-carte__pastille legende-carte__pastille--select"></span>Sélectionnée</div>
    `;
    L.DomEvent.disableClickPropagation(div);
    return div;
};
legende.addTo(carte);

// -------------------- F1.1 : chargement du reseau --------------------

fetch('/api/reseau')
    .then(r => r.json())
    .then(data => {
        data.segments.forEach(seg => {
            const latlngs = seg.trace.map(p => [p[0], p[1]]);
            const poly = L.polyline(latlngs, styleVoieDefaut).addTo(calqueVoies);
            poly.bindTooltip(seg.longueurKm.toFixed(1) + ' km', {
                permanent: true,
                direction: 'center',
                className: 'etiquette-distance'
            });
            segmentsIndex.push({
                id: seg.id,
                gareDepartId: seg.gareDepartId,
                gareArriveeId: seg.gareArriveeId,
                latlngs,
                poly
            });
        });
        grapheVoies = construireGraphe(data.segments);

        data.gares.forEach(gare => {
            const marqueur = iconeGare(gare).addTo(calqueGares);
            marqueur.gareData = gare;
            marqueur.bindTooltip(gare.nom + ' — ' + gare.statutLibelle, { direction: 'top', offset: [0, -8] });

            marqueur.on('mouseover', () => {
                if (gare.id !== gareSelectionneeId) marqueur.setStyle({ weight: 3 });
            });
            marqueur.on('mouseout', () => {
                if (gare.id !== gareSelectionneeId) marqueur.setStyle({ weight: 2 });
            });
            marqueur.on('click', (e) => {
                L.DomEvent.stopPropagation(e);
                selectionnerGare(gare.id);
                chargerFluxGare(gare.id);
            });

            marqueursGares[gare.id] = marqueur;
        });
    })
    .catch(err => console.error('Erreur chargement réseau:', err));

// -------------------- F1.4 : suggestion de la gare la plus proche au clic sur la carte --------------------

function gareLaPlusProche(latlng) {
    let meilleur = null, meilleureDistanceM = Infinity;
    Object.values(marqueursGares).forEach(m => {
        const d = latlng.distanceTo(m.getLatLng());
        if (d < meilleureDistanceM) { meilleureDistanceM = d; meilleur = m; }
    });
    return meilleur ? { marqueur: meilleur, distanceM: meilleureDistanceM } : null;
}

function formaterDistance(metres) {
    return metres >= 1000 ? (metres / 1000).toFixed(1) + ' km' : Math.round(metres) + ' m';
}

carte.on('click', (e) => {
    const proche = gareLaPlusProche(e.latlng);
    if (!proche) return;
    const gare = proche.marqueur.gareData;

    const contenu = document.createElement('div');
    contenu.className = 'suggestion-gare';
    contenu.innerHTML =
        '<strong>Gare la plus proche</strong>' +
        `<p>${gare.nom} <span class="suggestion-distance">à ${formaterDistance(proche.distanceM)}</span></p>`;

    const btn = document.createElement('button');
    btn.type = 'button';
    btn.className = 'bouton-suggestion';
    btn.textContent = 'Consulter cette gare';
    btn.addEventListener('click', () => {
        selectionnerGare(gare.id);
        chargerFluxGare(gare.id);
        if (typeof selectGare !== 'undefined' && selectGare) selectGare.value = gare.id;
        carte.closePopup();
    });
    contenu.appendChild(btn);

    L.popup({ className: 'popup-suggestion', closeButton: true })
        .setLatLng(e.latlng)
        .setContent(contenu)
        .openOn(carte);
});

// -------------------- F1.2 : consultation gare a un instant t --------------------

const selectGare = document.getElementById('selectGare');
const inputHeure = document.getElementById('inputHeure');
const panneauFlux = document.getElementById('panneauFlux');

function badgeClasse(type) {
    if (type === 'Départ') return 'flux-item--depart';
    if (type === 'Arrivée') return 'flux-item--arrivee';
    return '';
}

function chargerFluxGare(gareId) {
    if (!gareId) {
        panneauFlux.innerHTML = '<p class="message-vide">Sélectionnez une gare pour voir les départs et trains en transit à partir de l\'heure choisie.</p>';
        return;
    }
    const heure = inputHeure.value || '00:00';
    fetch(`/api/gare/${gareId}/flux?heure=${heure}:00`)
        .then(r => r.json())
        .then(data => {
            let html = `<h2 style="border:none;padding-bottom:0;margin-bottom:0.2rem;">${data.gareNom}</h2>`;
            html += `<p class="flux-gare-sstitre">Flux à partir de ${heure}</p>`;

            if (data.flux.length === 0) {
                html += '<p class="message-vide">Aucun départ ni transit après cette heure.</p>';
            } else {
                data.flux.forEach(f => {
                    const heureAffichee = f.heureDepart || f.heureArrivee || '—';
                    html += `
                        <div class="flux-item ${badgeClasse(f.type)}">
                            <div class="flux-item__entete">
                                <span class="flux-item__type">${f.type}</span>
                                <span class="flux-item__heure">${heureAffichee.substring(0,5)}</span>
                            </div>
                            <div class="flux-item__train">${f.trainMarque}</div>
                            <div class="flux-item__ligne">${f.nomLigne}</div>
                            ${f.type === 'Transit' ? `<div class="flux-item__pause">Arrêt : ${f.dureeArret}</div>` : ''}
                        </div>`;
                });
            }
            panneauFlux.innerHTML = html;
        })
        .catch(err => {
            console.error('Erreur flux gare:', err);
            panneauFlux.innerHTML = '<p class="message-vide">Erreur de chargement.</p>';
        });
}

selectGare.addEventListener('change', () => {
    selectionnerGare(selectGare.value, { centrer: true });
    chargerFluxGare(selectGare.value);
});
inputHeure.addEventListener('change', () => {
    if (selectGare.value) chargerFluxGare(selectGare.value);
});

// -------------------- F1.3 : habillage temporel d'un voyage --------------------

const selectVoyage = document.getElementById('selectVoyage');

fetch('/api/voyages')
    .then(r => r.json())
    .then(voyages => {
        voyages.forEach(v => {
            const opt = document.createElement('option');
            opt.value = v.id;
            opt.textContent = v.label;
            selectVoyage.appendChild(opt);
        });
    });

/** A partir des arrets d'un voyage, retrouve la trace GPS reelle empruntee (via le graphe des segments)
 *  et les segments a mettre en surbrillance sur la carte. Gere les gares sautees (train direct). */
function calculerTrajetReel(data) {
    const arretsValides = data.arrets.filter(a => a.lat != null);
    const idsSegmentsActifs = new Set();
    const parcelles = [];

    for (let i = 0; i < arretsValides.length - 1; i++) {
        const a = arretsValides[i];
        const b = arretsValides[i + 1];
        const chemin = chercherCheminSegments(grapheVoies, a.gareId, b.gareId);
        const points = [[a.lat, a.lng]];

        chemin.forEach(etape => {
            idsSegmentsActifs.add(etape.segment.id);
            const segRef = segmentsIndex.find(s => s.id === etape.segment.id);
            const coords = etape.depart === segRef.gareDepartId ? segRef.latlngs : [...segRef.latlngs].reverse();
            coords.forEach((c, idx) => { if (idx > 0) points.push(c); });
        });

        const infosParcelle = data.parcelles.find(p => p.gareDepartId === a.gareId && p.gareArriveeId === b.gareId);
        parcelles.push({ points, dureeMinutes: infosParcelle ? infosParcelle.dureeMinutes : 1 });
    }
    return { idsSegmentsActifs, parcelles };
}

/** Position interpolee le long d'une trace (par longueur d'arc, t entre 0 et 1) : mouvement a vitesse constante. */
function pointSurTrace(points, t) {
    if (points.length === 1) return points[0];
    const longueurs = [];
    let total = 0;
    for (let i = 0; i < points.length - 1; i++) {
        const d = L.latLng(points[i]).distanceTo(L.latLng(points[i + 1]));
        longueurs.push(d);
        total += d;
    }
    if (total === 0) return points[0];
    let cible = t * total, cumul = 0;
    for (let i = 0; i < longueurs.length; i++) {
        if (cumul + longueurs[i] >= cible) {
            const frac = (cible - cumul) / longueurs[i];
            const [lat1, lng1] = points[i], [lat2, lng2] = points[i + 1];
            return [lat1 + (lat2 - lat1) * frac, lng1 + (lng2 - lng1) * frac];
        }
        cumul += longueurs[i];
    }
    return points[points.length - 1];
}

const DUREE_REPLAY_MS = 9000; // duree totale de l'animation, independante de la duree reelle du voyage

/** Effet waouh : rejoue le voyage sur la carte, un petit train parcourt la trace GPS reelle,
 *  chaque troncon dure proportionnellement a sa duree reelle (F1.3). */
function rejouerVoyage(parcelles) {
    calqueTrain.clearLayers();
    if (animationTrainId) cancelAnimationFrame(animationTrainId);
    if (!parcelles.length) return;

    const totalMinutes = parcelles.reduce((s, p) => s + Math.max(p.dureeMinutes, 1), 0) || 1;
    const icone = L.divIcon({ className: '', html: '<div class="train-icone">🚂</div>', iconSize: [22, 22], iconAnchor: [11, 11] });
    const marqueurTrain = L.marker(parcelles[0].points[0], { icon: icone }).addTo(calqueTrain);

    let indexParcelle = 0;
    let debutParcelleMs = null;

    function step(horodatage) {
        const parcelle = parcelles[indexParcelle];
        const dureeParcelleMs = Math.max((Math.max(parcelle.dureeMinutes, 1) / totalMinutes) * DUREE_REPLAY_MS, 300);
        if (debutParcelleMs === null) debutParcelleMs = horodatage;
        const t = Math.min((horodatage - debutParcelleMs) / dureeParcelleMs, 1);
        marqueurTrain.setLatLng(pointSurTrace(parcelle.points, t));

        if (t >= 1) {
            indexParcelle++;
            debutParcelleMs = null;
            if (indexParcelle >= parcelles.length) { animationTrainId = null; return; }
        }
        animationTrainId = requestAnimationFrame(step);
    }
    animationTrainId = requestAnimationFrame(step);
}

const btnRejouer = document.getElementById('btnRejouerVoyage');
btnRejouer.addEventListener('click', () => {
    if (dernierTrajetActif) rejouerVoyage(dernierTrajetActif);
});

selectVoyage.addEventListener('change', () => {
    calqueHabillage.clearLayers();
    calqueTrain.clearLayers();
    if (animationTrainId) cancelAnimationFrame(animationTrainId);
    dernierTrajetActif = null;
    btnRejouer.disabled = true;

    if (!selectVoyage.value) {
        reinitialiserVoies();
        return;
    }

    fetch(`/api/voyage/${selectVoyage.value}/habillage`)
        .then(r => r.json())
        .then(data => {
            // Etiquettes au-dessus de chaque gare : heure de passage + duree de pause
            data.arrets.forEach(a => {
                if (a.lat == null) return;
                const heure = (a.heureDepart || a.heureArrivee || '').substring(0, 5);
                const texte = a.dureeArret === 'Sans arrêt'
                    ? `${heure}`
                    : `${heure} | Arrêt : ${a.dureeArret.replace(' min','')} min`;

                const icone = L.divIcon({
                    className: '',
                    html: `<div class="etiquette-temps">${texte}</div>`,
                    iconSize: null,
                    iconAnchor: [20, 32]
                });
                L.marker([a.lat, a.lng], { icon: icone }).addTo(calqueHabillage);
            });

            // Etiquettes sur chaque parcelle : duree de parcours du troncon
            data.parcelles.forEach(p => {
                const gareDepart = data.arrets.find(a => a.gareId === p.gareDepartId);
                const gareArrivee = data.arrets.find(a => a.gareId === p.gareArriveeId);
                if (!gareDepart || !gareArrivee) return;

                const milieuLat = (gareDepart.lat + gareArrivee.lat) / 2;
                const milieuLng = (gareDepart.lng + gareArrivee.lng) / 2;

                const icone = L.divIcon({
                    className: '',
                    html: `<div class="etiquette-parcelle">${p.dureeLibelle}</div>`,
                    iconSize: null,
                    iconAnchor: [50, 10]
                });
                L.marker([milieuLat, milieuLng], { icon: icone }).addTo(calqueHabillage);
            });

            // Zoom sur l'etendue du voyage
            const pts = data.arrets.filter(a => a.lat != null).map(a => [a.lat, a.lng]);
            if (pts.length > 1) {
                carte.fitBounds(pts, { padding: [40, 40] });
            }

            // Distingue le trajet reel de ce voyage du reste du reseau + lance l'effet waouh
            const { idsSegmentsActifs, parcelles } = calculerTrajetReel(data);
            mettreEnValeurSegments(idsSegmentsActifs);
            dernierTrajetActif = parcelles;
            btnRejouer.disabled = false;
            rejouerVoyage(parcelles);
        })
        .catch(err => console.error('Erreur habillage voyage:', err));
});

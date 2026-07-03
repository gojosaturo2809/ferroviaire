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

function couleurStatut(statut) {
    if (statut === 'PRINCIPALE') return '#C2622D';
    if (statut === 'TRI') return '#E8B33D';
    return '#5C7E6A'; // HALTE
}

function rayonStatut(statut) {
    return statut === 'PRINCIPALE' ? 9 : 6;
}

function iconeGare(gare) {
    return L.circleMarker([gare.lat, gare.lng], {
        radius: rayonStatut(gare.statut),
        fillColor: couleurStatut(gare.statut),
        color: '#F0E9DC',
        weight: 2,
        fillOpacity: 1
    });
}

// -------------------- F1.1 : chargement du reseau --------------------

fetch('/api/reseau')
    .then(r => r.json())
    .then(data => {
        data.segments.forEach(seg => {
            const latlngs = seg.trace.map(p => [p[0], p[1]]);
            L.polyline(latlngs, {
                color: '#5C7E6A',
                weight: 3,
                opacity: 0.85
            }).addTo(calqueVoies);
        });

        data.gares.forEach(gare => {
            const marqueur = iconeGare(gare).addTo(calqueGares);
            marqueur.bindTooltip(gare.nom + ' — ' + gare.statutLibelle, { direction: 'top', offset: [0, -8] });
            marqueur.on('click', () => {
                document.getElementById('selectGare').value = gare.id;
                chargerFluxGare(gare.id);
            });
            marqueursGares[gare.id] = marqueur;
        });
    })
    .catch(err => console.error('Erreur chargement réseau:', err));

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

selectGare.addEventListener('change', () => chargerFluxGare(selectGare.value));
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

selectVoyage.addEventListener('change', () => {
    calqueHabillage.clearLayers();
    if (!selectVoyage.value) return;

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
        })
        .catch(err => console.error('Erreur habillage voyage:', err));
});

document.getElementById('boutonInversion').addEventListener('click', () => {
    const gareDepart = document.getElementById('gareDepart');
    const gareArrivee = document.getElementById('gareArrivee');
    const tmp = gareDepart.value;
    gareDepart.value = gareArrivee.value;
    gareArrivee.value = tmp;
});

// -------------------- F2.4 : mini-carte interactive des propositions d'itineraire --------------------

const conteneurCarte = document.getElementById('carteRecherche');
if (conteneurCarte) {

    const carte = L.map('carteRecherche', { zoomControl: true }).setView([-21.6, 47.55], 8);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
        maxZoom: 18
    }).addTo(carte);

    const calqueReseau = L.layerGroup().addTo(carte);
    const calqueTrajet = L.layerGroup().addTo(carte);

    const COULEUR_DIRECT = '#5C7E6A';
    const COULEURS_ESCALE = ['#C2622D', '#E8B33D', '#3D5A4A'];

    let segmentsIndexParId = {};
    let grapheReseau = {};
    let garesParId = {};

    fetch('/api/reseau')
        .then(r => r.json())
        .then(data => {
            data.segments.forEach(seg => {
                const latlngs = seg.trace.map(p => [p[0], p[1]]);
                L.polyline(latlngs, { color: '#5C7E6A', weight: 2, opacity: 0.35 }).addTo(calqueReseau);
                segmentsIndexParId[seg.id] = { gareDepartId: seg.gareDepartId, gareArriveeId: seg.gareArriveeId, latlngs };
            });
            data.gares.forEach(gare => {
                garesParId[gare.id] = gare;
                L.circleMarker([gare.lat, gare.lng], { radius: 4, color: '#5C7E6A', weight: 1, fillColor: '#F0E9DC', fillOpacity: 1 })
                    .bindTooltip(gare.nom, { direction: 'top', offset: [0, -4] })
                    .addTo(calqueReseau);
            });
            grapheReseau = construireGraphe(data.segments);

            // Affiche le premier trajet disponible par defaut, pour un rendu immediat.
            const premierTicket = document.querySelector('.ticket-itineraire--cliquable');
            if (premierTicket) afficherTrajetDuTicket(premierTicket);
        })
        .catch(err => console.error('Erreur chargement réseau (recherche):', err));

    function iconeEtape(couleur, rayon) {
        return { radius: rayon, color: '#20211D', weight: 2, fillColor: couleur, fillOpacity: 1 };
    }

    /** Dessine le trajet reel (via BFS sur le graphe) pour la liste de paires [{depId, arrId}] d'un ticket. */
    function afficherTrajet(pairesGares) {
        calqueTrajet.clearLayers();
        if (!pairesGares.length) return;

        const escale = pairesGares.length > 1;
        const tousLesPoints = [];

        pairesGares.forEach((paire, index) => {
            const gareDepart = garesParId[paire.depId];
            const gareArrivee = garesParId[paire.arrId];
            if (!gareDepart || !gareArrivee) return;

            const chemin = chercherCheminSegments(grapheReseau, paire.depId, paire.arrId);
            const points = chainerPoints(chemin, segmentsIndexParId, [gareDepart.lat, gareDepart.lng]);
            if (points.length === 1) points.push([gareArrivee.lat, gareArrivee.lng]);

            const couleur = escale ? COULEURS_ESCALE[index % COULEURS_ESCALE.length] : COULEUR_DIRECT;
            L.polyline(points, { color: couleur, weight: 5, opacity: 0.95 }).addTo(calqueTrajet);
            tousLesPoints.push(...points);

            // Gare de depart de la 1ere jambe et d'arrivee de la derniere : marqueurs terminus.
            if (index === 0) {
                L.circleMarker([gareDepart.lat, gareDepart.lng], iconeEtape('#F0E9DC', 8))
                    .bindTooltip('Départ — ' + gareDepart.nom, { direction: 'top', offset: [0, -6] })
                    .addTo(calqueTrajet);
            }
            if (index === pairesGares.length - 1) {
                L.circleMarker([gareArrivee.lat, gareArrivee.lng], iconeEtape('#F0E9DC', 8))
                    .bindTooltip('Arrivée — ' + gareArrivee.nom, { direction: 'top', offset: [0, -6] })
                    .addTo(calqueTrajet);
            } else {
                // Gare de correspondance : marqueur distinct (F2.4 - distingue escale / direct)
                L.circleMarker([gareArrivee.lat, gareArrivee.lng], iconeEtape('#E8B33D', 9))
                    .bindTooltip('🔀 Correspondance — ' + gareArrivee.nom, { direction: 'top', offset: [0, -6] })
                    .addTo(calqueTrajet);
            }
        });

        if (tousLesPoints.length) carte.fitBounds(tousLesPoints, { padding: [30, 30] });
    }

    function afficherTrajetDuTicket(ticket) {
        const pairesGares = Array.from(ticket.querySelectorAll('.parcours-segment[data-gare-depart-id]')).map(el => ({
            depId: parseInt(el.dataset.gareDepartId, 10),
            arrId: parseInt(el.dataset.gareArriveeId, 10)
        }));
        afficherTrajet(pairesGares);

        document.querySelectorAll('.ticket-itineraire--active').forEach(t => t.classList.remove('ticket-itineraire--active'));
        ticket.classList.add('ticket-itineraire--active');
    }

    document.querySelectorAll('.ticket-itineraire--cliquable').forEach(ticket => {
        ticket.addEventListener('click', () => afficherTrajetDuTicket(ticket));
    });
}

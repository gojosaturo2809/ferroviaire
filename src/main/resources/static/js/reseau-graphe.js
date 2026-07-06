/* ============================================================
   Module partage : graphe du reseau ferroviaire
   Utilise par carte.js (F1.3) et recherche.js (F2) pour retrouver
   la trace GPS reelle entre deux gares, quel que soit le nombre
   de gares intermediaires sautees.
   ============================================================ */

/** Construit un graphe non-oriente gareId -> [{versId, segment}] a partir des segments de /api/reseau. */
function construireGraphe(segments) {
    const graphe = {};
    function ajouterArc(a, b, segment) {
        if (!graphe[a]) graphe[a] = [];
        graphe[a].push({ versId: b, segment });
    }
    segments.forEach(seg => {
        ajouterArc(seg.gareDepartId, seg.gareArriveeId, seg);
        ajouterArc(seg.gareArriveeId, seg.gareDepartId, seg);
    });
    return graphe;
}

/** BFS : retrouve la suite ordonnee de segments reels reliant deux gares (gere les gares sautees). */
function chercherCheminSegments(graphe, depuisId, versId) {
    if (depuisId === versId) return [];
    const visites = new Set([depuisId]);
    const file = [{ gareId: depuisId, chemin: [] }];
    while (file.length) {
        const { gareId, chemin } = file.shift();
        const voisins = graphe[gareId] || [];
        for (const { versId: suivantId, segment } of voisins) {
            if (visites.has(suivantId)) continue;
            const nouveauChemin = [...chemin, { segment, depart: gareId, arrivee: suivantId }];
            if (suivantId === versId) return nouveauChemin;
            visites.add(suivantId);
            file.push({ gareId: suivantId, chemin: nouveauChemin });
        }
    }
    return [];
}

/** A partir d'un chemin de segments (sortie de chercherCheminSegments) et d'un index {id -> latlngs oriente depart->arrivee},
 *  construit une seule liste de points continue [lat,lng] dans le sens du trajet. */
function chainerPoints(chemin, segmentsIndexParId, pointDepart) {
    const points = [pointDepart];
    chemin.forEach(etape => {
        const segRef = segmentsIndexParId[etape.segment.id];
        const coords = etape.depart === segRef.gareDepartId ? segRef.latlngs : [...segRef.latlngs].reverse();
        coords.forEach((c, idx) => { if (idx > 0) points.push(c); });
    });
    return points;
}

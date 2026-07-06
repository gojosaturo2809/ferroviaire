document.getElementById('boutonInversion').addEventListener('click', () => {
    const gareDepart = document.getElementById('gareDepart');
    const gareArrivee = document.getElementById('gareArrivee');
    const tmp = gareDepart.value;
    gareDepart.value = gareArrivee.value;
    gareArrivee.value = tmp;
});

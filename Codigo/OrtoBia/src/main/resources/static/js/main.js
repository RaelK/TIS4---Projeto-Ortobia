// Função para inicializar o mapa do Google Maps
function initMap() {
    // Coordenadas da Rua Cândido Nogueira, 35 - Grajaú, Belo Horizonte - MG
    const consultorio = { lat: -19.926557, lng: -43.928647 };
    
    // Criar o mapa centralizado na localização do consultório
    const map = new google.maps.Map(document.getElementById('map'), {
        zoom: 16,
        center: consultorio,
        mapId: '8d193001f940fde3', // Estilo moderno do mapa
        mapTypeControl: false, // Remove os controles de tipo de mapa
        fullscreenControl: false, // Remove o controle de tela cheia
        streetViewControl: false, // Remove o controle do Street View
    });

    // Adicionar marcador personalizado
    const marker = new google.maps.Marker({
        position: consultorio,
        map: map,
        title: 'Clínica Odontológica',
        animation: google.maps.Animation.DROP
    });

    // Adicionar janela de informações
    const infowindow = new google.maps.InfoWindow({
        content: `
            <div style="padding: 10px;">
                <h3 style="margin: 0 0 5px 0;">Clínica Odontológica</h3>
                <p style="margin: 0;">R. Cândido Nogueira, 35<br>
                Grajaú, Belo Horizonte - MG<br>
                30431-218</p>
            </div>
        `
    });

    // Abrir janela de informações ao clicar no marcador
    marker.addListener('click', () => {
        infowindow.open(map, marker);
    });
} 
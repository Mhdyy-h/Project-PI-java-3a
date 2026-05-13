package org.example.api;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import org.example.dao.RendezVousDAO;
import org.example.model.RendezVous;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * API Maps Visualization pour BioSync
 * Interface cartographique interactive avec HTML/CSS/JavaScript
 */
public class MapsVisualizationAPI {
    
    private static final int DEFAULT_PORT = 8083;
    
    private static int findAvailablePort(int defaultPort) {
        try (java.net.ServerSocket socket = new java.net.ServerSocket(defaultPort)) {
            return defaultPort;
        } catch (java.io.IOException e) {
            // Port is taken, try to find an available one
            for (int port = defaultPort + 1; port <= defaultPort + 100; port++) {
                try (java.net.ServerSocket testSocket = new java.net.ServerSocket(port)) {
                    System.out.println("🔍 Port " + defaultPort + " is in use, using port " + port + " instead");
                    return port;
                } catch (java.io.IOException ignored) {
                    // Continue trying next port
                }
            }
            throw new RuntimeException("Could not find an available port between " + defaultPort + " and " + (defaultPort + 100));
        }
    }
    
    public static void main(String[] args) throws IOException {
        int port = findAvailablePort(DEFAULT_PORT);
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        
        // Health check
        server.createContext("/api/maps/health", new HealthHandler());
        
        // Interactive map page
        server.createContext("/api/maps", new MapPageHandler());
        
        // Map data endpoint
        server.createContext("/api/maps/data", new MapDataHandler());
        
        // Static assets (CSS, JS)
        server.createContext("/api/maps/static", new StaticHandler());
        
        server.setExecutor(null);
        server.start();
        
        System.out.println("🗺️ BioSync Maps Visualization API started on http://localhost:" + port);
        System.out.println("📍 Available endpoints:");
        System.out.println("  GET  /api/maps/health - Health check");
        System.out.println("  GET  /api/maps - Page carte interactive");
        System.out.println("  GET  /api/maps/data - Données pour la carte");
        System.out.println("  GET  /api/maps/static - Fichiers statiques");
        System.out.println();
        System.out.println("🌐 Open your browser and go to: http://localhost:" + port + "/api/maps");
    }
    
    static class HealthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "{\"status\":\"healthy\",\"service\":\"BioSync Maps Visualization API\",\"timestamp\":" + System.currentTimeMillis() + "}";
            sendResponse(exchange, 200, response, "application/json");
        }
    }
    
    static class MapPageHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                String htmlPage = generateMapPage();
                sendResponse(exchange, 200, htmlPage, "text/html");
            } else {
                sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}", "application/json");
            }
        }
    }
    
    static class MapDataHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                if ("GET".equals(exchange.getRequestMethod())) {
                    System.out.println("🔍 DEBUG: Loading rendezvous from database...");
                    
                    // Test with mock data first to see if display works
                    List<RendezVous> rendezVous = createMockRendezVous();
                    System.out.println("🔍 DEBUG: Created " + rendezVous.size() + " mock rendezvous");
                    
                    // Debug each rendezvous
                    for (int i = 0; i < rendezVous.size(); i++) {
                        RendezVous rdv = rendezVous.get(i);
                        System.out.println("🔍 DEBUG: RDV " + rdv.getId() + 
                                         " - Patient: " + rdv.getPatientNom() + 
                                         ", Specialist: " + rdv.getSpecialisteNom() + 
                                         ", Motif: " + rdv.getMotif());
                    }
                    
                    String response = createMapDataResponse(rendezVous);
                    sendResponse(exchange, 200, response, "application/json");
                } else {
                    sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}", "application/json");
                }
            } catch (Exception e) {
                System.err.println("🔍 ERROR: Failed to fetch map data: " + e.getMessage());
                e.printStackTrace();
                sendResponse(exchange, 500, "{\"error\":\"Failed to fetch map data: " + e.getMessage() + "\"}", "application/json");
            }
        }
    }
    
    // Create mock data to test if display works
    private static List<RendezVous> createMockRendezVous() {
        List<RendezVous> mockList = new java.util.ArrayList<>();
        
        // Create mock rendezvous with real doctor names
        RendezVous rdv1 = new RendezVous();
        rdv1.setId(1);
        rdv1.setPatientNom("Patient A");
        rdv1.setSpecialisteNom("Dr. Chamem");
        rdv1.setMotif("Consultation générale");
        rdv1.setStatut("en attente");
        rdv1.setLieu("Cabinet médical Ariana");
        rdv1.setDateHeure(java.time.LocalDateTime.of(2024, 4, 30, 10, 0));
        mockList.add(rdv1);
        
        RendezVous rdv2 = new RendezVous();
        rdv2.setId(2);
        rdv2.setPatientNom("Patient B");
        rdv2.setSpecialisteNom("Dr. Ben Ali");
        rdv2.setMotif("Suivi cardiologique");
        rdv2.setStatut("confirmé");
        rdv2.setLieu("Clinique La Marsa");
        rdv2.setDateHeure(java.time.LocalDateTime.of(2024, 5, 1, 14, 30));
        mockList.add(rdv2);
        
        RendezVous rdv3 = new RendezVous();
        rdv3.setId(3);
        rdv3.setPatientNom("Patient C");
        rdv3.setSpecialisteNom("Dr. Mohamed");
        rdv3.setMotif("Pédiatrie");
        rdv3.setStatut("en attente");
        rdv3.setLieu("Hôpital Habib Thameur");
        rdv3.setDateHeure(java.time.LocalDateTime.of(2024, 5, 2, 9, 0));
        mockList.add(rdv3);
        
        return mockList;
    }
    
    static class StaticHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            
            if (path.contains("style.css")) {
                String css = generateCSS();
                sendResponse(exchange, 200, css, "text/css");
            } else if (path.contains("script.js")) {
                String js = generateJavaScript();
                sendResponse(exchange, 200, js, "application/javascript");
            } else {
                sendResponse(exchange, 404, "{\"error\":\"Static file not found\"}", "application/json");
            }
        }
    }
    
    private static String generateMapPage() {
        return "<!DOCTYPE html>\n" +
               "<html lang=\"fr\">\n" +
               "<head>\n" +
               "    <meta charset=\"UTF-8\">\n" +
               "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
               "    <title>🗺️ BioSync - Carte des Rendez-vous</title>\n" +
               "    <link rel=\"stylesheet\" href=\"https://unpkg.com/leaflet@1.9.4/dist/leaflet.css\">\n" +
               "    <link rel=\"stylesheet\" href=\"/api/maps/static/style.css\">\n" +
               "</head>\n" +
               "<body>\n" +
               "    <header>\n" +
               "        <h1>🗺️ BioSync - Carte des Rendez-vous</h1>\n" +
               "        <div class=\"controls\">\n" +
               "            <button id=\"btnAll\">Tous les RDV</button>\n" +
               "            <button id=\"btnPending\">En attente</button>\n" +
               "            <button id=\"btnConfirmed\">Confirmés</button>\n" +
               "            <button id=\"btnCancelled\">Annulés</button>\n" +
               "            <select id=\"specialistFilter\">\n" +
               "                <option value=\"\">Tous les médecins</option>\n" +
               "            </select>\n" +
               "        </div>\n" +
               "    </header>\n" +
               "    <main>\n" +
               "        <div id=\"map\"></div>\n" +
               "        <div class=\"sidebar\">\n" +
               "            <h2>📋 Rendez-vous</h2>\n" +
               "            <div id=\"rdvList\"></div>\n" +
               "        </div>\n" +
               "    </main>\n" +
               "    <div id=\"popupTemplate\" style=\"display:none;\">\n" +
               "        <div class=\"popup-content\">\n" +
               "            <h3>{{motif}}</h3>\n" +
               "            <p><strong>Patient:</strong> {{patientNom}}</p>\n" +
               "            <p><strong>Médecin:</strong> {{specialisteNom}}</p>\n" +
               "            <p><strong>Date:</strong> {{dateHeure}}</p>\n" +
               "            <p><strong>Lieu:</strong> {{lieu}}</p>\n" +
               "            <p><strong>Statut:</strong> <span class=\"status-{{statut}}\">{{statut}}</span></p>\n" +
               "        </div>\n" +
               "    </div>\n" +
               "    <script src=\"https://unpkg.com/leaflet@1.9.4/dist/leaflet.js\"></script>\n" +
               "    <script src=\"/api/maps/static/script.js\"></script>\n" +
               "</body>\n" +
               "</html>";
    }
    
    private static String generateCSS() {
        return "* {\n" +
               "    margin: 0;\n" +
               "    padding: 0;\n" +
               "    box-sizing: border-box;\n" +
               "}\n" +
               "\n" +
               "body {\n" +
               "    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;\n" +
               "    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);\n" +
               "    color: #333;\n" +
               "}\n" +
               "\n" +
               "header {\n" +
               "    background: rgba(255, 255, 255, 0.95);\n" +
               "    padding: 1rem;\n" +
               "    box-shadow: 0 2px 10px rgba(0,0,0,0.1);\n" +
               "    position: relative;\n" +
               "    z-index: 1000;\n" +
               "}\n" +
               "\n" +
               "header h1 {\n" +
               "    text-align: center;\n" +
               "    color: #4a5568;\n" +
               "    margin-bottom: 1rem;\n" +
               "}\n" +
               "\n" +
               ".controls {\n" +
               "    display: flex;\n" +
               "    justify-content: center;\n" +
               "    gap: 1rem;\n" +
               "    flex-wrap: wrap;\n" +
               "}\n" +
               "\n" +
               ".controls button {\n" +
               "    padding: 0.5rem 1rem;\n" +
               "    border: none;\n" +
               "    border-radius: 25px;\n" +
               "    background: linear-gradient(45deg, #667eea, #764ba2);\n" +
               "    color: white;\n" +
               "    cursor: pointer;\n" +
               "    transition: all 0.3s ease;\n" +
               "    font-weight: 600;\n" +
               "}\n" +
               "\n" +
               ".controls button:hover {\n" +
               "    transform: translateY(-2px);\n" +
               "    box-shadow: 0 5px 15px rgba(0,0,0,0.2);\n" +
               "}\n" +
               "\n" +
               ".controls button.active {\n" +
               "    background: #48bb78;\n" +
               "}\n" +
               "\n" +
               ".controls select {\n" +
               "    padding: 0.5rem 1rem;\n" +
               "    border: 2px solid #e2e8f0;\n" +
               "    border-radius: 25px;\n" +
               "    background: white;\n" +
               "    cursor: pointer;\n" +
               "}\n" +
               "\n" +
               "main {\n" +
               "    display: flex;\n" +
               "    height: calc(100vh - 120px);\n" +
               "}\n" +
               "\n" +
               "#map {\n" +
               "    flex: 1;\n" +
               "    height: 100%;\n" +
               "}\n" +
               "\n" +
               ".sidebar {\n" +
               "    width: 350px;\n" +
               "    background: rgba(255, 255, 255, 0.95);\n" +
               "    padding: 1rem;\n" +
               "    overflow-y: auto;\n" +
               "    box-shadow: -2px 0 10px rgba(0,0,0,0.1);\n" +
               "}\n" +
               "\n" +
               ".sidebar h2 {\n" +
               "    color: #4a5568;\n" +
               "    margin-bottom: 1rem;\n" +
               "    border-bottom: 2px solid #e2e8f0;\n" +
               "    padding-bottom: 0.5rem;\n" +
               "}\n" +
               "\n" +
               ".rdv-item {\n" +
               "    background: white;\n" +
               "    border-radius: 10px;\n" +
               "    padding: 1rem;\n" +
               "    margin-bottom: 1rem;\n" +
               "    box-shadow: 0 2px 5px rgba(0,0,0,0.1);\n" +
               "    cursor: pointer;\n" +
               "    transition: all 0.3s ease;\n" +
               "    border-left: 4px solid #cbd5e0;\n" +
               "}\n" +
               "\n" +
               ".rdv-item:hover {\n" +
               "    transform: translateX(-5px);\n" +
               "    box-shadow: 0 5px 15px rgba(0,0,0,0.2);\n" +
               "}\n" +
               "\n" +
               ".rdv-item.status-en_attente {\n" +
               "    border-left-color: #f6ad55;\n" +
               "}\n" +
               "\n" +
               ".rdv-item.status-confirmé {\n" +
               "    border-left-color: #48bb78;\n" +
               "}\n" +
               "\n" +
               ".rdv-item.status-annulé {\n" +
               "    border-left-color: #fc8181;\n" +
               "}\n" +
               "\n" +
               ".rdv-item h3 {\n" +
               "    color: #2d3748;\n" +
               "    margin-bottom: 0.5rem;\n" +
               "}\n" +
               "\n" +
               ".rdv-item p {\n" +
               "    color: #718096;\n" +
               "    margin: 0.25rem 0;\n" +
               "    font-size: 0.9rem;\n" +
               "}\n" +
               "\n" +
               ".status-en_attente {\n" +
               "    color: #d69e2e;\n" +
               "    font-weight: 600;\n" +
               "}\n" +
               "\n" +
               ".status-confirmé {\n" +
               "    color: #38a169;\n" +
               "    font-weight: 600;\n" +
               "}\n" +
               "\n" +
               ".status-annulé {\n" +
               "    color: #e53e3e;\n" +
               "    font-weight: 600;\n" +
               "}\n" +
               "\n" +
               ".popup-content h3 {\n" +
               "    color: #2d3748;\n" +
               "    margin-bottom: 0.5rem;\n" +
               "}\n" +
               "\n" +
               ".popup-content p {\n" +
               "    margin: 0.25rem 0;\n" +
               "    color: #4a5568;\n" +
               "}\n" +
               "\n" +
               ".leaflet-popup-content {\n" +
               "    min-width: 200px;\n" +
               "}\n" +
               "\n" +
               "@media (max-width: 768px) {\n" +
               "    main {\n" +
               "        flex-direction: column;\n" +
               "    }\n" +
               "    \n" +
               "    .sidebar {\n" +
               "        width: 100%;\n" +
               "        height: 200px;\n" +
               "    }\n" +
               "    \n" +
               "    .controls {\n" +
               "        flex-direction: column;\n" +
               "        align-items: center;\n" +
               "    }\n" +
               "}";
    }
    
    private static String generateJavaScript() {
        return "// Initialize the map\n" +
               "let map;\n" +
               "let markers = [];\n" +
               "let currentFilter = 'all';\n" +
               "let currentSpecialist = '';\n" +
               "\n" +
               "// Initialize map when page loads\n" +
               "document.addEventListener('DOMContentLoaded', function() {\n" +
               "    // Create map centered on Tunisia\n" +
               "    map = L.map('map').setView([33.8869, 9.5375], 7);\n" +
               "    \n" +
               "    // Add OpenStreetMap tiles\n" +
               "    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {\n" +
               "        attribution: '© OpenStreetMap contributors'\n" +
               "    }).addTo(map);\n" +
               "    \n" +
               "    // Load data\n" +
               "    loadMapData();\n" +
               "    \n" +
               "    // Setup event listeners\n" +
               "    setupEventListeners();\n" +
               "});\n" +
               "\n" +
               "function setupEventListeners() {\n" +
               "    // Filter buttons\n" +
               "    document.getElementById('btnAll').addEventListener('click', () => filterByStatus('all'));\n" +
               "    document.getElementById('btnPending').addEventListener('click', () => filterByStatus('en attente'));\n" +
               "    document.getElementById('btnConfirmed').addEventListener('click', () => filterByStatus('confirmé'));\n" +
               "    document.getElementById('btnCancelled').addEventListener('click', () => filterByStatus('annulé'));\n" +
               "    \n" +
               "    // Specialist filter\n" +
               "    document.getElementById('specialistFilter').addEventListener('change', (e) => {\n" +
               "        currentSpecialist = e.target.value;\n" +
               "        updateDisplay();\n" +
               "    });\n" +
               "}\n" +
               "\n" +
               "async function loadMapData() {\n" +
               "    try {\n" +
               "        const response = await fetch('/api/maps/data');\n" +
               "        const data = await response.json();\n" +
               "        \n" +
               "        if (data.success) {\n" +
               "            renderMapData(data.locations);\n" +
               "            populateSpecialistFilter(data.locations);\n" +
               "        }\n" +
               "    } catch (error) {\n" +
               "        console.error('Error loading map data:', error);\n" +
               "    }\n" +
               "}\n" +
               "\n" +
               "function renderMapData(locations) {\n" +
               "    // Clear existing markers\n" +
               "    markers.forEach(marker => map.removeLayer(marker));\n" +
               "    markers = [];\n" +
               "    \n" +
               "    // Add markers for each location\n" +
               "    locations.forEach(location => {\n" +
               "        const marker = L.marker([location.coordinates.lat, location.coordinates.lng])\n" +
               "            .addTo(map)\n" +
               "            .bindPopup(createPopupContent(location));\n" +
               "        \n" +
               "        marker.locationData = location;\n" +
               "        markers.push(marker);\n" +
               "    });\n" +
               "    \n" +
               "    // Update sidebar\n" +
               "    updateSidebar(locations);\n" +
               "    \n" +
               "    // Apply current filters\n" +
               "    updateDisplay();\n" +
               "}\n" +
               "\n" +
               "function createPopupContent(location) {\n" +
               "    return `\n" +
               "        <div class=\"popup-content\">\n" +
               "            <h3>${location.motif}</h3>\n" +
               "            <p><strong>Patient:</strong> ${location.patientNom}</p>\n" +
               "            <p><strong>Médecin:</strong> ${location.specialisteNom}</p>\n" +
               "            <p><strong>Date:</strong> ${location.dateHeure}</p>\n" +
               "            <p><strong>Lieu:</strong> ${location.lieu}</p>\n" +
               "            <p><strong>Statut:</strong> <span class=\"status-${location.statut}\">${location.statut}</span></p>\n" +
               "        </div>\n" +
               "    `;\n" +
               "}\n" +
               "\n" +
               "function updateSidebar(locations) {\n" +
               "    const rdvList = document.getElementById('rdvList');\n" +
               "    rdvList.innerHTML = '';\n" +
               "    \n" +
               "    locations.forEach(location => {\n" +
               "        const item = document.createElement('div');\n" +
               "        item.className = `rdv-item status-${location.statut}`;\n" +
               "        item.innerHTML = `\n" +
               "            <h3>${location.motif}</h3>\n" +
               "            <p><strong>Patient:</strong> ${location.patientNom}</p>\n" +
               "            <p><strong>Médecin:</strong> ${location.specialisteNom}</p>\n" +
               "            <p><strong>Date:</strong> ${location.dateHeure}</p>\n" +
               "            <p><strong>Lieu:</strong> ${location.lieu}</p>\n" +
               "            <p><strong>Statut:</strong> <span class=\"status-${location.statut}\">${location.statut}</span></p>\n" +
               "        `;\n" +
               "        \n" +
               "        // Add click event to focus on map marker\n" +
               "        item.addEventListener('click', () => {\n" +
               "            map.setView([location.coordinates.lat, location.coordinates.lng], 12);\n" +
               "            // Find and open the corresponding marker popup\n" +
               "            const marker = markers.find(m => m.locationData.id === location.id);\n" +
               "            if (marker) {\n" +
               "                marker.openPopup();\n" +
               "            }\n" +
               "        });\n" +
               "        \n" +
               "        rdvList.appendChild(item);\n" +
               "    });\n" +
               "}\n" +
               "\n" +
               "function populateSpecialistFilter(locations) {\n" +
               "    const specialistFilter = document.getElementById('specialistFilter');\n" +
               "    const specialists = [...new Set(locations.map(loc => loc.specialisteNom))];\n" +
               "    \n" +
               "    console.log('🔍 DEBUG: Found specialists:', specialists);\n" +
               "    \n" +
               "    specialistFilter.innerHTML = '<option value=\"\">Tous les médecins</option>';\n" +
               "    specialists.forEach(specialist => {\n" +
               "        const option = document.createElement('option');\n" +
               "        option.value = specialist;\n" +
               "        option.textContent = specialist;\n" +
               "        specialistFilter.appendChild(option);\n" +
               "    });\n" +
               "    \n" +
               "    console.log('🔍 DEBUG: Specialist filter populated with', specialists.length, 'options');\n" +
               "}\n" +
               "\n" +
               "function filterByStatus(status) {\n" +
               "    currentFilter = status;\n" +
               "    \n" +
               "    // Update button states\n" +
               "    document.querySelectorAll('.controls button').forEach(btn => btn.classList.remove('active'));\n" +
               "    \n" +
               "    if (status === 'all') {\n" +
               "        document.getElementById('btnAll').classList.add('active');\n" +
               "    } else if (status === 'en attente') {\n" +
               "        document.getElementById('btnPending').classList.add('active');\n" +
               "    } else if (status === 'confirmé') {\n" +
               "        document.getElementById('btnConfirmed').classList.add('active');\n" +
               "    } else if (status === 'annulé') {\n" +
               "        document.getElementById('btnCancelled').classList.add('active');\n" +
               "    }\n" +
               "    \n" +
               "    updateDisplay();\n" +
               "}\n" +
               "\n" +
               "function updateDisplay() {\n" +
               "    markers.forEach(marker => {\n" +
               "        const location = marker.locationData;\n" +
               "        const statusMatch = currentFilter === 'all' || location.statut === currentFilter;\n" +
               "        const specialistMatch = !currentSpecialist || location.specialisteNom === currentSpecialist;\n" +
               "        const shouldShow = statusMatch && specialistMatch;\n" +
               "        \n" +
               "        if (shouldShow) {\n" +
               "            marker.addTo(map);\n" +
               "        } else {\n" +
               "            map.removeLayer(marker);\n" +
               "        }\n" +
               "    });\n" +
               "    \n" +
               "    // Update sidebar\n" +
               "    const sidebarItems = document.querySelectorAll('.rdv-item');\n" +
               "    sidebarItems.forEach(item => {\n" +
               "        const itemStatus = item.querySelector('.status-en_attente, .status-confirmé, .status-annulé')?.textContent;\n" +
               "        const itemSpecialist = item.querySelector('p:nth-child(3)').textContent.replace('Médecin: ', '');\n" +
               "        \n" +
               "        const statusMatch = currentFilter === 'all' || itemStatus === currentFilter;\n" +
               "        const specialistMatch = !currentSpecialist || itemSpecialist === currentSpecialist;\n" +
               "        \n" +
               "        item.style.display = (statusMatch && specialistMatch) ? 'block' : 'none';\n" +
               "    });\n" +
               "}";
    }
    
    private static String createMapDataResponse(List<RendezVous> rendezVous) {
        StringBuilder json = new StringBuilder();
        json.append("{\"success\":true,\"count\":").append(rendezVous.size()).append(",\"locations\":[");
        
        for (int i = 0; i < rendezVous.size(); i++) {
            if (i > 0) json.append(",");
            RendezVous rdv = rendezVous.get(i);
            json.append(createMapLocationJson(rdv));
        }
        
        json.append("]}");
        return json.toString();
    }
    
    private static String createMapLocationJson(RendezVous rdv) {
        // Generate coordinates for Tunisia region
        double lat = 33.8869 + (Math.random() - 0.5) * 2; // Tunisia center ±1°
        double lng = 9.5375 + (Math.random() - 0.5) * 2;  // Tunisia center ±1°
        
        String specialistNom = rdv.getSpecialisteNom();
        System.out.println("🔍 DEBUG: RDV " + rdv.getId() + " - Specialist: " + specialistNom);
        if (specialistNom == null || specialistNom.trim().isEmpty()) {
            specialistNom = "Dr. Non spécifié";
            System.out.println("🔍 DEBUG: Using default specialist name");
        }
        
        return "{" +
                "\"id\":" + rdv.getId() + "," +
                "\"motif\":\"" + (rdv.getMotif() != null ? rdv.getMotif().replace("\"", "\\\"") : "") + "\"," +
                "\"patientNom\":\"" + (rdv.getPatientNom() != null ? rdv.getPatientNom().replace("\"", "\\\"") : "") + "\"," +
                "\"specialisteNom\":\"" + specialistNom.replace("\"", "\\\"") + "\"," +
                "\"dateHeure\":\"" + (rdv.getDateHeure() != null ? rdv.getDateHeure().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "") + "\"," +
                "\"lieu\":\"" + (rdv.getLieu() != null ? rdv.getLieu().replace("\"", "\\\"") : "") + "\"," +
                "\"statut\":\"" + (rdv.getStatut() != null ? rdv.getStatut() : "") + "\"," +
                "\"coordinates\":{" +
                "\"lat\":" + lat + "," +
                "\"lng\":" + lng +
                "}," +
                "\"address\":\"" + (rdv.getLieu() != null ? rdv.getLieu() + ", Tunisie" : "Tunisie") + "\"" +
                "}";
    }
    
    private static void sendResponse(HttpExchange exchange, int statusCode, String response, String contentType) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes(StandardCharsets.UTF_8));
        os.close();
    }
}

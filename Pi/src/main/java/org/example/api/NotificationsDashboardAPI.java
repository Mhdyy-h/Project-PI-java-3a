package org.example.api;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import org.example.dao.RendezVousDAO;
import org.example.dao.UserDAO;
import org.example.model.RendezVous;
import org.example.model.User;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Notifications Dashboard API pour BioSync
 * Interface web pour visualiser et envoyer des notifications
 */
public class NotificationsDashboardAPI {
    
    private static final int DEFAULT_PORT = 8084;
    
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
        server.createContext("/api/dashboard/health", new HealthHandler());
        
        // Dashboard page
        server.createContext("/api/dashboard", new DashboardHandler());
        
        // Send notification
        server.createContext("/api/dashboard/send", new SendNotificationHandler());
        
        // Get rendezvous list
        server.createContext("/api/dashboard/rendezvous", new RendezVousListHandler());
        
        server.setExecutor(null);
        server.start();
        
        System.out.println("📱 BioSync Notifications Dashboard started on http://localhost:" + port);
        System.out.println("🌐 Open your browser and go to: http://localhost:" + port + "/api/dashboard");
        System.out.println("📧 Available actions:");
        System.out.println("  - Send SMS reminders");
        System.out.println("  - Send email confirmations");
        System.out.println("  - Send cancellation notifications");
        System.out.println("  - Send delay notifications");
    }
    
    static class HealthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "{\"status\":\"healthy\",\"service\":\"BioSync Notifications Dashboard\",\"timestamp\":" + System.currentTimeMillis() + "}";
            sendResponse(exchange, 200, response, "application/json");
        }
    }
    
    static class DashboardHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                String htmlPage = generateDashboardPage();
                sendResponse(exchange, 200, htmlPage, "text/html");
            } else {
                sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}", "application/json");
            }
        }
    }
    
    static class RendezVousListHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                if ("GET".equals(exchange.getRequestMethod())) {
                    List<RendezVous> rendezVous = RendezVousDAO.getAllRendezVous();
                    String response = createRendezVousListResponse(rendezVous);
                    sendResponse(exchange, 200, response, "application/json");
                } else {
                    sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}", "application/json");
                }
            } catch (Exception e) {
                sendResponse(exchange, 500, "{\"error\":\"Failed to fetch rendezvous: " + e.getMessage() + "\"}", "application/json");
            }
        }
    }
    
    static class SendNotificationHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                if ("POST".equals(exchange.getRequestMethod())) {
                    String query = exchange.getRequestURI().getQuery();
                    Map<String, String> params = parseQueryParams(query);
                    
                    String type = params.get("type");
                    int id = Integer.parseInt(params.get("id"));
                    
                    boolean success = false;
                    String message = "";
                    
                    switch (type) {
                        case "sms_reminder":
                            success = sendSMSReminder(id);
                            message = "SMS reminder sent successfully";
                            break;
                        case "email_confirmation":
                            success = sendEmailConfirmation(id);
                            message = "Email confirmation sent successfully";
                            break;
                        case "cancellation":
                            success = sendCancellation(id);
                            message = "Cancellation notifications sent successfully";
                            break;
                        case "delay":
                            int minutes = Integer.parseInt(params.getOrDefault("minutes", "15"));
                            success = sendDelayNotification(id, minutes);
                            message = "Delay notification sent successfully";
                            break;
                        default:
                            message = "Unknown notification type";
                    }
                    
                    String response = "{" +
                            "\"success\":" + success + "," +
                            "\"message\":\"" + message + "\"," +
                            "\"type\":\"" + type + "\"," +
                            "\"appointmentId\":" + id +
                            "}";
                    
                    sendResponse(exchange, success ? 200 : 400, response, "application/json");
                } else {
                    sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}", "application/json");
                }
            } catch (Exception e) {
                sendResponse(exchange, 500, "{\"error\":\"Failed to send notification: " + e.getMessage() + "\"}", "application/json");
            }
        }
    }
    
    private static String generateDashboardPage() {
        return "<!DOCTYPE html>\n" +
               "<html lang=\"fr\">\n" +
               "<head>\n" +
               "    <meta charset=\"UTF-8\">\n" +
               "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
               "    <title>📱 BioSync - Notifications Dashboard</title>\n" +
               "    <style>\n" +
               generateDashboardCSS() +
               "    </style>\n" +
               "</head>\n" +
               "<body>\n" +
               "    <div class=\"container\">\n" +
               "        <header>\n" +
               "            <h1>📱 BioSync Notifications Dashboard</h1>\n" +
               "            <p>Envoyez des notifications SMS et Email pour les rendez-vous</p>\n" +
               "        </header>\n" +
               "        \n" +
               "        <div class=\"controls\">\n" +
               "            <div class=\"control-group\">\n" +
               "                <label for=\"rendezvousSelect\">Sélectionner un RDV:</label>\n" +
               "                <select id=\"rendezvousSelect\">\n" +
               "                    <option value=\"\">Chargement...</option>\n" +
               "                </select>\n" +
               "            </div>\n" +
               "            \n" +
               "            <div class=\"button-group\">\n" +
               "                <button id=\"btnSMSReminder\" class=\"btn btn-sms\">📱 SMS Rappel</button>\n" +
               "                <button id=\"btnEmailConfirmation\" class=\"btn btn-email\">📧 Email Confirmation</button>\n" +
               "                <button id=\"btnCancellation\" class=\"btn btn-cancel\">❌ Annulation</button>\n" +
               "                <button id=\"btnDelay\" class=\"btn btn-delay\">⏰ Retard</button>\n" +
               "            </div>\n" +
               "            \n" +
               "            <div class=\"delay-controls\">\n" +
               "                <label for=\"delayMinutes\">Minutes de retard:</label>\n" +
               "                <input type=\"number\" id=\"delayMinutes\" value=\"15\" min=\"5\" max=\"120\">\n" +
               "            </div>\n" +
               "        </div>\n" +
               "        \n" +
               "        <div class=\"results\">\n" +
               "            <div id=\"selectedRDV\" class=\"selected-rdv\">\n" +
               "                <h3>📋 Rendez-vous Sélectionné</h3>\n" +
               "                <div id=\"rdvDetails\">Sélectionnez un rendez-vous...</div>\n" +
               "            </div>\n" +
               "            \n" +
               "            <div id=\"notificationResult\" class=\"notification-result\">\n" +
               "                <h3>📤 Résultat de la Notification</h3>\n" +
               "                <div id=\"resultDetails\">En attente d'action...</div>\n" +
               "            </div>\n" +
               "        </div>\n" +
               "    </div>\n" +
               "    \n" +
               "    <script>\n" +
               generateDashboardJS() +
               "    </script>\n" +
               "</body>\n" +
               "</html>";
    }
    
    private static String generateDashboardCSS() {
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
               "    min-height: 100vh;\n" +
               "    padding: 20px;\n" +
               "}\n" +
               "\n" +
               ".container {\n" +
               "    max-width: 1200px;\n" +
               "    margin: 0 auto;\n" +
               "    background: rgba(255, 255, 255, 0.95);\n" +
               "    border-radius: 15px;\n" +
               "    box-shadow: 0 10px 30px rgba(0,0,0,0.1);\n" +
               "    overflow: hidden;\n" +
               "}\n" +
               "\n" +
               "header {\n" +
               "    background: linear-gradient(45deg, #667eea, #764ba2);\n" +
               "    color: white;\n" +
               "    padding: 2rem;\n" +
               "    text-align: center;\n" +
               "}\n" +
               "\n" +
               "header h1 {\n" +
               "    font-size: 2.5rem;\n" +
               "    margin-bottom: 0.5rem;\n" +
               "}\n" +
               "\n" +
               "header p {\n" +
               "    font-size: 1.1rem;\n" +
               "    opacity: 0.9;\n" +
               "}\n" +
               "\n" +
               ".controls {\n" +
               "    padding: 2rem;\n" +
               "    background: #f8f9fa;\n" +
               "    border-bottom: 1px solid #e9ecef;\n" +
               "}\n" +
               "\n" +
               ".control-group {\n" +
               "    margin-bottom: 2rem;\n" +
               "}\n" +
               "\n" +
               ".control-group label {\n" +
               "    display: block;\n" +
               "    font-weight: 600;\n" +
               "    color: #495057;\n" +
               "    margin-bottom: 0.5rem;\n" +
               "}\n" +
               "\n" +
               "#rendezvousSelect {\n" +
               "    width: 100%;\n" +
               "    padding: 1rem;\n" +
               "    border: 2px solid #dee2e6;\n" +
               "    border-radius: 10px;\n" +
               "    font-size: 1rem;\n" +
               "    background: white;\n" +
               "}\n" +
               "\n" +
               ".button-group {\n" +
               "    display: grid;\n" +
               "    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));\n" +
               "    gap: 1rem;\n" +
               "    margin-bottom: 2rem;\n" +
               "}\n" +
               "\n" +
               ".btn {\n" +
               "    padding: 1rem 1.5rem;\n" +
               "    border: none;\n" +
               "    border-radius: 10px;\n" +
               "    font-size: 1rem;\n" +
               "    font-weight: 600;\n" +
               "    cursor: pointer;\n" +
               "    transition: all 0.3s ease;\n" +
               "    color: white;\n" +
               "    text-align: center;\n" +
               "}\n" +
               "\n" +
               ".btn:hover {\n" +
               "    transform: translateY(-2px);\n" +
               "    box-shadow: 0 5px 15px rgba(0,0,0,0.2);\n" +
               "}\n" +
               "\n" +
               ".btn:disabled {\n" +
               "    opacity: 0.5;\n" +
               "    cursor: not-allowed;\n" +
               "    transform: none;\n" +
               "}\n" +
               "\n" +
               ".btn-sms {\n" +
               "    background: linear-gradient(45deg, #28a745, #20c997);\n" +
               "}\n" +
               "\n" +
               ".btn-email {\n" +
               "    background: linear-gradient(45deg, #007bff, #0056b3);\n" +
               "}\n" +
               "\n" +
               ".btn-cancel {\n" +
               "    background: linear-gradient(45deg, #dc3545, #c82333);\n" +
               "}\n" +
               "\n" +
               ".btn-delay {\n" +
               "    background: linear-gradient(45deg, #ffc107, #e0a800);\n" +
               "}\n" +
               "\n" +
               ".delay-controls {\n" +
               "    display: flex;\n" +
               "    align-items: center;\n" +
               "    gap: 1rem;\n" +
               "    margin-bottom: 2rem;\n" +
               "}\n" +
               "\n" +
               ".delay-controls label {\n" +
               "    font-weight: 600;\n" +
               "    color: #495057;\n" +
               "}\n" +
               "\n" +
               "#delayMinutes {\n" +
               "    padding: 0.5rem;\n" +
               "    border: 2px solid #dee2e6;\n" +
               "    border-radius: 5px;\n" +
               "    width: 100px;\n" +
               "    font-size: 1rem;\n" +
               "}\n" +
               "\n" +
               ".results {\n" +
               "    display: grid;\n" +
               "    grid-template-columns: 1fr 1fr;\n" +
               "    gap: 2rem;\n" +
               "    padding: 2rem;\n" +
               "}\n" +
               "\n" +
               ".selected-rdv, .notification-result {\n" +
               "    background: white;\n" +
               "    border-radius: 10px;\n" +
               "    padding: 1.5rem;\n" +
               "    box-shadow: 0 2px 10px rgba(0,0,0,0.1);\n" +
               "}\n" +
               "\n" +
               ".selected-rdv h3, .notification-result h3 {\n" +
               "    color: #495057;\n" +
               "    margin-bottom: 1rem;\n" +
               "    border-bottom: 2px solid #e9ecef;\n" +
               "    padding-bottom: 0.5rem;\n" +
               "}\n" +
               "\n" +
               "#rdvDetails {\n" +
               "    color: #6c757d;\n" +
               "    line-height: 1.6;\n" +
               "}\n" +
               "\n" +
               "#resultDetails {\n" +
               "    color: #6c757d;\n" +
               "    line-height: 1.6;\n" +
               "}\n" +
               "\n" +
               ".success {\n" +
               "    color: #28a745;\n" +
               "    font-weight: 600;\n" +
               "}\n" +
               "\n" +
               ".error {\n" +
               "    color: #dc3545;\n" +
               "    font-weight: 600;\n" +
               "}\n" +
               "\n" +
               "@media (max-width: 768px) {\n" +
               "    .results {\n" +
               "        grid-template-columns: 1fr;\n" +
               "    }\n" +
               "    \n" +
               "    .button-group {\n" +
               "        grid-template-columns: 1fr;\n" +
               "    }\n" +
               "}";
    }
    
    private static String generateDashboardJS() {
        return "// Load rendezvous data\n" +
               "let rendezvousList = [];\n" +
               "let selectedRDV = null;\n" +
               "\n" +
               "// Initialize dashboard\n" +
               "document.addEventListener('DOMContentLoaded', function() {\n" +
               "    loadRendezvous();\n" +
               "    setupEventListeners();\n" +
               "});\n" +
               "\n" +
               "async function loadRendezvous() {\n" +
               "    try {\n" +
               "        const response = await fetch('/api/dashboard/rendezvous');\n" +
               "        const data = await response.json();\n" +
               "        \n" +
               "        if (data.success) {\n" +
               "            rendezvousList = data.rendezvous;\n" +
               "            populateRendezvousSelect();\n" +
               "        }\n" +
               "    } catch (error) {\n" +
               "        console.error('Error loading rendezvous:', error);\n" +
               "    }\n" +
               "}\n" +
               "\n" +
               "function populateRendezvousSelect() {\n" +
               "    const select = document.getElementById('rendezvousSelect');\n" +
               "    select.innerHTML = '<option value=\"\">Sélectionner un RDV...</option>';\n" +
               "    \n" +
               "    rendezvousList.forEach(rdv => {\n" +
               "        const option = document.createElement('option');\n" +
               "        option.value = rdv.id;\n" +
               "        option.textContent = `RDV #${rdv.id} - ${rdv.motif} (${rdv.patientNom})`;\n" +
               "        select.appendChild(option);\n" +
               "    });\n" +
               "}\n" +
               "\n" +
               "function setupEventListeners() {\n" +
               "    // Rendezvous selection\n" +
               "    document.getElementById('rendezvousSelect').addEventListener('change', function(e) {\n" +
               "        const rdvId = parseInt(e.target.value);\n" +
               "        selectedRDV = rendezvousList.find(r => r.id === rdvId);\n" +
               "        updateSelectedRDVDisplay();\n" +
               "    });\n" +
               "    \n" +
               "    // Notification buttons\n" +
               "    document.getElementById('btnSMSReminder').addEventListener('click', () => sendNotification('sms_reminder'));\n" +
               "    document.getElementById('btnEmailConfirmation').addEventListener('click', () => sendNotification('email_confirmation'));\n" +
               "    document.getElementById('btnCancellation').addEventListener('click', () => sendNotification('cancellation'));\n" +
               "    document.getElementById('btnDelay').addEventListener('click', () => sendNotification('delay'));\n" +
               "}\n" +
               "\n" +
               "function updateSelectedRDVDisplay() {\n" +
               "    const detailsDiv = document.getElementById('rdvDetails');\n" +
               "    \n" +
               "    if (selectedRDV) {\n" +
               "        detailsDiv.innerHTML = `\n" +
               "            <p><strong>ID:</strong> ${selectedRDV.id}</p>\n" +
               "            <p><strong>Patient:</strong> ${selectedRDV.patientNom}</p>\n" +
               "            <p><strong>Médecin:</strong> ${selectedRDV.specialisteNom}</p>\n" +
               "            <p><strong>Motif:</strong> ${selectedRDV.motif}</p>\n" +
               "            <p><strong>Date:</strong> ${selectedRDV.dateHeure}</p>\n" +
               "            <p><strong>Lieu:</strong> ${selectedRDV.lieu}</p>\n" +
               "            <p><strong>Statut:</strong> <span class=\"status-${selectedRDV.statut}\">${selectedRDV.statut}</span></p>\n" +
               "        `;\n" +
               "    } else {\n" +
               "        detailsDiv.innerHTML = 'Sélectionnez un rendez-vous...';\n" +
               "    }\n" +
               "}\n" +
               "\n" +
               "async function sendNotification(type) {\n" +
               "    if (!selectedRDV) {\n" +
               "        alert('Veuillez sélectionner un rendez-vous d\\'abord.');\n" +
               "        return;\n" +
               "    }\n" +
               "    \n" +
               "    const resultDiv = document.getElementById('resultDetails');\n" +
               "    resultDiv.innerHTML = '<p>📤 Envoi en cours...</p>';\n" +
               "    \n" +
               "    try {\n" +
               "        let url = `/api/dashboard/send?type=${type}&id=${selectedRDV.id}`;\n" +
               "        \n" +
               "        if (type === 'delay') {\n" +
               "            const minutes = document.getElementById('delayMinutes').value;\n" +
               "            url += `&minutes=${minutes}`;\n" +
               "        }\n" +
               "        \n" +
               "        const response = await fetch(url, { method: 'POST' });\n" +
               "        const result = await response.json();\n" +
               "        \n" +
               "        if (result.success) {\n" +
               "            resultDiv.innerHTML = `<p class=\"success\">✅ ${result.message}</p>`;\n" +
               "        } else {\n" +
               "            resultDiv.innerHTML = `<p class=\"error\">❌ ${result.message}</p>`;\n" +
               "        }\n" +
               "    } catch (error) {\n" +
               "        resultDiv.innerHTML = `<p class=\"error\">❌ Erreur: ${error.message}</p>`;\n" +
               "    }\n" +
               "}";
    }
    
    private static String createRendezVousListResponse(List<RendezVous> rendezVous) {
        StringBuilder json = new StringBuilder();
        json.append("{\"success\":true,\"count\":").append(rendezVous.size()).append(",\"rendezvous\":[");
        
        for (int i = 0; i < rendezVous.size(); i++) {
            if (i > 0) json.append(",");
            RendezVous rdv = rendezVous.get(i);
            json.append("{")
               .append("\"id\":").append(rdv.getId()).append(",")
               .append("\"motif\":\"").append(rdv.getMotif() != null ? rdv.getMotif().replace("\"", "\\\"") : "").append("\",")
               .append("\"patientNom\":\"").append(rdv.getPatientNom() != null ? rdv.getPatientNom().replace("\"", "\\\"") : "").append("\",")
               .append("\"specialisteNom\":\"").append(rdv.getSpecialisteNom() != null ? rdv.getSpecialisteNom().replace("\"", "\\\"") : "").append("\",")
               .append("\"dateHeure\":\"").append(rdv.getDateHeure() != null ? rdv.getDateHeure().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "").append("\",")
               .append("\"lieu\":\"").append(rdv.getLieu() != null ? rdv.getLieu().replace("\"", "\\\"") : "").append("\",")
               .append("\"statut\":\"").append(rdv.getStatut() != null ? rdv.getStatut() : "").append("\"")
               .append("}");
        }
        
        json.append("]}");
        return json.toString();
    }
    
    // Mock notification methods (replace with real implementations)
    private static boolean sendSMSReminder(int id) {
        try {
            RendezVous rdv = RendezVousDAO.getRendezVousById(id);
            if (rdv != null && rdv.getPatientId() != null) {
                User patient = UserDAO.getUserById(rdv.getPatientId());
                if (patient != null) {
                    String message = "📅 Rappel: Votre RDV " + rdv.getMotif() + 
                                   " le " + rdv.getDateHeure().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + 
                                   " à " + rdv.getLieu() + 
                                   ". Présentez-vous 15min en avance.";
                    
                    System.out.println("📱 SMS sent to " + rdv.getPatientNom() + ": " + message);
                    return true;
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error sending SMS reminder: " + e.getMessage());
        }
        return false;
    }
    
    private static boolean sendEmailConfirmation(int id) {
        try {
            RendezVous rdv = RendezVousDAO.getRendezVousById(id);
            if (rdv != null && rdv.getPatientId() != null) {
                User patient = UserDAO.getUserById(rdv.getPatientId());
                if (patient != null) {
                    String message = "✅ Votre rendez-vous est confirmé:\n" +
                                   "📋 Motif: " + rdv.getMotif() + "\n" +
                                   "📅 Date: " + rdv.getDateHeure().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "\n" +
                                   "📍 Lieu: " + rdv.getLieu() + "\n" +
                                   "👨‍⚕️ Médecin: " + rdv.getSpecialisteNom() + "\n\n" +
                                   "Merci de votre confiance.";
                    
                    System.out.println("📧 Email sent to " + rdv.getPatientNom());
                    return true;
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error sending email confirmation: " + e.getMessage());
        }
        return false;
    }
    
    private static boolean sendCancellation(int id) {
        try {
            RendezVous rdv = RendezVousDAO.getRendezVousById(id);
            if (rdv != null && rdv.getPatientId() != null) {
                User patient = UserDAO.getUserById(rdv.getPatientId());
                if (patient != null) {
                    String message = "❌ Votre rendez-vous a été annulé:\n" +
                                   "📋 Motif: " + rdv.getMotif() + "\n" +
                                   "📅 Date prévue: " + rdv.getDateHeure().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "\n\n" +
                                   "Contactez-nous pour un nouveau rendez-vous.";
                    
                    System.out.println("❌ Cancellation sent to " + rdv.getPatientNom());
                    return true;
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error sending cancellation: " + e.getMessage());
        }
        return false;
    }
    
    private static boolean sendDelayNotification(int id, int minutes) {
        try {
            RendezVous rdv = RendezVousDAO.getRendezVousById(id);
            if (rdv != null && rdv.getPatientId() != null) {
                User patient = UserDAO.getUserById(rdv.getPatientId());
                if (patient != null) {
                    String message = "⏰ Votre rendez-vous est retardé de " + minutes + " minutes:\n" +
                                   "📋 Motif: " + rdv.getMotif() + "\n" +
                                   "📅 Nouvelle heure: " + rdv.getDateHeure().plusMinutes(minutes).format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "\n" +
                                   "📍 Lieu: " + rdv.getLieu() + "\n\n" +
                                   "Nous nous excusons pour le désagrément.";
                    
                    System.out.println("⏰ Delay notification sent to " + rdv.getPatientNom());
                    return true;
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error sending delay notification: " + e.getMessage());
        }
        return false;
    }
    
    private static void sendResponse(HttpExchange exchange, int statusCode, String response, String contentType) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes(StandardCharsets.UTF_8));
        os.close();
    }
    
    private static Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new HashMap<>();
        if (query != null) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    params.put(keyValue[0], keyValue[1]);
                }
            }
        }
        return params;
    }
}

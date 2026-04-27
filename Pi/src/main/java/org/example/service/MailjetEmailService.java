package org.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Service d'envoi d'emails via API Mailjet.
 * Alternative pour la réinitialisation de mot de passe.
 * 6,000 emails/mois gratuits.
 */
public class MailjetEmailService {

    private static final String MAILJET_API_URL = "https://api.mailjet.com/v3.1/send";
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();

    private final String apiKey;
    private final String apiSecret;
    private final String fromEmail;
    private final String fromName;
    private final boolean enabled;

    private static MailjetEmailService instance;

    private MailjetEmailService() {
        Properties props = loadConfig();
        this.apiKey = props.getProperty("mailjet.api.key", "");
        this.apiSecret = props.getProperty("mailjet.api.secret", "");
        this.fromEmail = props.getProperty("mailjet.from.email", "noreply@biosync.com");
        this.fromName = props.getProperty("mailjet.from.name", "BioSync");
        this.enabled = !apiKey.isEmpty() && !apiSecret.isEmpty();

        System.out.println("[Mailjet] DEBUG: apiKey=" + (apiKey.isEmpty() ? "VIDE" : apiKey.substring(0, Math.min(8, apiKey.length())) + "..."));
        System.out.println("[Mailjet] DEBUG: apiSecret=" + (apiSecret.isEmpty() ? "VIDE" : apiSecret.substring(0, Math.min(8, apiSecret.length())) + "..."));
        System.out.println("[Mailjet] DEBUG: fromEmail=" + fromEmail);
        System.out.println("[Mailjet] DEBUG: enabled=" + enabled);

        if (!enabled) {
            System.out.println("MailjetEmailService: Non configuré. Envoi d'emails désactivé.");
        } else {
            System.out.println("MailjetEmailService: Configuré avec succès.");
        }
    }

    private Properties loadConfig() {
        Properties props = new Properties();
        try (InputStream is = getClass().getResourceAsStream("/config.properties")) {
            if (is != null) {
                props.load(is);
                System.out.println("[Mailjet] Configuration chargée depuis config.properties");
            } else {
                System.err.println("[Mailjet] config.properties non trouvé!");
            }
        } catch (IOException e) {
            System.err.println("[Mailjet] Erreur chargement config: " + e.getMessage());
        }
        return props;
    }

    public static synchronized MailjetEmailService getInstance() {
        if (instance == null) {
            instance = new MailjetEmailService();
        }
        return instance;
    }

    /**
     * Envoie un email de réinitialisation de mot de passe.
     *
     * @param toEmail   Adresse du destinataire
     * @param code      Code à 6 chiffres
     * @param userName  Nom de l'utilisateur
     * @return true si l'email a été envoyé avec succès
     */
    public boolean sendPasswordResetEmail(String toEmail, String code, String userName) {
        if (!enabled) {
            System.out.println("[Mailjet] Non configuré - Code pour " + toEmail + ": " + code);
            simulateSend(toEmail, code, userName);
            return false;
        }

        try {
            // Corps JSON Mailjet v3.1
            Map<String, Object> emailData = Map.of(
                "Messages", List.of(Map.of(
                    "From", Map.of(
                        "Email", fromEmail,
                        "Name", fromName
                    ),
                    "To", List.of(Map.of(
                        "Email", toEmail,
                        "Name", userName
                    )),
                    "Subject", "Réinitialisation de votre mot de passe - BioSync",
                    "HTMLPart", buildEmailHtml(userName, code)
                ))
            );

            String json = mapper.writeValueAsString(emailData);

            // Authentification Basic: apiKey:apiSecret
            String auth = apiKey + ":" + apiSecret;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(MAILJET_API_URL))
                .header("Authorization", "Basic " + encodedAuth)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            boolean success = response.statusCode() == 200;

            if (success) {
                System.out.println("[Mailjet] Email envoyé avec succès à " + toEmail);
            } else {
                System.err.println("[Mailjet] Erreur " + response.statusCode() + ": " + response.body());
            }

            return success;

        } catch (Exception e) {
            System.err.println("[Mailjet] Exception lors de l'envoi: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Vérifie si le service est configuré et prêt à envoyer des emails.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Affiche le code dans la console pour le mode test.
     */
    private void simulateSend(String toEmail, String code, String userName) {
        System.out.println("\n========================================");
        System.out.println("EMAIL DE RÉINITIALISATION (SIMULATION)");
        System.out.println("========================================");
        System.out.println("À: " + toEmail);
        System.out.println("Utilisateur: " + userName);
        System.out.println("Code: " + code);
        System.out.println("========================================\n");
    }

    /**
     * Génère le HTML de l'email de réinitialisation.
     */
    private String buildEmailHtml(String userName, String code) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: 'Segoe UI', Arial, sans-serif; background: #f3f4f6; margin: 0; padding: 20px; }
                    .container { max-width: 500px; margin: 0 auto; background: white; border-radius: 16px; padding: 40px; box-shadow: 0 10px 25px rgba(0,0,0,0.1); }
                    h2 { color: #4C6FFF; margin-top: 0; font-size: 24px; }
                    .code-box { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); border-radius: 12px; padding: 25px; text-align: center; margin: 25px 0; }
                    .code { font-size: 36px; font-weight: bold; color: white; letter-spacing: 6px; text-shadow: 0 2px 4px rgba(0,0,0,0.2); }
                    .expiry { color: rgba(255,255,255,0.9); font-size: 13px; margin-top: 12px; }
                    .content { color: #374151; line-height: 1.6; }
                    .footer { color: #9ca3af; font-size: 12px; margin-top: 40px; text-align: center; border-top: 1px solid #e5e7eb; padding-top: 20px; }
                    .icon { font-size: 48px; text-align: center; margin-bottom: 20px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="icon">🔐</div>
                    <h2>Réinitialisation de mot de passe</h2>
                    <div class="content">
                        <p>Bonjour <strong>%s</strong>,</p>
                        <p>Vous avez demandé la réinitialisation de votre mot de passe <strong>BioSync</strong>.</p>
                        <p>Voici votre code de vérification :</p>
                    </div>
                    <div class="code-box">
                        <div class="code">%s</div>
                        <div class="expiry">⏱ Ce code est valable 5 minutes</div>
                    </div>
                    <div class="content">
                        <p>Si vous n'avez pas fait cette demande, vous pouvez ignorer cet email en toute sécurité.</p>
                    </div>
                    <div class="footer">
                        © 2024 BioSync - Sécurité de vos données médicales
                    </div>
                </div>
            </body>
            </html>
            """.formatted(userName, code);
    }
}

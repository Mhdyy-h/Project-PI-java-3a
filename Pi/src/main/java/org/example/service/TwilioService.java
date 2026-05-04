package org.example.service;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Scanner;

public class TwilioService {

    private static final String ACCOUNT_SID = "ACcnoutsid";
    private static final String AUTH_TOKEN   = "token";
    private static final String FROM_NUMBER  = "+19343453998";
    private static final String TO_NUMBER    = "+21699179165";

    private static final String API_URL =
            "https://api.twilio.com/2010-04-01/Accounts/"
                    + ACCOUNT_SID + "/Messages.json";

    // ══════════════════════════════════════════════════
    //  MÉTHODE PRINCIPALE
    // ══════════════════════════════════════════════════

    public static boolean envoyerSMS(String message) {
        try {
            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            String credentials = ACCOUNT_SID + ":" + AUTH_TOKEN;
            String encoded = Base64.getEncoder()
                    .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Basic " + encoded);
            conn.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");
            conn.setDoOutput(true);
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(10000);

            String body = "From=" + encode(FROM_NUMBER)
                    + "&To="   + encode(TO_NUMBER)
                    + "&Body=" + encode(message);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }

            int status = conn.getResponseCode();

            if (status == 200 || status == 201) {
                System.out.println("[Twilio] SMS envoye avec succes.");
                return true;
            } else {
                StringBuilder erreur = new StringBuilder();
                try (Scanner sc = new Scanner(conn.getErrorStream(),
                        StandardCharsets.UTF_8)) {
                    while (sc.hasNextLine()) erreur.append(sc.nextLine());
                }
                System.err.println("[Twilio] Echec. Status: "
                        + status + " | " + erreur);
                return false;
            }

        } catch (Exception e) {
            System.err.println("[Twilio] Erreur connexion: " + e.getMessage());
            return false;
        }
    }

    // ══════════════════════════════════════════════════
    //  ALERTES
    // ══════════════════════════════════════════════════

    /** 🟡 Aliment excitant après 16h */
    public static void alerteAlimentExcitantApres16h(String nomPatient,
                                                     String nomAliment,
                                                     String heureRepas) {
        String msg = "[ALERTE JAUNE] NutriCoach\n"
                + "Patient : " + nomPatient + "\n"
                + "Aliment excitant detecte : " + nomAliment + "\n"
                + "Heure : " + heureRepas + "\n"
                + "Consommation d'excitant apres 16h.";
        envoyerSMSAsync(msg);
    }

    /** 🔴 Calories journalières > 1200 kcal */
    public static void alerteCaloriesElevees(String nomPatient,
                                             int caloriesTotal) {
        String msg = "[ALERTE ROUGE] NutriCoach\n"
                + "Patient : " + nomPatient + "\n"
                + "Calories journalieres : " + caloriesTotal + " kcal\n"
                + "Seuil depasse : > 1200 kcal.\n"
                + "Intervention recommandee.";
        envoyerSMSAsync(msg);
    }

    /** 🔴 Repas SOIR après 22h */
    public static void alerteRepasSoirApres22h(String nomPatient,
                                               String heureRepas) {
        String msg = "[ALERTE ROUGE] NutriCoach\n"
                + "Patient : " + nomPatient + "\n"
                + "Repas SOIR enregistre a : " + heureRepas + "\n"
                + "Repas apres 22h detecte.\n"
                + "Suivi nutritionnel requis.";
        envoyerSMSAsync(msg);
    }

    /** 🟡 Score nutritionnel < 4/14 */
    public static void alerteScoreFaible(String nomPatient, int score) {
        String msg = "[ALERTE JAUNE] NutriCoach\n"
                + "Patient : " + nomPatient + "\n"
                + "Score nutritionnel : " + score + "/14\n"
                + "Score inferieur au seuil minimum (4/14).\n"
                + "Reevaluation du programme conseillee.";
        envoyerSMSAsync(msg);
    }

    // ══════════════════════════════════════════════════
    //  ENVOI ASYNCHRONE
    // ══════════════════════════════════════════════════

    public static void envoyerSMSAsync(String message) {
        new Thread(() -> {
            boolean succes = envoyerSMS(message);
            System.out.println(succes
                    ? "[Twilio] Alerte envoyee au coach."
                    : "[Twilio] Echec envoi alerte.");
        }, "twilio-sms-thread").start();
    }

    // ══════════════════════════════════════════════════
    //  UTILITAIRE
    // ══════════════════════════════════════════════════

    private static String encode(String value) {
        try {
            return java.net.URLEncoder.encode(value, "UTF-8");
        } catch (Exception e) {
            return value;
        }
    }
}
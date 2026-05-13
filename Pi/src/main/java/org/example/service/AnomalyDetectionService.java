package org.example.service;

import java.util.ArrayList;
import java.util.List;

/**
 * Détection d'anomalies comportementales pendant un quiz.
 * Analyse : vitesse de réponse, patterns répétitifs, fatigue, décrochage.
 */
public class AnomalyDetectionService {

    // ── DTO d'entrée ────────────────────────────────────────────
    public static class QuestionRecord {
        public final int    index;
        public final long   responseTimeMs;
        public final boolean correct;
        public final String chosenOption; // "A"/"B"/"C"/"D" ou "" si timeout

        public QuestionRecord(int index, long responseTimeMs, boolean correct, String chosenOption) {
            this.index         = index;
            this.responseTimeMs = responseTimeMs;
            this.correct       = correct;
            this.chosenOption  = chosenOption != null ? chosenOption : "";
        }
    }

    // ── DTO de sortie ────────────────────────────────────────────
    public static class AnomalyReport {
        public final List<String> anomalies;
        public final double focusScore;         // 0–100
        public final String overallStatus;      // "OPTIMAL" / "ATTENTION" / "ALERTE"
        public final String statusEmoji;        // 🟢 / 🟡 / 🔴
        public final String recommendation;
        public final double avgResponseTimeSec;
        public final int    tooFastCount;
        public final int    tooSlowCount;
        public final int    timeoutCount;

        public AnomalyReport(List<String> anomalies, double focusScore,
                             String overallStatus, String statusEmoji,
                             String recommendation, double avgResponseTimeSec,
                             int tooFastCount, int tooSlowCount, int timeoutCount) {
            this.anomalies          = anomalies;
            this.focusScore         = focusScore;
            this.overallStatus      = overallStatus;
            this.statusEmoji        = statusEmoji;
            this.recommendation     = recommendation;
            this.avgResponseTimeSec = avgResponseTimeSec;
            this.tooFastCount       = tooFastCount;
            this.tooSlowCount       = tooSlowCount;
            this.timeoutCount       = timeoutCount;
        }
    }

    // ── Seuils ──────────────────────────────────────────────────
    private static final long TOO_FAST_MS  = 1_500;   // < 1.5 s  → clic sans lire
    private static final long TOO_SLOW_MS  = 25_000;  // > 25 s   → distraction
    private static final int  SAME_STREAK  = 4;       // ≥ 4 même lettre → pattern
    private static final double FATIGUE_RATIO = 1.6;  // 2e moitié ×1.6 plus lente

    // ─────────────────────────────────────────────────────────────
    public AnomalyReport analyze(List<QuestionRecord> records) {

        if (records == null || records.isEmpty()) {
            return ok(0, 0, 0, 0.0);
        }

        List<String> anomalies = new ArrayList<>();
        int tooFast = 0, tooSlow = 0, timeouts = 0;
        long totalMs = 0;

        for (QuestionRecord r : records) {
            totalMs += r.responseTimeMs;
            if (r.chosenOption.isEmpty())       timeouts++;
            else if (r.responseTimeMs < TOO_FAST_MS) tooFast++;
            else if (r.responseTimeMs > TOO_SLOW_MS) tooSlow++;
        }

        double avgSec = totalMs / 1000.0 / records.size();

        // ── Anomalie 1 : clics trop rapides ─────────────────────
        if (tooFast >= 3) {
            anomalies.add("⚡ " + tooFast + " réponses < 1.5 s — possible clic aléatoire");
        } else if (tooFast >= 2) {
            anomalies.add("⚡ " + tooFast + " réponses très rapides détectées");
        }

        // ── Anomalie 2 : réponses trop lentes ───────────────────
        if (tooSlow >= 3) {
            anomalies.add("⏳ " + tooSlow + " réponses > 25 s — distraction probable");
        }

        // ── Anomalie 3 : timeouts multiples ─────────────────────
        if (timeouts >= 2) {
            anomalies.add("⌛ " + timeouts + " questions expirées — manque d'engagement");
        }

        // ── Anomalie 4 : même option répétée ────────────────────
        if (records.size() >= 4) {
            int streak = 1, maxStreak = 1;
            for (int i = 1; i < records.size(); i++) {
                String prev = records.get(i - 1).chosenOption;
                String curr = records.get(i).chosenOption;
                if (!prev.isEmpty() && prev.equals(curr)) {
                    streak++;
                    maxStreak = Math.max(maxStreak, streak);
                } else {
                    streak = 1;
                }
            }
            if (maxStreak >= SAME_STREAK) {
                anomalies.add("🔁 Option «" + records.get(records.size()-1).chosenOption
                        + "» répétée " + maxStreak + " fois — pattern suspect");
            }
        }

        // ── Anomalie 5 : fatigue (2e moitié + lente) ────────────
        if (records.size() >= 6) {
            int mid = records.size() / 2;
            double first  = records.subList(0, mid).stream()
                    .mapToLong(r -> r.responseTimeMs).average().orElse(0);
            double second = records.subList(mid, records.size()).stream()
                    .mapToLong(r -> r.responseTimeMs).average().orElse(0);
            if (second > first * FATIGUE_RATIO) {
                anomalies.add("😴 Fatigue : temps ×"
                        + String.format("%.1f", second / Math.max(first, 1))
                        + " en 2ᵉ moitié");
            }
        }

        // ── Anomalie 6 : décrochage final ───────────────────────
        if (records.size() >= 4) {
            int wrongStreak = 0;
            for (int i = records.size() - 1; i >= 0; i--) {
                if (!records.get(i).correct) wrongStreak++;
                else break;
            }
            if (wrongStreak >= 3) {
                anomalies.add("📉 " + wrongStreak + " erreurs consécutives en fin — décrochage");
            }
        }

        // ── Score focus (0–100) ──────────────────────────────────
        double deductions = tooFast * 8.0 + tooSlow * 5.0 + timeouts * 10.0 + anomalies.size() * 4.0;
        double focus = Math.max(0, Math.min(100, 100 - deductions));

        // ── Statut global ────────────────────────────────────────
        String status, emoji, reco;
        if (anomalies.isEmpty() && focus >= 80) {
            status = "OPTIMAL";  emoji = "🟢";
            reco   = "Excellente concentration tout au long du quiz.";
        } else if (anomalies.size() <= 1 && focus >= 60) {
            status = "ATTENTION"; emoji = "🟡";
            reco   = "Attention légère détectée. Essayez dans un environnement calme.";
        } else {
            status = "ALERTE";   emoji = "🔴";
            reco   = "Plusieurs anomalies détectées. Faites une pause avant de réessayer.";
        }

        return new AnomalyReport(anomalies, focus, status, emoji, reco,
                avgSec, tooFast, tooSlow, timeouts);
    }

    /** Rapport vide (aucune question ou avant toute réponse). */
    private static AnomalyReport ok(int f, int s, int t, double avg) {
        return new AnomalyReport(new ArrayList<>(), 100.0,
                "OPTIMAL", "🟢", "Bonne concentration.", avg, f, s, t);
    }
}

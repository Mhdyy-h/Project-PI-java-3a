package org.example.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MentalHealthTipsService {

    private static final Map<String, List<String>> TIPS = Map.of(
        "stress", List.of(
            "💨 Respirez : 4 sec inspiration – 7 sec rétention – 8 sec expiration. Répétez 3 fois.",
            "🌿 10 minutes de marche en plein air réduisent le cortisol de 15 %. Sortez maintenant !",
            "📝 Écrivez 3 choses positives d'aujourd'hui. La gratitude recalibre le système nerveux.",
            "🧘 La cohérence cardiaque (5 min matin) stabilise la fréquence cardiaque et l'humeur.",
            "🎵 La musique à 60 bpm synchronise les ondes cérébrales alpha et favorise la détente."
        ),
        "performance", List.of(
            "😴 Un athlète qui dort 9h améliore sa vitesse de réaction de 20 % et son endurance de 30 %.",
            "💧 Déshydratation de 2 % = baisse de 10 % des performances cognitives. Buvez !",
            "👁️ Visualisez votre prochaine séance en détail — les athlètes olympiques le font chaque jour.",
            "🔄 La récupération active (jogging léger) élimine l'acide lactique 2× plus vite que le repos.",
            "🎯 Fixez un objectif SMART avant chaque séance : précis, mesurable, réaliste et daté."
        ),
        "nutrition", List.of(
            "🥩 Mangez 30 g de protéines dans les 30 min post-entraînement pour maximiser la synthèse musculaire.",
            "🧠 Oméga-3 = carburant cérébral. Saumon, noix et graines de chia 3×/semaine minimum.",
            "🍚 Les glucides complexes (riz, avoine) alimentent votre cerveau sans pic glycémique.",
            "🕐 Respectez une fenêtre d'alimentation de 8–10 h pour optimiser le métabolisme.",
            "🥦 5 portions de légumes/fruits par jour réduisent le risque de dépression de 11 %."
        ),
        "sommeil", List.of(
            "📵 Évitez les écrans 1 h avant le coucher — la lumière bleue retarde la mélatonine de 2 h.",
            "🌡️ 16–19 °C dans la chambre = température idéale pour un sommeil profond réparateur.",
            "⏰ Une sieste de 20 min améliore la vigilance de 34 % et les performances de 16 %.",
            "🌙 Un rituel coucher constant (même heure) renforce l'horloge circadienne en 2 semaines.",
            "🏋️ L'hormone de croissance (GH) est sécrétée à 80 % pendant le sommeil profond. Dormez !"
        ),
        "motivation", List.of(
            "💡 \"Ce n'est pas la montagne qui nous épuise, c'est le caillou dans notre chaussure.\" – W. Churchill",
            "🔥 La cohérence bat l'intensité. 20 min/jour > 2 h une fois par semaine.",
            "🧗 Sortir de sa zone de confort de 10 % suffit pour progresser sans risque de burn-out.",
            "🏅 Chaque effort enregistré renforce la neuroplasticité. Votre cerveau se recâble à chaque session.",
            "🌱 \"Le talent est une plante qui a besoin d'eau chaque jour.\" Entraînez-vous."
        )
    );

    private static final List<String> ALL_TIPS = buildAllTips();

    private static List<String> buildAllTips() {
        List<String> all = new ArrayList<>();
        TIPS.values().forEach(all::addAll);
        return all;
    }

    public static String getTipOfDay() {
        int dayIndex = LocalDate.now().getDayOfYear() % ALL_TIPS.size();
        return ALL_TIPS.get(dayIndex);
    }

    public static String getTipForStressLevel(int niveau) {
        List<String> pool = getPoolForLevel(niveau);
        int dayIndex = LocalDate.now().getDayOfYear() % pool.size();
        return pool.get(dayIndex);
    }

    public static String getEmojiForLevel(int niveau) {
        if (niveau <= 1) return "🌿";
        if (niveau <= 3) return "💪";
        if (niveau <= 5) return "🧠";
        if (niveau <= 7) return "🔥";
        return "⚡";
    }

    public static String getLabelForLevel(int niveau) {
        if (niveau <= 1) return "Faible";
        if (niveau <= 3) return "Léger";
        if (niveau <= 5) return "Modéré";
        if (niveau <= 7) return "Élevé";
        return "Sévère";
    }

    public static String getColorForLevel(int niveau) {
        if (niveau <= 1) return "#34D399";   // vert clair BioSync
        if (niveau <= 3) return "#1D9E75";   // vert foncé BioSync
        if (niveau <= 5) return "#7C6FCD";   // violet BioSync
        if (niveau <= 7) return "#6B5EBC";   // violet foncé BioSync
        return "#5B21B6";                    // violet profond BioSync
    }

    public static String getLightBgForLevel(int niveau) {
        if (niveau <= 1) return "#D1FAE5";   // fond vert clair
        if (niveau <= 3) return "#A7F3D0";   // fond vert moyen
        if (niveau <= 5) return "#EDE9FE";   // fond violet clair
        if (niveau <= 7) return "#DDD6FE";   // fond violet moyen
        return "#C4B5FD";                    // fond violet soutenu
    }

    public static String getEmojiForMedal(String medaille) {
        if (medaille == null) return "";
        return switch (medaille) {
            case "Or" -> "🥇";
            case "Argent" -> "🥈";
            case "Bronze" -> "🥉";
            default -> "🏅";
        };
    }

    public static String getBgColorForMedal(String medaille) {
        if (medaille == null) return "#F3F4F6";
        return switch (medaille) {
            case "Or" -> "#FEF3C7";
            case "Argent" -> "#F1F5F9";
            case "Bronze" -> "#FDF2E9";
            default -> "#F3F4F6";
        };
    }

    public static String getTextColorForMedal(String medaille) {
        if (medaille == null) return "#6B7280";
        return switch (medaille) {
            case "Or" -> "#92400E";
            case "Argent" -> "#475569";
            case "Bronze" -> "#78350F";
            default -> "#6B7280";
        };
    }

    private static List<String> getPoolForLevel(int niveau) {
        if (niveau <= 1) return TIPS.get("sommeil");
        if (niveau <= 3) return TIPS.get("nutrition");
        if (niveau <= 5) return TIPS.get("performance");
        if (niveau <= 7) return TIPS.get("stress");
        return TIPS.get("motivation");
    }
}

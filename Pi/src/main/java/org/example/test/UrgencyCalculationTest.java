package org.example.test;

/**
 * Test class to demonstrate urgency calculation based on motif
 */
public class UrgencyCalculationTest {
    
    public static void main(String[] args) {
        System.out.println("🧪 Testing Urgency Calculation Logic...\n");
        
        // Test cases
        String[] testMotifs = {
            "Consultation de contrôle routine",
            "Douleur forte à la poitrine",
            "Accident de voiture avec saignement",
            "Fièvre et vomissements depuis hier",
            "Fatigue chronique et perte de poids",
            "Renouvellement d'ordonnance",
            "Difficulté à respirer",
            "Blessure légère au bras",
            "Infection cutanée avec gonflement"
        };
        
        for (String motif : testMotifs) {
            int urgency = calculateUrgencyLevel(motif);
            String urgencyText = getUrgencyText(urgency);
            System.out.println("Motif: \"" + motif + "\"");
            System.out.println("→ Urgency: " + urgencyText + " (Niveau " + urgency + ")");
            System.out.println();
        }
    }
    
    private static int calculateUrgencyLevel(String motif) {
        if (motif == null || motif.trim().isEmpty()) {
            return 1;
        }
        
        String lowerMotif = motif.toLowerCase();
        
        // High urgency keywords
        if (containsAny(lowerMotif, "urgence", "urgent", "douleur forte", "accident", "saignement", "difficulté respirer", "poitrine", "malaise")) {
            return 5;
        }
        // Medium-high urgency keywords
        else if (containsAny(lowerMotif, "fièvre", "vomissement", "diarrhée", "infection", "blessure", "chute", "brûlure")) {
            return 4;
        }
        // Medium urgency keywords
        else if (containsAny(lowerMotif, "mal", "douleur", "fatigue", "perte", "gonflement", "rougeur")) {
            return 3;
        }
        // Low-medium urgency keywords
        else if (containsAny(lowerMotif, "contrôle", "suivi", "vaccin", "ordonnance", "renouvellement")) {
            return 2;
        }
        
        return 1; // Default: Faible
    }
    
    private static String getUrgencyText(int level) {
        switch (level) {
            case 5: return "Urgence";
            case 4: return "Élevée";
            case 3: return "Moyenne";
            case 2: return "Faible";
            case 1: return "Faible";
            default: return "Inconnue";
        }
    }
    
    private static boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}

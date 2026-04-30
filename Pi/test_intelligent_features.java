// Test des fonctionnalités intelligentes
import org.example.service.IntelligentScheduler;
import org.example.service.AbsencePredictor;
import org.example.model.User;
import org.example.model.RendezVous;
import org.example.model.Specialiste;

import java.time.LocalDateTime;
import java.util.List;

public class test_intelligent_features {
    public static void main(String[] args) {
        System.out.println("=== TEST DES FONCTIONNALITÉS INTELLIGENTES ===");
        
        // Test 1: Suggestion intelligente de créneaux
        System.out.println("\n🧠 1. TEST - Suggestion intelligente de créneaux");
        testIntelligentScheduling();
        
        // Test 2: Prédiction d'absences
        System.out.println("\n🔮 2. TEST - Prédiction d'absences");
        testAbsencePrediction();
        
        // Test 3: Analytics des absences
        System.out.println("\n📊 3. TEST - Analytics des absences");
        testAbsenceAnalytics();
        
        System.out.println("\n=== TEST COMPLETÉ ===");
    }
    
    private static void testIntelligentScheduling() {
        try {
            // Créer un patient fictif
            User patient = new User();
            patient.setId(1);
            patient.setNomComplet("Test Patient");
            
            // Test différents motifs avec différents niveaux d'urgence
            String[] motifs = {
                "douleur thoracique urgente",
                "contrôle annuel routine", 
                "suivi diabète",
                "première fois consultation"
            };
            
            String[] specialites = {"Cardiologue", "Généraliste", "Endocrinologue", "Généraliste"};
            
            for (int i = 0; i < motifs.length; i++) {
                System.out.println("\n📋 Motif: \"" + motifs[i] + "\"");
                System.out.println("🏥 Spécialité: " + specialites[i]);
                
                List<IntelligentScheduler.TimeSlot> suggestions = 
                    IntelligentScheduler.suggestOptimalSlots(patient, motifs[i], specialites[i]);
                
                if (suggestions.isEmpty()) {
                    System.out.println("   ❌ Aucune suggestion trouvée");
                } else {
                    System.out.println("   ✅ " + suggestions.size() + " suggestions trouvées:");
                    for (int j = 0; j < Math.min(3, suggestions.size()); j++) {
                        IntelligentScheduler.TimeSlot slot = suggestions.get(j);
                        System.out.println("      " + (j+1) + ". " + slot.getDisplayText());
                    }
                }
            }
            
        } catch (Exception e) {
            System.out.println("❌ Erreur dans testIntelligentScheduling: " + e.getMessage());
        }
    }
    
    private static void testAbsencePrediction() {
        try {
            // Créer un patient fictif
            User patient = new User();
            patient.setId(1);
            patient.setNomComplet("Test Patient");
            
            // Créer un rendez-vous fictif
            RendezVous rdv = new RendezVous();
            rdv.setId(1);
            rdv.setPatientId(1);
            rdv.setSpecialisteId(1);
            rdv.setDateHeure(LocalDateTime.now().plusDays(2));
            rdv.setMotif("contrôle routine");
            rdv.setMode("présentiel");
            rdv.setStatut("en attente");
            
            System.out.println("📋 Patient: " + patient.getNomComplet());
            System.out.println("📅 RDV: " + rdv.getDateHeure());
            System.out.println("💬 Motif: " + rdv.getMotif());
            
            // Prédire le risque d'absence
            double riskScore = AbsencePredictor.predictAbsence(patient, rdv);
            System.out.println("📊 Score de risque: " + String.format("%.1f%%", riskScore * 100));
            
            // Générer un rappel intelligent
            AbsencePredictor.Reminder reminder = AbsencePredictor.generateIntelligentReminder(patient, rdv);
            System.out.println("📱 Type de rappel: " + reminder.getType());
            System.out.println("⏰ Délai: " + reminder.getAdvanceNotice().toHours() + " heures avant");
            System.out.println("📨 SMS requis: " + (reminder.isRequiresSms() ? "Oui" : "Non"));
            System.out.println("💬 Message: " + reminder.getMessage());
            
        } catch (Exception e) {
            System.out.println("❌ Erreur dans testAbsencePrediction: " + e.getMessage());
        }
    }
    
    private static void testAbsenceAnalytics() {
        try {
            // Analyser les tendances d'absences pour le dernier mois
            LocalDateTime endDate = LocalDateTime.now();
            LocalDateTime startDate = endDate.minusMonths(1);
            
            System.out.println("📅 Période d'analyse: " + startDate.toLocalDate() + " au " + endDate.toLocalDate());
            
            AbsencePredictor.AbsenceAnalytics analytics = 
                AbsencePredictor.analyzeAbsenceTrends(startDate.toLocalDate(), endDate.toLocalDate());
            
            System.out.println("📊 " + analytics.getSummary());
            
            System.out.println("\n📈 Taux d'annulation par jour:");
            analytics.getCancellationRateByDay().forEach((day, rate) -> {
                System.out.println(String.format("   %s: %.1f%%", 
                    day.toString().substring(0, 1).toUpperCase() + day.toString().substring(1, 3),
                    rate * 100));
            });
            
        } catch (Exception e) {
            System.out.println("❌ Erreur dans testAbsenceAnalytics: " + e.getMessage());
        }
    }
}

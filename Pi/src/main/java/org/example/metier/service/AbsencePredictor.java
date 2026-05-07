package org.example.metier.service;

import org.example.dao.RendezVousDAO;
import org.example.model.RendezVous;
import org.example.model.User;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Système d'analyse et prédiction des absences
 * Prédit si un patient va annuler et envoie des rappels intelligents
 */
public class AbsencePredictor {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    /**
     * Prédit la probabilité qu'un patient annule son rendez-vous
     * @param patient Le patient concerné
     * @param rendezVous Le rendez-vous à prédire
     * @return Score de prédiction (0.0 = très faible risque, 1.0 = très haut risque)
     */
    public static double predictAbsence(User patient, RendezVous rendezVous) {
        double score = 0;
        
        // 1. Historique d'annulations (40% du poids)
        double historiqueScore = calculateHistoriqueScore(patient);
        score += historiqueScore * 0.4;
        
        // 2. Délai depuis le dernier RDV (30% du poids)
        double delaiScore = calculateDelaiScore(patient, rendezVous);
        score += delaiScore * 0.3;
        
        // 3. Type de consultation (20% du poids)
        double typeScore = calculateTypeScore(rendezVous);
        score += typeScore * 0.2;
        
        // 4. Conditions externes (10% du poids)
        double conditionsScore = calculateConditionsScore(rendezVous);
        score += conditionsScore * 0.1;
        
        // Normaliser le score entre 0 et 1
        return Math.min(1.0, Math.max(0.0, score));
    }
    
    /**
     * Calcule le score basé sur l'historique d'annulations du patient
     */
    private static double calculateHistoriqueScore(User patient) {
        List<RendezVous> patientHistory = RendezVousDAO.getRendezVousByPatient(patient.getId());
        
        if (patientHistory.isEmpty()) {
            return 0.3; // Nouveau patient = risque moyen
        }
        
        long totalRdvs = patientHistory.size();
        long cancelledRdvs = patientHistory.stream()
            .filter(r -> "annulé".equalsIgnoreCase(r.getStatut()))
            .count();
        
        double tauxAnnulation = (double) cancelledRdvs / totalRdvs;
        
        // Ajuster le score selon le taux
        if (tauxAnnulation >= 0.3) {
            return 0.8; // Taux élevé d'annulation
        } else if (tauxAnnulation >= 0.15) {
            return 0.5; // Taux moyen d'annulation
        } else if (tauxAnnulation >= 0.05) {
            return 0.2; // Faible taux d'annulation
        } else {
            return 0.1; // Très fiable
        }
    }
    
    /**
     * Calcule le score basé sur le délai depuis le dernier rendez-vous
     */
    private static double calculateDelaiScore(User patient, RendezVous upcomingRdv) {
        List<RendezVous> patientHistory = RendezVousDAO.getRendezVousByPatient(patient.getId());
        
        // Filtrer les RDV passés et réalisés
        List<RendezVous> pastRdvs = patientHistory.stream()
            .filter(r -> r.getDateHeure().isBefore(LocalDateTime.now()) && 
                        "réalisé".equalsIgnoreCase(r.getStatut()))
            .sorted(Comparator.comparing(RendezVous::getDateHeure).reversed())
            .collect(Collectors.toList());
        
        if (pastRdvs.isEmpty()) {
            return 0.4; // Premier RDV = risque légèrement élevé
        }
        
        RendezVous lastRdv = pastRdvs.get(0);
        long daysSinceLastRdv = ChronoUnit.DAYS.between(lastRdv.getDateHeure(), LocalDateTime.now());
        
        if (daysSinceLastRdv <= 30) {
            return 0.1; // RDV récent = très fiable
        } else if (daysSinceLastRdv <= 90) {
            return 0.2; // RDV assez récent = fiable
        } else if (daysSinceLastRdv <= 180) {
            return 0.4; // RDV distant = risque moyen
        } else {
            return 0.6; // Très distant = risque élevé
        }
    }
    
    /**
     * Calcule le score basé sur le type de consultation
     */
    private static double calculateTypeScore(RendezVous rendezVous) {
        String motif = rendezVous.getMotif().toLowerCase();
        String mode = rendezVous.getMode().toLowerCase();
        
        // Première fois = plus de risque d'annulation
        if (containsAny(motif, "première fois", "nouveau patient", "consultation initiale")) {
            return 0.6;
        }
        
        // Suivi/contrôle = moins de risque
        if (containsAny(motif, "suivi", "contrôle", "renouvellement", "avis")) {
            return 0.2;
        }
        
        // Téléconsultation = légèrement plus de risque
        if (mode.contains("télé") || mode.contains("distance")) {
            return 0.3;
        }
        
        // Urgence = très peu de risque d'annulation
        if (containsAny(motif, "urgent", "douleur", "urgence")) {
            return 0.1;
        }
        
        return 0.3; // Standard
    }
    
    /**
     * Calcule le score basé sur les conditions externes
     */
    private static double calculateConditionsScore(RendezVous rendezVous) {
        double score = 0;
        LocalDateTime rdvDateTime = rendezVous.getDateHeure();
        DayOfWeek dayOfWeek = rdvDateTime.getDayOfWeek();
        int hour = rdvDateTime.getHour();
        
        // Jour de la semaine
        if (dayOfWeek == DayOfWeek.MONDAY) {
            score += 0.2; // Lundi = plus d'annulations
        } else if (dayOfWeek == DayOfWeek.FRIDAY) {
            score += 0.1; // Vendredi = légèrement plus d'annulations
        }
        
        // Heure de la journée
        if (hour <= 10 || hour >= 16) {
            score += 0.1; // Début/fin de journée = plus d'annulations
        }
        
        // Saison (simplifié)
        Month month = rdvDateTime.getMonth();
        if (month == Month.DECEMBER || month == Month.JANUARY) {
            score += 0.1; // Période des fêtes = plus d'annulations
        }
        
        return Math.min(0.3, score);
    }
    
    /**
     * Génère un rappel intelligent selon le profil du patient
     */
    public static Reminder generateIntelligentReminder(User patient, RendezVous rendezVous) {
        double riskScore = predictAbsence(patient, rendezVous);
        
        if (riskScore >= 0.7) {
            // Haut risque - Rappel multiple
            return new Reminder(
                ReminderType.HIGH_RISK,
                "RDV IMPORTANT: " + rendezVous.getDateHeure().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + 
                " avec Dr. " + getSpecialisteName(rendezVous.getSpecialisteId()) + 
                ". Veuillez confirmer votre présence: [OUI]/[NON]",
                Duration.ofHours(48), // 48h avant
                true // SMS requis
            );
        } else if (riskScore >= 0.4) {
            // Risque moyen - Rappel standard
            return new Reminder(
                ReminderType.MEDIUM_RISK,
                "Rappel RDV: " + rendezVous.getDateHeure().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + 
                " avec Dr. " + getSpecialisteName(rendezVous.getSpecialisteId()),
                Duration.ofHours(24), // 24h avant
                false
            );
        } else {
            // Faible risque - Rappel simple
            return new Reminder(
                ReminderType.LOW_RISK,
                "RDV: " + rendezVous.getDateHeure().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + 
                " - Dr. " + getSpecialisteName(rendezVous.getSpecialisteId()),
                Duration.ofHours(24), // 24h avant
                false
            );
        }
    }
    
    /**
     * Obtient le nom du spécialiste (simplifié)
     */
    private static String getSpecialisteName(Integer specialisteId) {
        // Pour l'instant, retourner une valeur par défaut
        // En pratique, il faudrait appeler SpecialisteDAO.getSpecialisteById()
        return "Spécialiste";
    }
    
    /**
     * Analyse les tendances d'absences pour une période
     */
    public static AbsenceAnalytics analyzeAbsenceTrends(LocalDate startDate, LocalDate endDate) {
        List<RendezVous> allRdvs = RendezVousDAO.getRendezVousByDateRange(startDate, endDate);
        
        long totalRdvs = allRdvs.size();
        long cancelledRdvs = allRdvs.stream()
            .filter(r -> "annulé".equalsIgnoreCase(r.getStatut()))
            .count();
        
        double tauxAnnulationGlobal = totalRdvs > 0 ? (double) cancelledRdvs / totalRdvs : 0;
        
        // Analyse par jour de la semaine
        Map<DayOfWeek, Double> tauxParJour = new HashMap<>();
        for (DayOfWeek day : DayOfWeek.values()) {
            long rdvsJour = allRdvs.stream()
                .filter(r -> r.getDateHeure().getDayOfWeek() == day)
                .count();
            long cancelledJour = allRdvs.stream()
                .filter(r -> r.getDateHeure().getDayOfWeek() == day && 
                           "annulé".equalsIgnoreCase(r.getStatut()))
                .count();
            
            tauxParJour.put(day, rdvsJour > 0 ? (double) cancelledJour / rdvsJour : 0);
        }
        
        return new AbsenceAnalytics(tauxAnnulationGlobal, tauxParJour, totalRdvs, cancelledRdvs);
    }
    
    /**
     * Vérifie si une chaîne contient un des mots-clés
     */
    private static boolean containsAny(String text, String... keywords) {
        return Arrays.stream(keywords).anyMatch(text::contains);
    }
    
    /**
     * Types de rappels
     */
    public enum ReminderType {
        LOW_RISK,      // Faible risque - rappel simple
        MEDIUM_RISK,   // Risque moyen - rappel standard
        HIGH_RISK      // Haut risque - rappel multiple
    }
    
    /**
     * Classe pour représenter un rappel
     */
    public static class Reminder {
        private final ReminderType type;
        private final String message;
        private final Duration advanceNotice;
        private final boolean requiresSms;
        
        public Reminder(ReminderType type, String message, Duration advanceNotice, boolean requiresSms) {
            this.type = type;
            this.message = message;
            this.advanceNotice = advanceNotice;
            this.requiresSms = requiresSms;
        }
        
        // Getters
        public ReminderType getType() { return type; }
        public String getMessage() { return message; }
        public Duration getAdvanceNotice() { return advanceNotice; }
        public boolean isRequiresSms() { return requiresSms; }
    }
    
    /**
     * Classe pour les analytics d'absences
     */
    public static class AbsenceAnalytics {
        private final double globalCancellationRate;
        private final Map<DayOfWeek, Double> cancellationRateByDay;
        private final long totalRendezVous;
        private final long cancelledRendezVous;
        
        public AbsenceAnalytics(double globalCancellationRate, 
                              Map<DayOfWeek, Double> cancellationRateByDay,
                              long totalRendezVous, 
                              long cancelledRendezVous) {
            this.globalCancellationRate = globalCancellationRate;
            this.cancellationRateByDay = cancellationRateByDay;
            this.totalRendezVous = totalRendezVous;
            this.cancelledRendezVous = cancelledRendezVous;
        }
        
        public String getSummary() {
            return String.format(
                "Taux d'annulation global: %.1f%% (%d/%d RDV). " +
                "Jour le plus risqué: %s (%.1f%%)",
                globalCancellationRate * 100,
                cancelledRendezVous,
                totalRendezVous,
                getRiskiestDay(),
                getHighestDailyRate() * 100
            );
        }
        
        private DayOfWeek getRiskiestDay() {
            return cancellationRateByDay.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(DayOfWeek.MONDAY);
        }
        
        private double getHighestDailyRate() {
            return cancellationRateByDay.values().stream()
                .mapToDouble(Double::doubleValue)
                .max()
                .orElse(0);
        }
        
        // Getters
        public double getGlobalCancellationRate() { return globalCancellationRate; }
        public Map<DayOfWeek, Double> getCancellationRateByDay() { return cancellationRateByDay; }
        public long getTotalRendezVous() { return totalRendezVous; }
        public long getCancelledRendezVous() { return cancelledRendezVous; }
    }
}

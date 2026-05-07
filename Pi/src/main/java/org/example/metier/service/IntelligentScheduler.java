package org.example.metier.service;

import org.example.dao.RendezVousDAO;
import org.example.dao.SpecialisteDAO;
import org.example.model.RendezVous;
import org.example.model.Specialiste;
import org.example.model.User;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Système intelligent de gestion des RDV
 * Suggestion automatique de créneaux, évitement des conflits, priorité urgences
 */
public class IntelligentScheduler {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    
    // Heures de travail standard
    private static final int WORK_START_HOUR = 8;
    private static final int WORK_END_HOUR = 18;
    private static final int SLOT_DURATION_MINUTES = 30;
    
    /**
     * Suggère les meilleurs créneaux disponibles pour un patient
     */
    public static List<TimeSlot> suggestOptimalSlots(User patient, String motif, String specialite) {
        List<TimeSlot> suggestions = new ArrayList<>();
        
        // 1. Analyser l'urgence du motif
        UrgencyLevel urgency = analyzeUrgency(motif);
        
        // 2. Obtenir les spécialistes disponibles
        List<Specialiste> availableSpecialists = SpecialisteDAO.getSpecialistesBySpecialite(specialite);
        
        // 3. Générer les créneaux optimisés
        for (Specialiste specialist : availableSpecialists) {
            suggestions.addAll(generateTimeSlots(specialist, urgency, patient));
        }
        
        // 4. Trier par score de pertinence
        suggestions.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
        
        // 5. Retourner les 5 meilleurs suggestions
        return suggestions.stream().limit(5).collect(Collectors.toList());
    }
    
    /**
     * Analyse l'urgence du motif
     */
    private static UrgencyLevel analyzeUrgency(String motif) {
        String lowerMotif = motif.toLowerCase();
        
        // Mots-clés urgence haute
        if (containsAny(lowerMotif, "urgent", "douleur aigue", "urgence", "accident", "crise", "hemorragie", "malaise")) {
            return UrgencyLevel.HIGH;
        }
        
        // Mots-clés urgence moyenne
        if (containsAny(lowerMotif, "douleur", "fièvre", "infection", "inflammation", "blessure")) {
            return UrgencyLevel.MEDIUM;
        }
        
        // Mots-clés urgence basse
        if (containsAny(lowerMotif, "contrôle", "suivi", "renouvellement", "ordonnance", "avis")) {
            return UrgencyLevel.LOW;
        }
        
        return UrgencyLevel.NORMAL;
    }
    
    /**
     * Génère les créneaux disponibles pour un spécialiste
     */
    private static List<TimeSlot> generateTimeSlots(Specialiste specialist, UrgencyLevel urgency, User patient) {
        List<TimeSlot> slots = new ArrayList<>();
        LocalDate startDate = calculateStartDate(urgency);
        LocalDate endDate = calculateEndDate(urgency);
        
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            if (isWorkingDay(date)) {
                slots.addAll(generateDailySlots(specialist, date, urgency, patient));
            }
        }
        
        return slots;
    }
    
    /**
     * Génère les créneaux pour une journée spécifique
     */
    private static List<TimeSlot> generateDailySlots(Specialiste specialist, LocalDate date, UrgencyLevel urgency, User patient) {
        List<TimeSlot> dailySlots = new ArrayList<>();
        
        for (int hour = WORK_START_HOUR; hour < WORK_END_HOUR; hour++) {
            for (int minute = 0; minute < 60; minute += SLOT_DURATION_MINUTES) {
                LocalDateTime slotDateTime = LocalDateTime.of(date, LocalTime.of(hour, minute));
                
                if (isSlotAvailable(specialist, slotDateTime)) {
                    TimeSlot slot = new TimeSlot(
                        specialist,
                        slotDateTime,
                        calculateSlotScore(specialist, slotDateTime, urgency, patient)
                    );
                    dailySlots.add(slot);
                }
            }
        }
        
        return dailySlots;
    }
    
    /**
     * Calcule le score de pertinence d'un créneau
     */
    private static double calculateSlotScore(Specialiste specialist, LocalDateTime slotDateTime, UrgencyLevel urgency, User patient) {
        double score = 0;
        
        // Score base selon l'urgence
        switch (urgency) {
            case HIGH:
                // Pour urgence haute: privilégier les créneaux proches
                long hoursUntilSlot = ChronoUnit.HOURS.between(LocalDateTime.now(), slotDateTime);
                score += Math.max(0, 100 - hoursUntilSlot);
                break;
            case MEDIUM:
                // Pour urgence moyenne: créneaux dans les 3 jours
                long daysUntilSlot = ChronoUnit.DAYS.between(LocalDate.now(), slotDateTime.toLocalDate());
                if (daysUntilSlot <= 3) {
                    score += 80 - (daysUntilSlot * 10);
                } else {
                    score += 30;
                }
                break;
            case LOW:
                // Pour urgence basse: pas de pénalité pour créneaux lointains
                score += 50;
                break;
            default:
                score += 60;
        }
        
        // Bonus selon l'heure (matin = légèrement meilleur)
        int hour = slotDateTime.getHour();
        if (hour >= 9 && hour <= 11) {
            score += 10; // Heures de pointe matinales
        } else if (hour >= 14 && hour <= 16) {
            score += 5; // Heures d'après-midi
        }
        
        // Bonus selon le jour (éviter lundi matin et vendredi après-midi)
        DayOfWeek dayOfWeek = slotDateTime.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.MONDAY && hour <= 10) {
            score -= 5; // Lundi matin = moins idéal
        } else if (dayOfWeek == DayOfWeek.FRIDAY && hour >= 15) {
            score -= 5; // Vendredi après-midi = moins idéal
        }
        
        // Bonus selon la spécialité du spécialiste
        if (specialist.getSpecialite().toLowerCase().contains("cardio") && urgency == UrgencyLevel.HIGH) {
            score += 15; // Bonus spécialité pour urgence cardio
        }
        
        return score;
    }
    
    /**
     * Vérifie si un créneau est disponible
     */
    private static boolean isSlotAvailable(Specialiste specialist, LocalDateTime slotDateTime) {
        // Vérifier s'il n'y a pas déjà de RDV à ce créneau
        List<RendezVous> existingRdvs = RendezVousDAO.getRendezVousBySpecialisteAndDate(
            specialist.getId(), 
            slotDateTime.toLocalDate()
        );
        
        for (RendezVous rdv : existingRdvs) {
            LocalDateTime rdvTime = rdv.getDateHeure();
            // Vérifier si le créneau chevauche (avec buffer de 15 min)
            if (Math.abs(ChronoUnit.MINUTES.between(rdvTime, slotDateTime)) < SLOT_DURATION_MINUTES) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Calcule la date de début selon l'urgence
     */
    private static LocalDate calculateStartDate(UrgencyLevel urgency) {
        switch (urgency) {
            case HIGH:
                return LocalDate.now(); // Aujourd'hui
            case MEDIUM:
                return LocalDate.now().plusDays(1); // Demain
            default:
                return LocalDate.now().plusDays(2); // Après-demain
        }
    }
    
    /**
     * Calcule la date de fin selon l'urgence
     */
    private static LocalDate calculateEndDate(UrgencyLevel urgency) {
        switch (urgency) {
            case HIGH:
                return LocalDate.now().plusDays(2); // 2 jours max
            case MEDIUM:
                return LocalDate.now().plusDays(7); // 1 semaine max
            default:
                return LocalDate.now().plusDays(14); // 2 semaines max
        }
    }
    
    /**
     * Vérifie si c'est un jour travaillé
     */
    private static boolean isWorkingDay(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
    }
    
    /**
     * Vérifie si une chaîne contient un des mots-clés
     */
    private static boolean containsAny(String text, String... keywords) {
        return Arrays.stream(keywords).anyMatch(text::contains);
    }
    
    /**
     * Classe interne pour représenter un créneau horaire
     */
    public static class TimeSlot {
        private final Specialiste specialist;
        private final LocalDateTime dateTime;
        private final double score;
        
        public TimeSlot(Specialiste specialist, LocalDateTime dateTime, double score) {
            this.specialist = specialist;
            this.dateTime = dateTime;
            this.score = score;
        }
        
        public String getDisplayText() {
            return String.format("%s - %s (%s) - Score: %.0f", 
                dateTime.format(DATE_FORMATTER),
                dateTime.format(TIME_FORMATTER),
                specialist.getNomDocteur(),
                score
            );
        }
        
        // Getters
        public Specialiste getSpecialist() { return specialist; }
        public LocalDateTime getDateTime() { return dateTime; }
        public double getScore() { return score; }
    }
    
    /**
     * Niveaux d'urgence
     */
    public enum UrgencyLevel {
        HIGH,    // Urgence haute - dans les 24h
        MEDIUM,  // Urgence moyenne - dans les 3 jours
        LOW,     // Urgence basse - dans les 2 semaines
        NORMAL   // Normal - standard
    }
}

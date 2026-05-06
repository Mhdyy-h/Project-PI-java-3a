package org.example.service;

import org.example.dao.CertificationDAO;
import org.example.model.CertificationRequest;

import java.util.List;
import java.util.stream.Collectors;

public class CertificationService {

    private static CertificationService instance;

    private CertificationService() {}

    public static CertificationService getInstance() {
        if (instance == null) {
            instance = new CertificationService();
        }
        return instance;
    }

    public List<CertificationRequest> getAllRequests() {
        return CertificationDAO.getAllRequests();
    }

    public CertificationStats getStats() {
        List<CertificationRequest> allRequests = getAllRequests();
        
        long pending = allRequests.stream()
            .filter(r -> "EN_ATTENTE".equals(r.getStatut()))
            .count();
        long accepted = allRequests.stream()
            .filter(r -> "ACCEPTE".equals(r.getStatut()))
            .count();
        long refused = allRequests.stream()
            .filter(r -> "REFUSE".equals(r.getStatut()))
            .count();
        
        return new CertificationStats(pending, accepted, refused, allRequests.size());
    }

    public boolean approveRequest(CertificationRequest request) {
        String newRole = "COACH".equals(request.getSpecialite())
            ? "[\"ROLE_COACH\"]"
            : "[\"ROLE_SPECIALISTE\"]";
        
        return CertificationDAO.updateStatut(request, "ACCEPTE", newRole);
    }

    public boolean rejectRequest(CertificationRequest request) {
        return CertificationDAO.updateStatut(request, "REFUSE", null);
    }

    public String extractShortMotivation(String motivation) {
        if (motivation == null || motivation.isBlank()) {
            return "(aucune)";
        }
        if (motivation.length() > 60) {
            return motivation.substring(0, 57) + "...";
        }
        return motivation;
    }

    public static class CertificationStats {
        private final long pending;
        private final long accepted;
        private final long refused;
        private final long total;

        public CertificationStats(long pending, long accepted, long refused, long total) {
            this.pending = pending;
            this.accepted = accepted;
            this.refused = refused;
            this.total = total;
        }

        public long getPending() {
            return pending;
        }

        public long getAccepted() {
            return accepted;
        }

        public long getRefused() {
            return refused;
        }

        public long getTotal() {
            return total;
        }
    }
}

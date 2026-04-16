package org.example.service;

import org.example.dao.CertificationDAO;
import org.example.dao.UserDAO;
import org.example.model.User;

import java.util.List;

public class DashboardService {

    private static DashboardService instance;

    private DashboardService() {}

    public static DashboardService getInstance() {
        if (instance == null) {
            instance = new DashboardService();
        }
        return instance;
    }

    public DashboardStats loadStats() {
        try {
            List<User> users = UserDAO.getAllUsers();
            int pendingCerts = CertificationDAO.countPending();
            
            return new DashboardStats(
                users.size(),
                pendingCerts,
                users
            );
        } catch (Exception e) {
            return new DashboardStats(0, 0, List.of());
        }
    }

    public String getUserInitials(String name) {
        if (name == null || name.isEmpty()) return "??";
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
        }
        return name.substring(0, Math.min(2, name.length())).toUpperCase();
    }

    public static class DashboardStats {
        private final int totalUsers;
        private final int pendingCertifications;
        private final List<User> recentUsers;

        public DashboardStats(int totalUsers, int pendingCertifications, List<User> recentUsers) {
            this.totalUsers = totalUsers;
            this.pendingCertifications = pendingCertifications;
            this.recentUsers = recentUsers;
        }

        public int getTotalUsers() {
            return totalUsers;
        }

        public int getPendingCertifications() {
            return pendingCertifications;
        }

        public List<User> getRecentUsers() {
            return recentUsers;
        }
    }
}

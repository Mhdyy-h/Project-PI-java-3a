package org.example.service;

import org.example.dao.ActivityLogDAO;

import java.util.Map;

public class ActivityLogService {

    private static ActivityLogService instance;

    private ActivityLogService() {}

    public static ActivityLogService getInstance() {
        if (instance == null) {
            instance = new ActivityLogService();
        }
        return instance;
    }

    public int getTotalLogs() {
        return ActivityLogDAO.getTotalCount();
    }

    public int getLogsToday() {
        return ActivityLogDAO.getTodayCount();
    }

    public int getLogsThisWeek() {
        return ActivityLogDAO.getThisWeekCount();
    }

    public int getLogsThisMonth() {
        return ActivityLogDAO.getThisMonthCount();
    }

    public Map<String, Integer> getActivityLast30Days() {
        return ActivityLogDAO.getActivityLast30Days();
    }

    public Map<String, Integer> getCountByRole() {
        return ActivityLogDAO.getCountByRole();
    }

    public Map<String, Integer> getTop5Users() {
        return ActivityLogDAO.getTop5Users();
    }

    public int[] getHourlyDistribution() {
        return ActivityLogDAO.getHourlyDistribution();
    }

    public Map<String, Integer> getTop5Actions() {
        return ActivityLogDAO.getTop5Actions();
    }
}

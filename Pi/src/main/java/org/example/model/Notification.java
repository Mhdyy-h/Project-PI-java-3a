package org.example.model;

import java.time.LocalDateTime;

public class Notification {
    private int id;
    private String title;
    private String message;
    private String type; // "info", "success", "warning", "error"
    private LocalDateTime timestamp;
    private boolean read;
    private int userId;
    private int rendezVousId;
    
    public Notification() {
        this.timestamp = LocalDateTime.now();
        this.read = false;
    }
    
    public Notification(String title, String message, String type, int userId) {
        this();
        this.title = title;
        this.message = message;
        this.type = type;
        this.userId = userId;
    }
    
    public Notification(String title, String message, String type, int userId, int rendezVousId) {
        this(title, message, type, userId);
        this.rendezVousId = rendezVousId;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public boolean isRead() {
        return read;
    }
    
    public void setRead(boolean read) {
        this.read = read;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public int getRendezVousId() {
        return rendezVousId;
    }
    
    public void setRendezVousId(int rendezVousId) {
        this.rendezVousId = rendezVousId;
    }
    
    public String getFormattedTimestamp() {
        return timestamp.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
    
    public String getIcon() {
        switch (type) {
            case "success":
                return "✅";
            case "warning":
                return "⚠️";
            case "error":
                return "❌";
            case "info":
            default:
                return "ℹ️";
        }
    }
    
    public String getColorStyle() {
        switch (type) {
            case "success":
                return "-fx-background-color: #d1fae5; -fx-border-color: #10b981; -fx-text-fill: #065f46;";
            case "warning":
                return "-fx-background-color: #fef3c7; -fx-border-color: #f59e0b; -fx-text-fill: #92400e;";
            case "error":
                return "-fx-background-color: #fee2e2; -fx-border-color: #ef4444; -fx-text-fill: #991b1b;";
            case "info":
            default:
                return "-fx-background-color: #dbeafe; -fx-border-color: #3b82f6; -fx-text-fill: #1e40af;";
        }
    }
}

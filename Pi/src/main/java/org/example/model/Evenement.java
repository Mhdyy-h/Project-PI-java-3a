package org.example.model;

import java.sql.Timestamp;

public class Evenement {
    private int id;
    private String titreEvent;
    private Timestamp dateEvent;
    private int pointsParticipation;
    private int groupeId;
    private String locationName;
    private String address;
    private double latitude;
    private double longitude;

    public Evenement() {}

    public Evenement(int id, String titreEvent, Timestamp dateEvent, int pointsParticipation, int groupeId, String locationName, String address) {
        this.id = id;
        this.titreEvent = titreEvent;
        this.dateEvent = dateEvent;
        this.pointsParticipation = pointsParticipation;
        this.groupeId = groupeId;
        this.locationName = locationName;
        this.address = address;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitreEvent() { return titreEvent; }
    public void setTitreEvent(String titreEvent) { this.titreEvent = titreEvent; }
    public Timestamp getDateEvent() { return dateEvent; }
    public void setDateEvent(Timestamp dateEvent) { this.dateEvent = dateEvent; }
    public int getPointsParticipation() { return pointsParticipation; }
    public void setPointsParticipation(int pointsParticipation) { this.pointsParticipation = pointsParticipation; }
    public int getGroupeId() { return groupeId; }
    public void setGroupeId(int groupeId) { this.groupeId = groupeId; }
    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
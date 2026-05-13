package org.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.web.WebView;
import org.example.dao.EvenementDAO;
import org.example.dao.GroupeDAO;
import org.example.model.Evenement;
import org.example.model.Groupe;
import org.example.model.User;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;

public class EventFormController {

    @FXML private TextField titreField, pointsField, locationField, addressField;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<Groupe> groupCombo;
    @FXML private Label statusLabel, formTitle;
    @FXML private WebView mapView;

    @FXML
    public void initialize() {
        loadGroups();
        initMap();
    }

    private void initMap() {
        // We check if WebView is available (Maven must be reloaded)
        if (mapView == null) return;

        String html = "<html><head>" +
                "<link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'/>" +
                "<script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>" +
                "</head><body style='margin:0;'><div id='map' style='height:100vh;'></div>" +
                "<script>" +
                "var map = L.map('map').setView([36.8065, 10.1815], 13);" +
                "L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(map);" +
                "var marker;" +
                "map.on('click', function(e) {" +
                "  if(marker) map.removeLayer(marker);" +
                "  marker = L.marker(e.latlng).addTo(map);" +
                "  fetch('https://nominatim.openstreetmap.org/reverse?format=json&lat='+e.latlng.lat+'&lon='+e.latlng.lng)" +
                "    .then(res => res.json()).then(data => { alert(data.display_name); });" +
                "});" +
                "</script></body></html>";

        mapView.getEngine().loadContent(html);
        mapView.getEngine().setOnAlert(event -> {
            if (addressField != null) addressField.setText(event.getData());
        });
    }

    private void loadGroups() {
        try {
            groupCombo.getItems().setAll(GroupeDAO.getAllGroups());
            groupCombo.setButtonCell(new ListCell<>() {
                @Override protected void updateItem(Groupe g, boolean empty) {
                    super.updateItem(g, empty);
                    setText(empty ? "" : g.getNomGroupe());
                }
            });
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    private void handleSave() {
        try {
            Evenement ev = (editingEvent == null) ? new Evenement() : editingEvent;
            ev.setTitreEvent(titreField.getText());
            ev.setDateEvent(Timestamp.valueOf(datePicker.getValue().atStartOfDay()));
            ev.setPointsParticipation(Integer.parseInt(pointsField.getText()));
            ev.setGroupeId(groupCombo.getValue().getId());
            ev.setLocationName(locationField.getText());
            ev.setAddress(addressField.getText());

            if (editingEvent == null) EvenementDAO.create(ev);
            else EvenementDAO.update(ev);

            handleCancel();
        } catch (Exception e) {
            if (statusLabel != null) statusLabel.setText("Erreur : " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/community_home.fxml"));
            Parent root = loader.load();
            CommunityController ctrl = loader.getController();
            ctrl.setCurrentUser(currentUser);
            titreField.getScene().setRoot(root);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private Evenement editingEvent = null;
    private User currentUser = null;

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public void setEventData(Evenement e) {
        this.editingEvent = e;
        titreField.setText(e.getTitreEvent());
        pointsField.setText(String.valueOf(e.getPointsParticipation()));
        locationField.setText(e.getLocationName());
        addressField.setText(e.getAddress());
        datePicker.setValue(e.getDateEvent().toLocalDateTime().toLocalDate());
    }
}
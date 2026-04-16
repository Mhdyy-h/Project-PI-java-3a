package org.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.dao.EvenementDAO;
import org.example.dao.GroupeDAO;
import org.example.model.Evenement;
import org.example.model.Groupe;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class EventFormController {
    @FXML private TextField titreField, pointsField, locationField, addressField;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<Groupe> groupCombo;
    @FXML private Label statusLabel, formTitle;

    private Evenement existingEvent;

    @FXML
    public void initialize() {
        try {
            groupCombo.getItems().addAll(GroupeDAO.getAllGroups());
            // Customizing ComboBox to show Group Names
            groupCombo.setCellFactory(lv -> new ListCell<>() {
                @Override protected void updateItem(Groupe item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "" : item.getNomGroupe());
                }
            });
            groupCombo.setButtonCell(new ListCell<>() {
                @Override protected void updateItem(Groupe item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "" : item.getNomGroupe());
                }
            });
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void setEventData(Evenement e) {
        this.existingEvent = e;
        formTitle.setText("Modifier l'Événement");
        titreField.setText(e.getTitreEvent());
        datePicker.setValue(e.getDateEvent().toLocalDateTime().toLocalDate());
        pointsField.setText(String.valueOf(e.getPointsParticipation()));
        locationField.setText(e.getLocationName());
        addressField.setText(e.getAddress());
    }

    @FXML
    private void handleSave() {
        try {
            // 1. Mandatory Check
            if (titreField.getText().isEmpty() || datePicker.getValue() == null || groupCombo.getValue() == null) {
                showStatus("Veuillez remplir les champs obligatoires (*)", true);
                return;
            }

            // 2. Future Date Check (Logic requirement)
            if (datePicker.getValue().isBefore(LocalDate.now())) {
                showStatus("La date doit être dans le futur !", true);
                return;
            }

            // 3. Numeric Check
            int points = Integer.parseInt(pointsField.getText());

            Timestamp ts = Timestamp.valueOf(datePicker.getValue().atStartOfDay());

            if (existingEvent == null) {
                Evenement newE = new Evenement(0, titreField.getText(), ts, points, groupCombo.getValue().getId(), locationField.getText(), addressField.getText());
                EvenementDAO.create(newE);
            } else {
                existingEvent.setTitreEvent(titreField.getText());
                existingEvent.setDateEvent(ts);
                existingEvent.setPointsParticipation(points);
                existingEvent.setLocationName(locationField.getText());
                existingEvent.setAddress(addressField.getText());
                EvenementDAO.update(existingEvent);
            }
            handleCancel();
        } catch (NumberFormatException e) {
            showStatus("Les points doivent être un nombre !", true);
        } catch (Exception e) {
            showStatus("Erreur: " + e.getMessage(), true);
        }
    }

    private void showStatus(String msg, boolean isError) {
        statusLabel.setText(msg);
        statusLabel.setStyle(isError ? "-fx-text-fill: #ef4444;" : "-fx-text-fill: #10b981;");
    }

    @FXML private void handleCancel() {
        try {
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("/view/community_home.fxml"));
            titreField.getScene().setRoot(root);
        } catch (Exception e) { e.printStackTrace(); }
    }
}
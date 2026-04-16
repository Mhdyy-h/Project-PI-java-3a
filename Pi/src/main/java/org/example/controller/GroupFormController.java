package org.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import org.example.dao.GroupeDAO;
import org.example.model.Groupe;
import java.io.IOException;

public class GroupFormController {
    @FXML private TextField nomField;
    @FXML private TextField capaciteField;
    @FXML private ComboBox<String> themeCombo;
    @FXML private TextArea descArea;
    @FXML private Label statusLabel;
    @FXML private Label formTitle;

    private Groupe existingGroupe = null;

    @FXML
    public void initialize() {
        themeCombo.getItems().addAll("Nutrition", "Fitness", "Mental Health", "Sommeil", "Relaxation", "Running");
    }

    /**
     * This method was missing! It allows the Admin to edit a group.
     */
    public void setGroupeData(Groupe g) {
        this.existingGroupe = g;
        if (formTitle != null) formTitle.setText("Modifier le Groupe");
        nomField.setText(g.getNomGroupe());
        themeCombo.setValue(g.getThematique());
        capaciteField.setText(String.valueOf(g.getCapaciteMax()));
        descArea.setText(g.getDescription());
    }

    @FXML
    private void handleSave() {
        try {
            String nom = nomField.getText().trim();
            String capStr = capaciteField.getText().trim();

            if (nom.isEmpty() || capStr.isEmpty() || themeCombo.getValue() == null) {
                statusLabel.setText("Champs obligatoires manquants.");
                return;
            }

            int cap = Integer.parseInt(capStr);

            if (GroupeDAO.nameExists(nom, (existingGroupe != null ? existingGroupe.getId() : -1))) {
                statusLabel.setText("Ce nom de groupe existe déjà.");
                return;
            }

            if (existingGroupe == null) {
                // CREATE
                Groupe newG = new Groupe(0, nom, themeCombo.getValue(), descArea.getText(), cap, "default.png");
                GroupeDAO.create(newG);
            } else {
                // UPDATE
                existingGroupe.setNomGroupe(nom);
                existingGroupe.setThematique(themeCombo.getValue());
                existingGroupe.setCapaciteMax(cap);
                existingGroupe.setDescription(descArea.getText());
                GroupeDAO.update(existingGroupe);
            }
            handleCancel();
        } catch (Exception e) {
            statusLabel.setText("Erreur : " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/community_home.fxml"));
            nomField.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
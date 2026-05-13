package org.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import org.example.dao.GroupeDAO;
import org.example.model.Groupe;
import org.example.model.User;
import java.io.File;
import java.io.IOException;

public class GroupFormController {
    @FXML private TextField nomField;
    @FXML private TextField capaciteField;
    @FXML private ComboBox<String> themeCombo;
    @FXML private TextArea descArea;
    @FXML private Label statusLabel;
    @FXML private Label formTitle;
    @FXML private ImageView imagePreview;

    private String imagePath = "default.png";
    private Groupe existingGroupe = null;
    private User currentUser = null;

    @FXML
    public void initialize() {
        themeCombo.getItems().addAll("Nutrition", "Fitness", "Mental Health", "Sommeil", "Relaxation", "Running");
        statusLabel.setStyle("-fx-text-fill: #ef4444;");
        // Load a default placeholder
        imagePreview.setImage(new Image("https://via.placeholder.com/180x120.png?text=No+Image"));
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public void setGroupeData(Groupe g) {        this.existingGroupe = g;
        if (formTitle != null) formTitle.setText("Modifier le Groupe");
        nomField.setText(g.getNomGroupe());
        themeCombo.setValue(g.getThematique());
        capaciteField.setText(String.valueOf(g.getCapaciteMax()));
        descArea.setText(g.getDescription());
        this.imagePath = g.getImage();

        if (imagePath != null && !imagePath.equals("default.png")) {
            try {
                imagePreview.setImage(new Image(new File(imagePath).toURI().toString()));
            } catch (Exception e) {
                System.err.println("Could not load image: " + imagePath);
            }
        }
    }

    @FXML
    private void handleChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir l'image du groupe");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );
        File selectedFile = fileChooser.showOpenDialog(nomField.getScene().getWindow());
        if (selectedFile != null) {
            this.imagePath = selectedFile.getAbsolutePath();
            imagePreview.setImage(new Image(selectedFile.toURI().toString()));
        }
    }

    @FXML
    private void handleSave() {
        try {
            String nom = nomField.getText().trim();
            String capStr = capaciteField.getText().trim();

            if (nom.isEmpty() || capStr.isEmpty() || themeCombo.getValue() == null) {
                statusLabel.setText("❌ Champs obligatoires manquants.");
                return;
            }

            int cap = Integer.parseInt(capStr);
            if (cap < 1 || cap > 20) {
                statusLabel.setText("❌ La capacité doit être entre 1 et 20.");
                return;
            }

            if (GroupeDAO.nameExists(nom, (existingGroupe != null ? existingGroupe.getId() : -1))) {
                statusLabel.setText("❌ Ce nom de groupe existe déjà.");
                return;
            }

            if (existingGroupe == null) {
                Groupe newG = new Groupe(0, nom, themeCombo.getValue(), descArea.getText(), cap, imagePath);
                GroupeDAO.create(newG);
            } else {
                existingGroupe.setNomGroupe(nom);
                existingGroupe.setThematique(themeCombo.getValue());
                existingGroupe.setCapaciteMax(cap);
                existingGroupe.setDescription(descArea.getText());
                existingGroupe.setImage(imagePath);
                GroupeDAO.update(existingGroupe);
            }

            handleCancel();
        } catch (Exception e) {
            statusLabel.setText("❌ Erreur : " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/community_home.fxml"));
            Parent root = loader.load();
            CommunityController ctrl = loader.getController();
            ctrl.setCurrentUser(currentUser);
            nomField.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
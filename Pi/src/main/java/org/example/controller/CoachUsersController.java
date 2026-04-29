package org.example.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.example.dao.UserDAO;
import org.example.model.User;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller de la liste des utilisateurs – Vue Coach.
 * Accessible depuis AdminController quand le rôle est ROLE_COACH.
 */
public class CoachUsersController {

    @FXML private Label coachNameLabel;
    @FXML private Label totalUsersLabel;
    @FXML private TextField rechercheField;
    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, String>  colNom;
    @FXML private TableColumn<User, String>  colEmail;
    @FXML private TableColumn<User, Integer> colScore;
    @FXML private TableColumn<User, String>  colDate;
    @FXML private TableColumn<User, Void>    colAction;

    private User coachUser;
    private ObservableList<User> usersList = FXCollections.observableArrayList();
    private FilteredList<User>   usersFiltres;

    // ─────────────────────────────────────────────────
    //  INITIALISATION
    // ─────────────────────────────────────────────────

    @FXML
    public void initialize() {
        colNom.setCellValueFactory(new PropertyValueFactory<>("nomComplet"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colScore.setCellValueFactory(new PropertyValueFactory<>("scoreGlobal"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateInscription"));

        // Colonne action avec bouton "Voir détails"
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Voir détails");
            {
                btn.setStyle("-fx-background-color: #4C6FFF; -fx-text-fill: white; " +
                        "-fx-background-radius: 6; -fx-padding: 5 12; -fx-cursor: hand; -fx-font-size: 12px;");
                btn.setOnAction(e -> {
                    User user = getTableView().getItems().get(getIndex());
                    ouvrirRepasUtilisateur(user);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        // Filtrage dynamique
        usersFiltres = new FilteredList<>(usersList, p -> true);
        usersTable.setItems(usersFiltres);

        rechercheField.textProperty().addListener((obs, o, n) -> appliquerFiltre());

        chargerUtilisateurs();
    }

    public void setCoachUser(User coach) {
        this.coachUser = coach;
        if (coach != null) {
            coachNameLabel.setText("Coach : " + coach.getNomComplet());
        }
    }

    // ─────────────────────────────────────────────────
    //  CHARGEMENT
    // ─────────────────────────────────────────────────

    private void chargerUtilisateurs() {
        List<User> tous = UserDAO.getAllUsers();
        // Filtre sur les utilisateurs non-coach, non-admin
        List<User> utilisateurs = tous.stream()
                .filter(u -> {
                    String roles = u.getRoles();
                    if (roles == null) return true;
                    return !roles.contains("ADMIN") && !roles.contains("COACH")
                            && !roles.contains("SPECIALISTE");
                })
                .collect(Collectors.toList());
        usersList.setAll(utilisateurs);
        totalUsersLabel.setText(String.valueOf(utilisateurs.size()));
    }

    private void appliquerFiltre() {
        String txt = rechercheField.getText() != null
                ? rechercheField.getText().toLowerCase().trim() : "";
        usersFiltres.setPredicate(u -> {
            if (txt.isEmpty()) return true;
            return u.getNomComplet().toLowerCase().contains(txt)
                    || u.getEmail().toLowerCase().contains(txt);
        });
    }

    // ─────────────────────────────────────────────────
    //  NAVIGATION
    // ─────────────────────────────────────────────────

    private void ouvrirRepasUtilisateur(User selectedUser) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/nutrition/coach_repas.fxml"));
            Parent root = loader.load();
            CoachRepasController ctrl = loader.getController();
            ctrl.setCoachUser(coachUser);
            ctrl.setSelectedUser(selectedUser);

            Stage stage = (Stage) usersTable.getScene().getWindow();
            stage.setScene(new Scene(root, stage.getScene().getWidth(), stage.getScene().getHeight()));
            stage.setTitle("BioSync – Repas de " + selectedUser.getNomComplet());
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Impossible d'ouvrir la vue des repas : " + e.getMessage());
        }
    }

    @FXML
    private void retournerVersDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/dashboard.fxml"));
            Parent root = loader.load();
            AdminController ctrl = loader.getController();
            if (coachUser != null) ctrl.setUser(coachUser);
            Stage stage = (Stage) usersTable.getScene().getWindow();
            stage.setScene(new Scene(root, stage.getScene().getWidth(), stage.getScene().getHeight()));
            stage.setTitle("BioSync – Dashboard");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}

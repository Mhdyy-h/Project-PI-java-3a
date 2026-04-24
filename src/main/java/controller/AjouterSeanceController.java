package controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import model.SeanceSport;
import service.ServiceSeanceSport;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class AjouterSeanceController {

    @FXML private TextField        nomField;
    @FXML private TextField        heureDebutField;
    @FXML private TextField        dureeField;
    @FXML private TextField        dateField;
    @FXML private ComboBox<String> medailleBox;
    @FXML private TextField        utilisateurIdField;
    @FXML private TextField        heureDebutReelleField;
    @FXML private ComboBox<String> alerteBox;

    @FXML private Label errNom;
    @FXML private Label errHeure;
    @FXML private Label errDuree;
    @FXML private Label errDate;
    @FXML private Label errMedaille;
    @FXML private Label errUtilisateur;
    @FXML private Label errHeureReelle;
    @FXML private Label errAlerte;
    @FXML private Label messageLabel;

    @FXML private Button btnAfficher;
    @FXML private Button btnMenu;

    private final String STYLE_NORMAL =
            "-fx-background-color: #faf8ff; -fx-text-fill: #4a235a;" +
                    "-fx-border-color: #d4af37; -fx-border-radius: 12;" +
                    "-fx-background-radius: 12; -fx-pref-height: 46;" +
                    "-fx-font-size: 13px; -fx-padding: 0 16;";

    private final String STYLE_ERREUR =
            "-fx-background-color: #fff5f5; -fx-text-fill: #4a235a;" +
                    "-fx-border-color: #e94560; -fx-border-width: 2;" +
                    "-fx-border-radius: 12; -fx-background-radius: 12;" +
                    "-fx-pref-height: 46; -fx-font-size: 13px; -fx-padding: 0 16;";

    private final String STYLE_OK =
            "-fx-background-color: #f0fff6; -fx-text-fill: #4a235a;" +
                    "-fx-border-color: #27ae60; -fx-border-width: 2;" +
                    "-fx-border-radius: 12; -fx-background-radius: 12;" +
                    "-fx-pref-height: 46; -fx-font-size: 13px; -fx-padding: 0 16;";

    private final String STYLE_ERR_LABEL =
            "-fx-font-size: 11px; -fx-text-fill: #e94560;" +
                    "-fx-font-weight: bold; -fx-padding: 2 0 0 5;";

    private final String STYLE_OK_LABEL =
            "-fx-font-size: 11px; -fx-text-fill: #27ae60;" +
                    "-fx-font-weight: bold; -fx-padding: 2 0 0 5;";

    // ── Initialize ────────────────────────────────────────────

    @FXML
    public void initialize() {
        medailleBox.setItems(FXCollections.observableArrayList(
                "Or", "Argent", "Bronze", "Aucune"));
        alerteBox.setItems(FXCollections.observableArrayList(
                "0 - Non envoyée", "1 - Envoyée"));

        // Validation temps réel
        nomField.textProperty().addListener((o, a, b)           -> validerNom());
        heureDebutField.textProperty().addListener((o, a, b)    -> validerHeure(heureDebutField, errHeure, true));
        dureeField.textProperty().addListener((o, a, b)         -> validerDuree());
        dateField.textProperty().addListener((o, a, b)          -> validerDate());
        utilisateurIdField.textProperty().addListener((o, a, b) -> validerUtilisateur());
        heureDebutReelleField.textProperty().addListener((o, a, b) -> validerHeureReelle());
        medailleBox.valueProperty().addListener((o, a, b)       -> validerMedaille());
        alerteBox.valueProperty().addListener((o, a, b)         -> validerAlerte());
    }

    // ── Validations ───────────────────────────────────────────

    private boolean validerNom() {
        String val = nomField.getText().trim();
        if (val.isEmpty()) {
            setErreur(nomField, errNom, "❌ Le nom est obligatoire !");
            return false;
        }
        if (val.length() < 3) {
            setErreur(nomField, errNom, "❌ Minimum 3 caractères !");
            return false;
        }
        if (val.length() > 50) {
            setErreur(nomField, errNom, "❌ Maximum 50 caractères !");
            return false;
        }
        if (!val.matches("[a-zA-ZÀ-ÿ\\s]+")) {
            setErreur(nomField, errNom, "❌ Lettres uniquement ! Pas de chiffres ni symboles !");
            return false;
        }
        setOk(nomField, errNom, "✅ Nom valide");
        return true;
    }

    private boolean validerHeure(TextField field, Label label, boolean obligatoire) {
        String val = field.getText().trim();
        if (val.isEmpty()) {
            if (obligatoire) {
                setErreur(field, label, "❌ L'heure est obligatoire !");
                return false;
            }
            field.setStyle(STYLE_NORMAL);
            label.setText("(optionnel)");
            label.setStyle("-fx-font-size: 11px; -fx-text-fill: #9b89a8;");
            return true;
        }
        if (!val.matches("([01]\\d|2[0-3]):[0-5]\\d:[0-5]\\d")) {
            setErreur(field, label, "❌ Format invalide ! HH:MM:SS (ex: 08:30:00)");
            return false;
        }
        setOk(field, label, "✅ Heure valide");
        return true;
    }

    private boolean validerDuree() {
        String val = dureeField.getText().trim();
        if (val.isEmpty()) {
            setErreur(dureeField, errDuree, "❌ La durée est obligatoire !");
            return false;
        }
        if (!val.matches("\\d+")) {
            setErreur(dureeField, errDuree, "❌ Nombre entier uniquement ! (ex: 45)");
            return false;
        }
        int d = Integer.parseInt(val);
        if (d < 5) {
            setErreur(dureeField, errDuree, "❌ Minimum 5 minutes !");
            return false;
        }
        if (d > 120) {
            setErreur(dureeField, errDuree, "❌ Maximum 120 minutes (2 heures) !");
            return false;
        }
        setOk(dureeField, errDuree, "✅ Durée valide — " + d + " min");
        return true;
    }

    private boolean validerDate() {
        String val = dateField.getText().trim();
        if (val.isEmpty()) {
            setErreur(dateField, errDate, "❌ La date est obligatoire !");
            return false;
        }
        if (!val.matches("\\d{4}-\\d{2}-\\d{2}")) {
            setErreur(dateField, errDate, "❌ Format invalide ! Utilisez YYYY-MM-DD");
            return false;
        }
        try {
            java.time.LocalDate date    = java.time.LocalDate.parse(val);
            java.time.LocalDate today   = java.time.LocalDate.now();
            java.time.LocalDate minDate = today.minusYears(1);
            if (date.isAfter(today)) {
                setErreur(dateField, errDate, "❌ La date ne peut pas être dans le futur !");
                return false;
            }
            if (date.isBefore(minDate)) {
                setErreur(dateField, errDate, "❌ Date trop ancienne ! Maximum 1 an en arrière");
                return false;
            }
        } catch (Exception e) {
            setErreur(dateField, errDate, "❌ Date invalide ! Vérifiez le jour et le mois");
            return false;
        }
        setOk(dateField, errDate, "✅ Date valide");
        return true;
    }

    private boolean validerMedaille() {
        if (medailleBox.getValue() == null) {
            errMedaille.setText("❌ Choisissez une médaille !");
            errMedaille.setStyle(STYLE_ERR_LABEL);
            return false;
        }
        errMedaille.setText("✅ Médaille : " + medailleBox.getValue());
        errMedaille.setStyle(STYLE_OK_LABEL);
        return true;
    }

    private boolean validerUtilisateur() {
        String val = utilisateurIdField.getText().trim();
        if (val.isEmpty()) {
            setErreur(utilisateurIdField, errUtilisateur, "❌ L'ID utilisateur est obligatoire !");
            return false;
        }
        if (!val.matches("\\d+")) {
            setErreur(utilisateurIdField, errUtilisateur, "❌ Nombre entier uniquement ! (ex: 1, 2, 15)");
            return false;
        }
        if (Integer.parseInt(val) <= 0) {
            setErreur(utilisateurIdField, errUtilisateur, "❌ L'ID doit être positif (> 0) !");
            return false;
        }
        setOk(utilisateurIdField, errUtilisateur, "✅ ID valide");
        return true;
    }

    private boolean validerHeureReelle() {
        return validerHeure(heureDebutReelleField, errHeureReelle, false);
    }

    private boolean validerAlerte() {
        if (alerteBox.getValue() == null) {
            errAlerte.setText("❌ Choisissez le statut de l'alerte !");
            errAlerte.setStyle(STYLE_ERR_LABEL);
            return false;
        }
        errAlerte.setText("✅ Statut : " + alerteBox.getValue());
        errAlerte.setStyle(STYLE_OK_LABEL);
        return true;
    }

    // ── Soumission ────────────────────────────────────────────

    @FXML
    public void ajouterSeance() {
        boolean ok =
                validerNom() &
                        validerHeure(heureDebutField, errHeure, true) &
                        validerDuree() &
                        validerDate() &
                        validerMedaille() &
                        validerUtilisateur() &
                        validerHeureReelle() &
                        validerAlerte();

        if (!ok) {
            messageLabel.setStyle("-fx-text-fill: #e94560; -fx-font-size: 13px; -fx-font-weight: bold;");
            messageLabel.setText("⚠️ Corrigez les erreurs avant de continuer !");
            return;
        }

        try {
            SeanceSport s = new SeanceSport();
            s.setNomSeance(nomField.getText().trim());
            s.setHeureDebut(heureDebutField.getText().trim());
            s.setDureeMinutes(Integer.parseInt(dureeField.getText().trim()));
            s.setDateSeance(dateField.getText().trim());
            s.setMedailleObtenue(medailleBox.getValue());
            s.setUtilisateurId(Integer.parseInt(utilisateurIdField.getText().trim()));
            s.setHeureDebutReelle(
                    heureDebutReelleField.getText().trim().isEmpty()
                            ? null : heureDebutReelleField.getText().trim());
            s.setAlerteEnvoyee(alerteBox.getValue().startsWith("1") ? 1 : 0);

            new ServiceSeanceSport().ajouter(s);

            messageLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 13px; -fx-font-weight: bold;");
            messageLabel.setText("✅ Séance ajoutée avec succès !");
            reinitialiser();

        } catch (Exception e) {
            messageLabel.setStyle("-fx-text-fill: #e94560; -fx-font-size: 13px; -fx-font-weight: bold;");
            messageLabel.setText("❌ Erreur BD : " + e.getMessage());
        }
    }

    private void reinitialiser() {
        nomField.clear(); heureDebutField.clear();
        dureeField.clear(); dateField.clear();
        medailleBox.setValue(null); utilisateurIdField.clear();
        heureDebutReelleField.clear(); alerteBox.setValue(null);
        nomField.setStyle(STYLE_NORMAL); heureDebutField.setStyle(STYLE_NORMAL);
        dureeField.setStyle(STYLE_NORMAL); dateField.setStyle(STYLE_NORMAL);
        utilisateurIdField.setStyle(STYLE_NORMAL);
        heureDebutReelleField.setStyle(STYLE_NORMAL);
        errNom.setText(""); errHeure.setText(""); errDuree.setText("");
        errDate.setText(""); errMedaille.setText(""); errUtilisateur.setText("");
        errHeureReelle.setText(""); errAlerte.setText("");
        messageLabel.setText("");
    }

    // ── Navigation ────────────────────────────────────────────

    @FXML
    public void lancerAnalyseIA() {
        try {
            List<SeanceSport> seances = new ServiceSeanceSport().afficherAll();
            if (seances.size() < 2) {
                new Alert(Alert.AlertType.WARNING, "⚠️ Il faut au moins 2 séances !").showAndWait();
                return;
            }
            List<Double> historique = seances.stream()
                    .mapToDouble(s -> (double) s.getDureeMinutes())
                    .boxed()
                    .collect(Collectors.toList());
            metier.service.SeanceSportService service = new metier.service.SeanceSportService();
            metier.service.SeanceSportService.RapportProgression rapport =
                    service.predireProgression("user_01", "Séance Sport", historique, 4);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("🤖 Analyse IA");
            alert.setHeaderText("Prédiction sur 4 séances");
            alert.setContentText(rapport.toString());
            alert.showAndWait();
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "❌ " + e.getMessage()).showAndWait();
        }
    }
    public void naviguerVersAfficher(ActionEvent e) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/AfficherSeance.fxml"));
            btnAfficher.getScene().setRoot(root);
        } catch (IOException ex) { System.err.println(ex.getMessage()); }
    }

    @FXML
    public void ouvrirMenu(ActionEvent e) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Menu.fxml"));
            btnMenu.getScene().setRoot(root);
        } catch (IOException ex) { System.err.println(ex.getMessage()); }
    }

    // ── Helpers visuels ───────────────────────────────────────

    private void setErreur(TextField field, Label label, String msg) {
        field.setStyle(STYLE_ERREUR);
        label.setText(msg);
        label.setStyle(STYLE_ERR_LABEL);
    }

    private void setOk(TextField field, Label label, String msg) {
        field.setStyle(STYLE_OK);
        label.setText(msg);
        label.setStyle(STYLE_OK_LABEL);
    }
}
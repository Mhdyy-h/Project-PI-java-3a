package org.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.model.Quiz;
import org.example.model.User;
import org.example.service.NavigationService;
import org.example.service.QuizService;
import org.example.util.AlertHelper;
import org.example.util.InputValidator;

import java.util.List;

/**
 * Contrôleur de quiz_form.fxml
 * Modes : CREATE (quiz == null) et EDIT (quiz != null)
 */
public class QuizFormController {

    // ── Header ─────────────────────────────────────────────────
    @FXML private Label formTitleLabel;
    @FXML private Label formSubtitleLabel;

    // ── Champs ─────────────────────────────────────────────────
    @FXML private TextField          titleField;
    @FXML private Label              titleErrorLabel;
    @FXML private TextArea           descriptionArea;
    @FXML private ComboBox<String>   categoryCombo;
    @FXML private Label              categoryErrorLabel;
    @FXML private ComboBox<String>   difficultyCombo;
    @FXML private TextField          passingScoreField;
    @FXML private Label              passingScoreErrorLabel;
    @FXML private CheckBox           activeCheckBox;
    @FXML private Label              statusLabel;
    @FXML private Button             saveButton;

    // ── État ───────────────────────────────────────────────────
    private final QuizService quizService = new QuizService();
    private Quiz   quizEnEdition;   // null = mode CREATE
    private User   currentUser;

    // ── Cycle de vie ───────────────────────────────────────────

    @FXML
    public void initialize() {
        categoryCombo.getItems().addAll(
                "Stress", "Anxiété", "Dépression", "Bien-être",
                "Mémoire", "Concentration", "Sommeil", "Relations", "Autre");

        difficultyCombo.getItems().addAll("FACILE", "MOYEN", "DIFFICILE");
        difficultyCombo.setValue("MOYEN");

        // Mode CREATE par défaut
        formTitleLabel.setText("Nouveau Quiz");
        formSubtitleLabel.setText("Remplissez les informations du quiz mental");
    }

    // ── API publique ────────────────────────────────────────────

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    /** Pré-remplit le formulaire en mode EDIT. */
    public void setQuiz(Quiz quiz) {
        this.quizEnEdition = quiz;
        if (quiz == null) return;

        formTitleLabel.setText("Modifier le Quiz");
        formSubtitleLabel.setText("Modifiez les informations du quiz");
        saveButton.setText("Mettre à jour");

        titleField.setText(quiz.getTitre());
        descriptionArea.setText(quiz.getDescription());
        categoryCombo.setValue(quiz.getCategorie());
        difficultyCombo.setValue(quiz.getDifficulte());
        passingScoreField.setText(String.valueOf(quiz.getPassingScore()));
        activeCheckBox.setSelected(quiz.isActif());
    }

    // ── Actions FXML ────────────────────────────────────────────

    @FXML
    private void handleSave() {
        masquerErreurs();

        String titre        = titleField.getText();
        String categorie    = categoryCombo.getValue();
        String difficulte   = difficultyCombo.getValue();
        String passingScore = passingScoreField.getText();

        List<String> errors = InputValidator.validateQuiz(titre, categorie, difficulte, passingScore);

        if (!errors.isEmpty()) {
            afficherErreurs(errors);
            AlertHelper.showWarning("Validation", String.join("\n", errors));
            return;
        }

        Quiz quiz = (quizEnEdition != null) ? quizEnEdition : new Quiz();
        quiz.setTitre(titre.trim());
        quiz.setDescription(descriptionArea.getText());
        quiz.setCategorie(categorie);
        quiz.setDifficulte(difficulte);
        quiz.setPassingScore(Integer.parseInt(passingScore.trim()));
        quiz.setActif(activeCheckBox.isSelected());

        // Set utilisateur_id from current user
        if (currentUser != null) {
            quiz.setUtilisateurId(currentUser.getId());
        } else {
            quiz.setUtilisateurId(1); // Default fallback
        }

        boolean success;
        String  message;

        if (quizEnEdition == null) {
            success = quizService.ajouterQuiz(quiz);
            message = "Le quiz « " + quiz.getTitre() + " » a été ajouté avec succès.";
        } else {
            success = quizService.modifierQuiz(quiz);
            message = "Le quiz « " + quiz.getTitre() + " » a été modifié avec succès.";
        }

        if (success) {
            AlertHelper.showSuccess("Succès", message);
            retournerAuManager();
        } else {
            AlertHelper.showError("Erreur DB", "Une erreur est survenue. Vérifiez votre connexion.");
            statusLabel.setText("❌ Erreur lors de la sauvegarde.");
            statusLabel.setStyle("-fx-text-fill: #EF4444;");
        }
    }

    @FXML
    private void handleCancel() {
        retournerAuManager();
    }

    @FXML
    private void handleRetour() {
        retournerAuManager();
    }

    // ── Helpers ─────────────────────────────────────────────────

    private void retournerAuManager() {
        NavigationService.getInstance().navigateToQuizManager(saveButton, currentUser);
    }

    private void masquerErreurs() {
        titleErrorLabel.setVisible(false);
        titleErrorLabel.setManaged(false);
        categoryErrorLabel.setVisible(false);
        categoryErrorLabel.setManaged(false);
        passingScoreErrorLabel.setVisible(false);
        passingScoreErrorLabel.setManaged(false);
        statusLabel.setText("");
    }

    private void afficherErreurs(List<String> errors) {
        for (String err : errors) {
            if (err.contains("titre")) {
                titleErrorLabel.setText(err);
                titleErrorLabel.setVisible(true);
                titleErrorLabel.setManaged(true);
            } else if (err.contains("catégorie")) {
                categoryErrorLabel.setText(err);
                categoryErrorLabel.setVisible(true);
                categoryErrorLabel.setManaged(true);
            } else if (err.contains("score")) {
                passingScoreErrorLabel.setText(err);
                passingScoreErrorLabel.setVisible(true);
                passingScoreErrorLabel.setManaged(true);
            }
        }
    }
}

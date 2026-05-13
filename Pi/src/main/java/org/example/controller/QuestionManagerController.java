package org.example.controller;

import org.example.model.User;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.model.Question;
import org.example.model.Quiz;
import org.example.service.NavigationService;
import org.example.service.OllamaQcmGeneratorService;
import org.example.service.QuestionService;
import org.example.util.AlertHelper;
import org.example.util.InputValidator;

import java.util.List;

/**
 * Contrôleur de question_manager.fxml
 * Gestion CRUD des questions d'un Quiz donné.
 */
public class QuestionManagerController {

    // ── Breadcrumb / titre ─────────────────────────────────────
    @FXML private Label quizTitleLabel;
    @FXML private Label questionCountLabel;
    @FXML private Label totalPointsLabel;

    // ── Table ──────────────────────────────────────────────────
    @FXML private TableView<Question>            questionTable;
    @FXML private TableColumn<Question, String>  idColumn;
    @FXML private TableColumn<Question, String>  questionTextColumn;
    @FXML private TableColumn<Question, String>  correctAnswerColumn;
    @FXML private TableColumn<Question, String>  pointsColumn;

    // ── Panneau formulaire ─────────────────────────────────────
    @FXML private Label    formPanelTitleLabel;
    @FXML private TextArea questionTextArea;
    @FXML private Label    questionErrorLabel;
    @FXML private TextField optionAField;
    @FXML private TextField optionBField;
    @FXML private TextField optionCField;
    @FXML private TextField optionDField;
    @FXML private Label     optionsErrorLabel;
    @FXML private ComboBox<String> correctAnswerCombo;
    @FXML private TextField pointsField;
    @FXML private TextArea  explanationArea;
    @FXML private Button    saveButton;
    @FXML private Label     statusLabel;

    // ── État ───────────────────────────────────────────────────
    private final QuestionService questionService = new QuestionService();
    private final OllamaQcmGeneratorService ollamaQcmGeneratorService = new OllamaQcmGeneratorService();
    private ObservableList<Question> questionList = FXCollections.observableArrayList();
    private Quiz   currentQuiz;
    private User   currentUser;
    private Question questionEnEdition;   // null = mode CREATE

    // ── Cycle de vie ───────────────────────────────────────────

    @FXML
    public void initialize() {
        configurerColonnes();
        configurerCombo();
        configurerSelectionTable();
    }

    // ── API publique ────────────────────────────────────────────

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public void setQuiz(Quiz quiz) {
        this.currentQuiz = quiz;
        quizTitleLabel.setText("Quiz : " + quiz.getTitre());
        chargerQuestions();
    }

    // ── Configuration ───────────────────────────────────────────

    private void configurerColonnes() {
        idColumn.setCellValueFactory(c ->
                new SimpleStringProperty(String.valueOf(c.getValue().getId())));
        questionTextColumn.setCellValueFactory(c -> {
            String txt = c.getValue().getContenu();
            return new SimpleStringProperty(txt != null && txt.length() > 60
                    ? txt.substring(0, 60) + "…" : txt);
        });
        correctAnswerColumn.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getBonneReponse()));
        pointsColumn.setCellValueFactory(c ->
                new SimpleStringProperty(String.valueOf(c.getValue().getPoints())));

        // Colonne actions (Modifier / Supprimer)
        TableColumn<Question, Void> actCol = new TableColumn<>("Actions");
        actCol.setPrefWidth(160);
        actCol.setSortable(false);
        actCol.setCellFactory(col -> new TableCell<Question, Void>() {
            final Button btnEdit = styledBtn("✏ Modifier", "#4285F4");
            final Button btnDel  = styledBtn("🗑 Suppr.",   "#EF4444");
            {
                btnEdit.setOnAction(e -> chargerQuestionDansForm(questionTable.getItems().get(getIndex())));
                btnDel.setOnAction(e -> supprimerQuestion(questionTable.getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= questionTable.getItems().size()) {
                    setGraphic(null);
                } else {
                    javafx.scene.layout.HBox box = new javafx.scene.layout.HBox(6, btnEdit, btnDel);
                    setGraphic(box);
                }
            }
        });
        questionTable.getColumns().add(actCol);
    }

    private void configurerCombo() {
        correctAnswerCombo.getItems().addAll("A", "B", "C", "D");
    }

    private void configurerSelectionTable() {
        questionTable.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) chargerQuestionDansForm(selected);
        });
    }

    // ── Chargement ──────────────────────────────────────────────

    private void chargerQuestions() {
        if (currentQuiz == null) return;
        questionList = FXCollections.observableArrayList(
                questionService.getQuestionsByQuizId(currentQuiz.getId()));
        questionTable.setItems(questionList);
        mettreAJourCompteurs();
    }

    private void mettreAJourCompteurs() {
        int count = questionList.size();
        int pts   = questionList.stream().mapToInt(Question::getPoints).sum();
        questionCountLabel.setText(count + " question(s)");
        totalPointsLabel.setText(pts + " pts total");
    }

    // ── Formulaire ──────────────────────────────────────────────

    private void chargerQuestionDansForm(Question q) {
        questionEnEdition = q;
        formPanelTitleLabel.setText("Modifier la Question");
        saveButton.setText("Mettre à jour");

        questionTextArea.setText(q.getContenu());
        optionAField.setText(q.getOptionA());
        optionBField.setText(q.getOptionB());
        optionCField.setText(q.getOptionC() != null ? q.getOptionC() : "");
        optionDField.setText(q.getOptionD() != null ? q.getOptionD() : "");
        correctAnswerCombo.setValue(q.getBonneReponse());
        pointsField.setText(String.valueOf(q.getPoints()));
        explanationArea.setText(q.getExplication() != null ? q.getExplication() : "");
        masquerErreurs();
    }

    @FXML
    private void handleSaveQuestion() {
        masquerErreurs();

        String contenu      = questionTextArea.getText();
        String optA         = optionAField.getText();
        String optB         = optionBField.getText();
        String bonneReponse = correctAnswerCombo.getValue();
        String ptsStr       = pointsField.getText();

        List<String> errors = InputValidator.validateQuestion(contenu, optA, optB, bonneReponse, ptsStr);

        if (!errors.isEmpty()) {
            afficherErreurs(errors);
            AlertHelper.showWarning("Validation", String.join("\n", errors));
            return;
        }

        Question question = (questionEnEdition != null) ? questionEnEdition : new Question();
        question.setQuizId(currentQuiz.getId());
        question.setContenu(contenu.trim());
        question.setOptionA(optA.trim());
        question.setOptionB(optB.trim());
        question.setOptionC(optionCField.getText().trim());
        question.setOptionD(optionDField.getText().trim());
        question.setBonneReponse(bonneReponse);
        question.setPoints(Integer.parseInt(ptsStr.trim()));
        question.setExplication(explanationArea.getText().trim());

        boolean success;
        String  msg;

        if (questionEnEdition == null) {
            success = questionService.ajouterQuestion(question);
            msg = "Question ajoutée avec succès.";
        } else {
            success = questionService.modifierQuestion(question);
            msg = "Question modifiée avec succès.";
        }

        if (success) {
            AlertHelper.showSuccess("Succès", msg);
            statusLabel.setText("✅ " + msg);
            statusLabel.setStyle("-fx-text-fill: #6EC5A6;");
            handleClearForm();
            chargerQuestions();
        } else {
            AlertHelper.showError("Erreur DB", "Une erreur est survenue. Vérifiez votre connexion.");
            statusLabel.setText("❌ Erreur lors de la sauvegarde.");
            statusLabel.setStyle("-fx-text-fill: #EF4444;");
        }
    }

    @FXML
    private void handleClearForm() {
        questionEnEdition = null;
        formPanelTitleLabel.setText("Nouvelle Question");
        saveButton.setText("Ajouter la Question");
        questionTextArea.clear();
        optionAField.clear();
        optionBField.clear();
        optionCField.clear();
        optionDField.clear();
        correctAnswerCombo.setValue(null);
        pointsField.setText("1");
        explanationArea.clear();
        statusLabel.setText("");
        masquerErreurs();
        questionTable.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleRetour() {
        NavigationService.getInstance().navigateToQuizManager(saveButton, currentUser);
    }

    @FXML
    private void handleGenerateAi() {
        if (currentQuiz == null) {
            AlertHelper.showWarning("IA", "Veuillez sélectionner un quiz.");
            return;
        }

        TextInputDialog themeDialog = new TextInputDialog(currentQuiz.getTitre() != null ? currentQuiz.getTitre() : "");
        themeDialog.setTitle("Génération IA");
        themeDialog.setHeaderText("Thème des questions");
        themeDialog.setContentText("Thème :");

        String theme = themeDialog.showAndWait().orElse("").trim();
        if (theme.isEmpty()) return;

        TextInputDialog countDialog = new TextInputDialog("5");
        countDialog.setTitle("Génération IA");
        countDialog.setHeaderText("Nombre de questions");
        countDialog.setContentText("Combien ?");

        int count;
        try {
            count = Integer.parseInt(countDialog.showAndWait().orElse("0").trim());
        } catch (Exception e) {
            AlertHelper.showWarning("IA", "Nombre invalide.");
            return;
        }
        if (count <= 0 || count > 20) {
            AlertHelper.showWarning("IA", "Choisissez un nombre entre 1 et 20.");
            return;
        }

        int difficulty = Math.max(1, Math.min(10, currentQuiz.getNiveauStressCible() > 0 ? currentQuiz.getNiveauStressCible() : 5));

        try {
            List<Question> generated = ollamaQcmGeneratorService.generate(theme, difficulty, count, 1);
            if (generated.isEmpty()) {
                AlertHelper.showWarning("IA", "Aucune question générée. Vérifiez qu'Ollama est lancé.");
                return;
            }

            int inserted = 0;
            for (Question q : generated) {
                q.setQuizId(currentQuiz.getId());
                if (questionService.ajouterQuestion(q)) inserted++;
            }

            chargerQuestions();
            AlertHelper.showSuccess("IA", inserted + " question(s) ajoutée(s).");
        } catch (java.net.ConnectException e) {
            AlertHelper.showError("IA Non Disponible", 
                "Impossible de se connecter à Ollama.\n\n" +
                "Solutions:\n" +
                "1. Installer Ollama: https://ollama.ai\n" +
                "2. Lancer Ollama: 'ollama serve'\n" +
                "3. Vérifier que le port 11434 est accessible\n\n" +
                "En attendant, vous pouvez créer les questions manuellement.");
        } catch (Exception e) {
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.contains("Failed to connect")) {
                AlertHelper.showError("IA Non Disponible", 
                    "Impossible de se connecter à Ollama.\n\n" +
                    "Solutions:\n" +
                    "1. Installer Ollama: https://ollama.ai\n" +
                    "2. Lancer Ollama: 'ollama serve'\n" +
                    "3. Vérifier que le port 11434 est accessible\n\n" +
                    "En attendant, vous pouvez créer les questions manuellement.");
            } else {
                AlertHelper.showError("IA", "Erreur génération IA : " + errorMsg);
            }
        }
    }

    // ── Suppression ─────────────────────────────────────────────

    private void supprimerQuestion(Question q) {
        if (q == null) return;
        if (!AlertHelper.showConfirmation("Supprimer", "Supprimer cette question ?")) return;

        if (questionService.supprimerQuestion(q.getId())) {
            questionList.remove(q);
            mettreAJourCompteurs();
            AlertHelper.showSuccess("Succès", "Question supprimée.");
            if (questionEnEdition != null && questionEnEdition.getId() == q.getId()) handleClearForm();
        } else {
            AlertHelper.showError("Erreur", "Une erreur est survenue. Vérifiez votre connexion.");
        }
    }

    // ── Helpers ─────────────────────────────────────────────────

    private void masquerErreurs() {
        questionErrorLabel.setVisible(false);
        questionErrorLabel.setManaged(false);
        optionsErrorLabel.setVisible(false);
        optionsErrorLabel.setManaged(false);
    }

    private void afficherErreurs(List<String> errors) {
        for (String err : errors) {
            if (err.contains("contenu")) {
                questionErrorLabel.setText(err);
                questionErrorLabel.setVisible(true);
                questionErrorLabel.setManaged(true);
            } else if (err.contains("option") || err.contains("Option")) {
                optionsErrorLabel.setText(err);
                optionsErrorLabel.setVisible(true);
                optionsErrorLabel.setManaged(true);
            }
        }
    }

    private Button styledBtn(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; "
                + "-fx-font-size: 11px; -fx-background-radius: 6; -fx-padding: 5 10 5 10; -fx-cursor: hand;");
        return btn;
    }
}


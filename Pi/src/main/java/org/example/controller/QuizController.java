package org.example.controller;

import org.example.model.User;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import org.example.model.Quiz;
import org.example.service.NavigationService;
import org.example.service.QuizService;
import org.example.util.AlertHelper;

import java.util.List;

/**
 * Contrôleur de quiz_manager.fxml
 * Gestion CRUD de la liste des Quiz Mentaux.
 */
public class QuizController {

    // ── Stats ──────────────────────────────────────────────────
    @FXML private Label totalCountLabel;
    @FXML private Label activeCountLabel;
    @FXML private Label inactiveCountLabel;

    // ── Filtres ────────────────────────────────────────────────
    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private ComboBox<String> difficultyFilter;
    @FXML private Label statusLabel;

    // ── Table ──────────────────────────────────────────────────
    @FXML private TableView<Quiz>            quizTable;
    @FXML private TableColumn<Quiz, String>  idColumn;
    @FXML private TableColumn<Quiz, String>  titleColumn;
    @FXML private TableColumn<Quiz, String>  categoryColumn;
    @FXML private TableColumn<Quiz, String>  difficultyColumn;
    @FXML private TableColumn<Quiz, String>  passingScoreColumn;
    @FXML private TableColumn<Quiz, String>  statusColumn;

    // ── État ───────────────────────────────────────────────────
    private final QuizService quizService = new QuizService();
    private ObservableList<Quiz> masterList = FXCollections.observableArrayList();
    private FilteredList<Quiz> filteredList;
    private User currentUser;

    // ── Cycle de vie ───────────────────────────────────────────

    @FXML
    public void initialize() {
        configurerColonnes();
        ajouterColonneActions();
        configurerFiltres();
        chargerQuiz();
    }

    /** Appelé depuis NavigationService pour injecter l'utilisateur courant. */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    // ── Configuration table ─────────────────────────────────────

    private void configurerColonnes() {
        idColumn.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getId())));
        titleColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitre()));
        categoryColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCategorie()));
        difficultyColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDifficulte()));
        passingScoreColumn.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getPassingScore() + "%"));
        statusColumn.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().isActif() ? "✅ Actif" : "⏸ Inactif"));
    }

    private void ajouterColonneActions() {
        TableColumn<Quiz, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(320);
        actionsCol.setSortable(false);

        actionsCol.setCellFactory(col -> {
            TableCell<Quiz, Void> cell = new TableCell<Quiz, Void>() {
                final Button btnModifier  = styledBtn("✏ Modifier",   "#4285F4");
                final Button btnQuestions = styledBtn("❓ Questions",  "#6EC5A6");
                final Button btnJouer     = styledBtn("▶ Jouer",      "#F59E0B");
                final Button btnSupprimer = styledBtn("🗑 Suppr.",     "#EF4444");

                {
                    btnModifier.setOnAction(e -> {
                        int idx = getIndex();
                        if (idx >= 0 && idx < quizTable.getItems().size()) {
                            handleModifier(quizTable.getItems().get(idx));
                        }
                    });
                    btnQuestions.setOnAction(e -> {
                        int idx = getIndex();
                        if (idx >= 0 && idx < quizTable.getItems().size()) {
                            handleGererQuestions(quizTable.getItems().get(idx));
                        }
                    });
                    btnJouer.setOnAction(e -> {
                        int idx = getIndex();
                        if (idx >= 0 && idx < quizTable.getItems().size()) {
                            handleJouer(quizTable.getItems().get(idx));
                        }
                    });
                    btnSupprimer.setOnAction(e -> {
                        int idx = getIndex();
                        if (idx >= 0 && idx < quizTable.getItems().size()) {
                            handleSupprimer(quizTable.getItems().get(idx));
                        }
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || getIndex() >= quizTable.getItems().size()) {
                        setGraphic(null);
                    } else {
                        HBox box = new HBox(6, btnModifier, btnQuestions, btnJouer, btnSupprimer);
                        setGraphic(box);
                    }
                }
            };
            return cell;
        });

        quizTable.getColumns().add(actionsCol);
    }

    private Button styledBtn(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; "
                + "-fx-font-size: 11px; -fx-background-radius: 6; -fx-padding: 5 10 5 10; -fx-cursor: hand;");
        return btn;
    }

    // ── Filtres ─────────────────────────────────────────────────

    private void configurerFiltres() {
        categoryFilter.getItems().addAll("Toutes", "Stress", "Anxiété", "Dépression",
                "Bien-être", "Mémoire", "Concentration", "Sommeil", "Relations", "Autre");
        categoryFilter.setValue("Toutes");

        difficultyFilter.getItems().addAll("Toutes", "FACILE", "MOYEN", "DIFFICILE");
        difficultyFilter.setValue("Toutes");

        categoryFilter.setOnAction(e -> appliquerFiltres());
        difficultyFilter.setOnAction(e -> appliquerFiltres());
    }

    @FXML
    private void handleSearch() {
        appliquerFiltres();
    }

    private void appliquerFiltres() {
        if (filteredList == null) return;

        String search = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        String cat    = categoryFilter.getValue();
        String diff   = difficultyFilter.getValue();

        filteredList.setPredicate(quiz -> {
            boolean matchSearch = search.isEmpty()
                    || quiz.getTitre().toLowerCase().contains(search)
                    || (quiz.getCategorie() != null && quiz.getCategorie().toLowerCase().contains(search));

            boolean matchCat = cat == null || cat.equals("Toutes")
                    || cat.equals(quiz.getCategorie());

            boolean matchDiff = diff == null || diff.equals("Toutes")
                    || diff.equals(quiz.getDifficulte());

            return matchSearch && matchCat && matchDiff;
        });

        statusLabel.setText(filteredList.size() + " quiz trouvé(s)");
    }

    // ── Chargement ──────────────────────────────────────────────

    private void chargerQuiz() {
        try {
            List<Quiz> quizList = quizService.getAllQuiz();
            masterList = FXCollections.observableArrayList(quizList);
            filteredList = new FilteredList<>(masterList, q -> true);
            quizTable.setItems(filteredList);
            mettreAJourStats();
            statusLabel.setText(quizList.size() + " quiz chargé(s)");
        } catch (Exception e) {
            AlertHelper.showError("Erreur", "Impossible de charger les quiz : " + e.getMessage());
        }
    }

    private void mettreAJourStats() {
        long actifs   = masterList.stream().filter(Quiz::isActif).count();
        long inactifs = masterList.size() - actifs;
        totalCountLabel.setText(String.valueOf(masterList.size()));
        activeCountLabel.setText(String.valueOf(actifs));
        inactiveCountLabel.setText(String.valueOf(inactifs));
    }

    // ── Actions FXML ────────────────────────────────────────────

    @FXML
    private void handleNewQuiz() {
        NavigationService.getInstance().navigateToQuizForm(quizTable, currentUser, null);
    }

    @FXML
    private void handleRefresh() {
        chargerQuiz();
    }

    @FXML
    private void handleDashboard() {
        if (currentUser != null) {
            NavigationService.getInstance().navigateToDashboard(quizTable, currentUser);
        } else {
            NavigationService.getInstance().navigateToLogin(quizTable);
        }
    }

    @FXML
    private void ouvrirVueUtilisateur() {
        NavigationService.getInstance().navigateToVueUtilisateur(quizTable, currentUser);
    }

    @FXML
    private void handleAiChat() {
        if (currentUser != null) {
            NavigationService.getInstance().navigateToAiChat(quizTable, currentUser);
        } else {
            NavigationService.getInstance().navigateToLogin(quizTable);
        }
    }

    @FXML
    private void handleDashboardCognitif() {
        NavigationService.getInstance().navigateToDashboardCognitif(quizTable);
    }

    // ── Actions sur une ligne ────────────────────────────────────

    private void handleModifier(Quiz quiz) {
        if (quiz == null) return;
        NavigationService.getInstance().navigateToQuizForm(quizTable, currentUser, quiz);
    }

    private void handleGererQuestions(Quiz quiz) {
        if (quiz == null) return;
        NavigationService.getInstance().navigateToQuestionManager(quizTable, currentUser, quiz);
    }

    private void handleJouer(Quiz quiz) {
        if (quiz == null) return;
        NavigationService.getInstance().navigateToQuizPlayer(quizTable, currentUser, quiz);
    }

    private void handleSupprimer(Quiz quiz) {
        if (quiz == null) return;
        boolean confirmed = AlertHelper.showConfirmation("Supprimer le quiz",
                "Êtes-vous sûr de vouloir supprimer le quiz « " + quiz.getTitre() + " » ?\n"
                + "Toutes ses questions seront supprimées.");
        if (!confirmed) return;

        if (quizService.supprimerQuiz(quiz.getId())) {
            masterList.remove(quiz);
            mettreAJourStats();
            AlertHelper.showSuccess("Succès", "Le quiz a été supprimé.");
        } else {
            AlertHelper.showError("Erreur", "Une erreur est survenue. Vérifiez votre connexion.");
        }
    }
}


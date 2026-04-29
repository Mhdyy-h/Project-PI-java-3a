package org.example.controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.model.Aliment;
import org.example.dao.AlimentDAO;
import org.example.service.GeminiService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.util.List;

public class AlimentController {

    // ── TableView ──
    @FXML private TableView<Aliment>            alimentTable;
    @FXML private TableColumn<Aliment, String>  colNom;
    @FXML private TableColumn<Aliment, Integer> colCalories;
    @FXML private TableColumn<Aliment, Double>  colProteines;
    @FXML private TableColumn<Aliment, Double>  colGlucides;
    @FXML private TableColumn<Aliment, Double>  colLipides;
    @FXML private TableColumn<Aliment, Boolean> colExcitant;

    // ── Formulaire ──
    @FXML private TextField nomField;
    @FXML private TextField caloriesField;
    @FXML private TextField proteinesField;
    @FXML private TextField glucidesField;
    @FXML private TextField lipidesField;
    @FXML private TextField indexGlycemiqueField;
    @FXML private CheckBox  excitantCheck;
    @FXML private TextField typeAlimentField;
    @FXML private TextField searchField;

    // ── Chatbot (créés dynamiquement) ──
    private TextField chatInput;
    private Button    chatButton;
    private Label     chatStatus;
    private VBox      chatHistorique;
    private ScrollPane chatScroll;

    private ObservableList<Aliment> aliments = FXCollections.observableArrayList();
    private FilteredList<Aliment>   alimentsFiltres;
    private Runnable onDataChangedCallback;

    // ─────────────────────────────────────────────────
    //  INITIALISATION
    // ─────────────────────────────────────────────────

    @FXML
    public void initialize() {
        colNom.setCellValueFactory(new PropertyValueFactory<>("nomAliment"));
        colCalories.setCellValueFactory(new PropertyValueFactory<>("calories"));
        colProteines.setCellValueFactory(new PropertyValueFactory<>("proteines"));
        colGlucides.setCellValueFactory(new PropertyValueFactory<>("glucides"));
        colLipides.setCellValueFactory(new PropertyValueFactory<>("lipides"));
        colExcitant.setCellValueFactory(new PropertyValueFactory<>("estExcitant"));

        alimentsFiltres = new FilteredList<>(aliments, p -> true);
        SortedList<Aliment> alimentsTries = new SortedList<>(alimentsFiltres);
        alimentsTries.comparatorProperty().bind(alimentTable.comparatorProperty());
        alimentTable.setItems(alimentsTries);

        chargerAliments();

        alimentTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, selected) -> {
                    if (selected != null) afficherAliment(selected);
                });

        searchField.textProperty().addListener((obs, old, val) -> filtrer(val));

        ajouterFiltreNumerique(caloriesField, true);
        ajouterFiltreNumerique(proteinesField, false);
        ajouterFiltreNumerique(glucidesField, false);
        ajouterFiltreNumerique(lipidesField, false);
        ajouterFiltreNumerique(indexGlycemiqueField, true);

        Platform.runLater(this::creerChatbot);
    }

    public void setOnDataChanged(Runnable callback) {
        this.onDataChangedCallback = callback;
    }

    // ─────────────────────────────────────────────────
    //  CRÉATION DU CHATBOT
    // ─────────────────────────────────────────────────

    private void creerChatbot() {
        if (nomField.getScene() == null) return;

        VBox chatBox = new VBox(10);
        chatBox.setPadding(new Insets(15));
        chatBox.setStyle(
                "-fx-background-color: #f8f9ff;"
                        + "-fx-background-radius: 12;"
                        + "-fx-border-color: #4C6FFF;"
                        + "-fx-border-width: 2;"
                        + "-fx-border-radius: 12;");
        chatBox.setPrefWidth(310);
        chatBox.setMinWidth(280);

        // ── Titre ──
        Label titre = new Label("Assistant Nutritionnel IA");
        titre.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        titre.setTextFill(Color.web("#4C6FFF"));

        Label sousTitre = new Label(
                "Entrez un nom d'aliment.\nJe remplirai le formulaire automatiquement.");
        sousTitre.setStyle("-fx-font-size: 11px; -fx-text-fill: #6b7280;");
        sousTitre.setWrapText(true);

        // ── Badge source ──
        Label badge = new Label("Sources : USDA & CIQUAL");
        badge.setStyle(
                "-fx-background-color: #e8f5e9;"
                        + "-fx-text-fill: #27ae60;"
                        + "-fx-font-size: 10px;"
                        + "-fx-padding: 3 8;"
                        + "-fx-background-radius: 10;");

        // ── Historique messages ──
        chatHistorique = new VBox(8);
        chatHistorique.setPadding(new Insets(8));

        chatScroll = new ScrollPane(chatHistorique);
        chatScroll.setFitToWidth(true);
        chatScroll.setPrefHeight(220);
        chatScroll.setStyle(
                "-fx-background-color: white;"
                        + "-fx-background-radius: 8;"
                        + "-fx-border-color: #e5e7eb;"
                        + "-fx-border-radius: 8;");

        ajouterMessageBot(
                "Bonjour ! Entrez le nom d'un aliment\n"
                        + "(ex: poulet, banane, riz, saumon...)\n"
                        + "et je remplirai le formulaire.\n\n"
                        + "Je reponds UNIQUEMENT aux aliments\n"
                        + "et questions nutritionnelles.");

        // ── Zone de saisie ──
        HBox inputBox = new HBox(8);
        inputBox.setAlignment(Pos.CENTER);

        chatInput = new TextField();
        chatInput.setPromptText("Nom de l'aliment...");
        chatInput.setStyle(
                "-fx-background-radius: 20;"
                        + "-fx-border-color: #4C6FFF;"
                        + "-fx-border-radius: 20;"
                        + "-fx-padding: 8 12;"
                        + "-fx-font-size: 12px;");
        HBox.setHgrow(chatInput, Priority.ALWAYS);

        chatButton = new Button("Rechercher");
        chatButton.setStyle(
                "-fx-background-color: #4C6FFF;"
                        + "-fx-text-fill: white;"
                        + "-fx-background-radius: 20;"
                        + "-fx-padding: 8 14;"
                        + "-fx-font-size: 12px;"
                        + "-fx-cursor: hand;");
        chatButton.setOnAction(e -> lancerAnalyseGemini());
        chatInput.setOnAction(e -> lancerAnalyseGemini());

        inputBox.getChildren().addAll(chatInput, chatButton);

        // ── Status ──
        chatStatus = new Label("");
        chatStatus.setStyle("-fx-font-size: 11px; -fx-text-fill: #6b7280;");
        chatStatus.setWrapText(true);

        // ── Bouton vider ──
        Button btnVider = new Button("Vider le formulaire");
        btnVider.setStyle(
                "-fx-background-color: transparent;"
                        + "-fx-text-fill: #e74c3c;"
                        + "-fx-border-color: #e74c3c;"
                        + "-fx-border-radius: 6;"
                        + "-fx-padding: 4 10;"
                        + "-fx-font-size: 11px;"
                        + "-fx-cursor: hand;");
        btnVider.setOnAction(e -> viderFormulaire());

        chatBox.getChildren().addAll(
                titre, sousTitre, badge, chatScroll,
                inputBox, chatStatus, btnVider);

        injecterChatbotDansScene(nomField.getScene().getRoot(), chatBox);
    }

    private void injecterChatbotDansScene(javafx.scene.Parent root, VBox chatBox) {
        if (root instanceof HBox hbox) {
            hbox.getChildren().add(chatBox);
        } else if (root instanceof BorderPane bp) {
            bp.setRight(chatBox);
            BorderPane.setMargin(chatBox, new Insets(10));
        } else if (root instanceof AnchorPane ap) {
            ap.getChildren().add(chatBox);
            AnchorPane.setTopAnchor(chatBox, 10.0);
            AnchorPane.setRightAnchor(chatBox, 10.0);
            AnchorPane.setBottomAnchor(chatBox, 10.0);
        } else if (root instanceof VBox vbox) {
            HBox wrapper = new HBox(15);
            wrapper.setPadding(new Insets(10));
            List<javafx.scene.Node> enfants =
                    new java.util.ArrayList<>(vbox.getChildren());
            vbox.getChildren().clear();
            VBox contenu = new VBox(10);
            contenu.getChildren().addAll(enfants);
            HBox.setHgrow(contenu, Priority.ALWAYS);
            wrapper.getChildren().addAll(contenu, chatBox);
            vbox.getChildren().add(wrapper);
        }
    }

    // ─────────────────────────────────────────────────
    //  ANALYSE GEMINI
    // ─────────────────────────────────────────────────

    private void lancerAnalyseGemini() {
        String query = chatInput.getText() != null ? chatInput.getText().trim() : "";
        if (query.isEmpty()) return;

        ajouterMessageUser(query);
        chatInput.clear();

        // ── Vérification stricte AVANT appel API ──
        if (!GeminiService.estUnAliment(query)) {
            ajouterMessageRefus(query);
            return;
        }

        // ── Chargement ──
        chatButton.setDisable(true);
        chatButton.setText("...");
        chatStatus.setText("Analyse en cours...");
        chatStatus.setStyle("-fx-font-size: 11px; -fx-text-fill: #4C6FFF;");

        Task<Aliment> task = new Task<>() {
            @Override
            protected Aliment call() {
                return GeminiService.obtenirInfoNutritionnelle(query);
            }
        };

        task.setOnSucceeded(e -> {
            Aliment aliment = task.getValue();
            chatButton.setDisable(false);
            chatButton.setText("Rechercher");
            chatStatus.setText("");

            if (aliment == null) {
                // Gemini a confirmé que ce n'est pas un aliment
                ajouterMessageRefus(query);
            } else {
                remplirFormulaireDepuisAliment(aliment);
                ajouterMessageResultat(aliment);
                chatStatus.setText("Formulaire rempli ! Verifiez et cliquez Ajouter.");
                chatStatus.setStyle(
                        "-fx-font-size: 11px; -fx-text-fill: #27ae60; -fx-font-weight: bold;");
            }

            Platform.runLater(() ->
                    chatScroll.setVvalue(chatScroll.getVmax()));
        });

        task.setOnFailed(e -> {
            chatButton.setDisable(false);
            chatButton.setText("Rechercher");
            ajouterMessageBot(
                    "Erreur de connexion a l'API Gemini.\n"
                            + "Verifiez votre connexion internet.");
            chatStatus.setText("");
        });

        new Thread(task).start();
    }

    // ─────────────────────────────────────────────────
    //  MESSAGES CHATBOT
    // ─────────────────────────────────────────────────

    /** Message de refus clair pour les questions hors domaine */
    private void ajouterMessageRefus(String query) {
        String msg = "Desole, je suis un assistant nutritionnel.\n\n"
                + "Je reponds UNIQUEMENT aux questions\n"
                + "sur les aliments et leur composition.\n\n"
                + "\"" + query + "\" ne semble pas etre\n"
                + "un aliment connu.\n\n"
                + "Essayez : pomme, poulet, riz, saumon...";

        Label lbl = new Label(msg);
        lbl.setWrapText(true);
        lbl.setMaxWidth(270);
        lbl.setStyle(
                "-fx-background-color: #fff3cd;"
                        + "-fx-background-radius: 10 10 10 0;"
                        + "-fx-border-color: #f39c12;"
                        + "-fx-border-width: 1;"
                        + "-fx-border-radius: 10 10 10 0;"
                        + "-fx-padding: 10 12;"
                        + "-fx-font-size: 11px;"
                        + "-fx-text-fill: #856404;");

        HBox wrapper = new HBox(lbl);
        wrapper.setAlignment(Pos.CENTER_LEFT);
        Platform.runLater(() -> chatHistorique.getChildren().add(wrapper));
    }

    /** Message de succès avec les valeurs trouvées */
    private void ajouterMessageResultat(Aliment a) {
        String msg = "Valeurs nutritionnelles pour\n100g de "
                + a.getNomAliment() + " :\n\n"
                + "  Calories  : " + a.getCalories() + " kcal\n"
                + "  Proteines : " + a.getProteines() + " g\n"
                + "  Glucides  : " + a.getGlucides() + " g\n"
                + "  Lipides   : " + a.getLipides() + " g\n"
                + "  IG        : " + a.getIndexGlycemique() + "\n"
                + "  Excitant  : " + (a.isEstExcitant() ? "Oui" : "Non") + "\n"
                + "  Type      : " + a.getTypeAliment() + "\n\n"
                + "Formulaire rempli !";

        Label lbl = new Label(msg);
        lbl.setWrapText(true);
        lbl.setMaxWidth(270);
        lbl.setStyle(
                "-fx-background-color: #e8f5e9;"
                        + "-fx-background-radius: 10 10 10 0;"
                        + "-fx-border-color: #27ae60;"
                        + "-fx-border-width: 1;"
                        + "-fx-border-radius: 10 10 10 0;"
                        + "-fx-padding: 10 12;"
                        + "-fx-font-size: 11px;"
                        + "-fx-text-fill: #1a5c2a;");

        HBox wrapper = new HBox(lbl);
        wrapper.setAlignment(Pos.CENTER_LEFT);
        Platform.runLater(() -> chatHistorique.getChildren().add(wrapper));
    }

    private void ajouterMessageBot(String message) {
        Label lbl = new Label(message);
        lbl.setWrapText(true);
        lbl.setMaxWidth(270);
        lbl.setStyle(
                "-fx-background-color: #f0f2ff;"
                        + "-fx-background-radius: 10 10 10 0;"
                        + "-fx-border-color: #4C6FFF;"
                        + "-fx-border-width: 1;"
                        + "-fx-border-radius: 10 10 10 0;"
                        + "-fx-padding: 8 12;"
                        + "-fx-font-size: 11px;"
                        + "-fx-text-fill: #1a1a2e;");

        HBox wrapper = new HBox(lbl);
        wrapper.setAlignment(Pos.CENTER_LEFT);
        Platform.runLater(() -> chatHistorique.getChildren().add(wrapper));
    }

    private void ajouterMessageUser(String message) {
        Label lbl = new Label(message);
        lbl.setWrapText(true);
        lbl.setMaxWidth(200);
        lbl.setStyle(
                "-fx-background-color: #4C6FFF;"
                        + "-fx-background-radius: 10 10 0 10;"
                        + "-fx-padding: 8 12;"
                        + "-fx-font-size: 11px;"
                        + "-fx-text-fill: white;");

        HBox wrapper = new HBox(lbl);
        wrapper.setAlignment(Pos.CENTER_RIGHT);
        Platform.runLater(() -> chatHistorique.getChildren().add(wrapper));
    }

    // ─────────────────────────────────────────────────
    //  REMPLISSAGE FORMULAIRE
    // ─────────────────────────────────────────────────

    private void remplirFormulaireDepuisAliment(Aliment a) {
        // nomAliment = nom officiel retourné par Gemini, PAS la saisie brute
        nomField.setText(a.getNomAliment());
        caloriesField.setText(String.valueOf(a.getCalories()));
        proteinesField.setText(String.valueOf(a.getProteines()));
        glucidesField.setText(String.valueOf(a.getGlucides()));
        lipidesField.setText(String.valueOf(a.getLipides()));
        indexGlycemiqueField.setText(String.valueOf(a.getIndexGlycemique()));
        excitantCheck.setSelected(a.isEstExcitant());
        typeAlimentField.setText(a.getTypeAliment() != null ? a.getTypeAliment() : "");

        // Highlight vert temporaire
        String styleVert = "-fx-border-color: #27ae60; -fx-border-width: 2px;"
                + "-fx-border-radius: 4px;";
        nomField.setStyle(styleVert);
        caloriesField.setStyle(styleVert);
        proteinesField.setStyle(styleVert);
        glucidesField.setStyle(styleVert);
        lipidesField.setStyle(styleVert);

        new Thread(() -> {
            try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
            Platform.runLater(() -> {
                nomField.setStyle(""); caloriesField.setStyle("");
                proteinesField.setStyle(""); glucidesField.setStyle("");
                lipidesField.setStyle("");
            });
        }).start();
    }

    // ─────────────────────────────────────────────────
    //  CRUD
    // ─────────────────────────────────────────────────

    private void chargerAliments() {
        aliments.clear();
        aliments.addAll(AlimentDAO.getAll());
    }

    private void filtrer(String terme) {
        alimentsFiltres.setPredicate(a -> {
            if (terme == null || terme.isEmpty()) return true;
            String t = terme.toLowerCase();
            return a.getNomAliment().toLowerCase().contains(t)
                    || (a.getTypeAliment() != null
                    && a.getTypeAliment().toLowerCase().contains(t));
        });
    }

    private void afficherAliment(Aliment a) {
        nomField.setText(a.getNomAliment());
        caloriesField.setText(String.valueOf(a.getCalories()));
        proteinesField.setText(String.valueOf(a.getProteines()));
        glucidesField.setText(String.valueOf(a.getGlucides()));
        lipidesField.setText(String.valueOf(a.getLipides()));
        indexGlycemiqueField.setText(String.valueOf(a.getIndexGlycemique()));
        excitantCheck.setSelected(a.isEstExcitant());
        typeAlimentField.setText(a.getTypeAliment() != null ? a.getTypeAliment() : "");
    }

    private Aliment getAlimentFromForm() {
        Aliment a = new Aliment();
        a.setNomAliment(nomField.getText() != null ? nomField.getText().trim() : "");
        try { a.setCalories(Integer.parseInt(caloriesField.getText().trim())); }
        catch (Exception e) { a.setCalories(0); }
        try { a.setProteines(Double.parseDouble(
                proteinesField.getText().trim().replace(",", "."))); }
        catch (Exception e) { a.setProteines(0); }
        try { a.setGlucides(Double.parseDouble(
                glucidesField.getText().trim().replace(",", "."))); }
        catch (Exception e) { a.setGlucides(0); }
        try { a.setLipides(Double.parseDouble(
                lipidesField.getText().trim().replace(",", "."))); }
        catch (Exception e) { a.setLipides(0); }
        try { a.setIndexGlycemique(
                Integer.parseInt(indexGlycemiqueField.getText().trim())); }
        catch (Exception e) { a.setIndexGlycemique(0); }
        a.setEstExcitant(excitantCheck.isSelected());
        a.setTypeAliment(typeAlimentField.getText() != null
                ? typeAlimentField.getText().trim() : "");
        return a;
    }

    @FXML
    private void ajouter() {
        Aliment a = getAlimentFromForm();
        if (a.getNomAliment().isEmpty()) {
            showAlertErreur("Champ obligatoire", "Le nom est requis.");
            return;
        }
        if (alimentDejaExistant(a.getNomAliment(), null)) {
            showAlertErreur("Doublon",
                    "L'aliment \"" + a.getNomAliment() + "\" existe deja.");
            return;
        }
        if (AlimentDAO.insert(a)) {
            chargerAliments();
            viderFormulaire();
            if (onDataChangedCallback != null) onDataChangedCallback.run();
            showAlertSucces("Aliment \"" + a.getNomAliment() + "\" ajoute !");
        } else {
            showAlertErreur("Erreur", "Impossible d'ajouter l'aliment.");
        }
    }

    @FXML
    private void modifier() {
        Aliment selected = alimentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlertErreur("Aucune selection",
                    "Selectionnez un aliment dans la liste.");
            return;
        }
        Aliment a = getAlimentFromForm();
        a.setId(selected.getId());
        if (AlimentDAO.update(a)) {
            chargerAliments();
            if (onDataChangedCallback != null) onDataChangedCallback.run();
            showAlertSucces("Aliment modifie !");
        } else {
            showAlertErreur("Erreur", "Impossible de modifier.");
        }
    }

    @FXML
    private void supprimer() {
        Aliment selected = alimentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlertErreur("Aucune selection", "Selectionnez un aliment.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer l'aliment");
        confirm.setContentText("Supprimer \"" + selected.getNomAliment() + "\" ?");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            if (AlimentDAO.delete(selected.getId())) {
                chargerAliments();
                viderFormulaire();
                if (onDataChangedCallback != null) onDataChangedCallback.run();
                showAlertSucces("Aliment supprime !");
            } else {
                showAlertErreur("Erreur",
                        "Impossible de supprimer. Aliment utilise dans des repas.");
            }
        }
    }

    @FXML
    private void viderFormulaire() {
        nomField.clear(); caloriesField.clear();
        proteinesField.clear(); glucidesField.clear();
        lipidesField.clear(); indexGlycemiqueField.clear();
        excitantCheck.setSelected(false); typeAlimentField.clear();
        nomField.setStyle(""); caloriesField.setStyle("");
        proteinesField.setStyle(""); glucidesField.setStyle("");
        lipidesField.setStyle("");
        alimentTable.getSelectionModel().clearSelection();
    }

    @FXML
    private void fermer() {
        ((Stage) nomField.getScene().getWindow()).close();
    }

    // ─────────────────────────────────────────────────
    //  UTILITAIRES
    // ─────────────────────────────────────────────────

    private boolean alimentDejaExistant(String nom, Integer idExistant) {
        for (Aliment a : AlimentDAO.getAll()) {
            if (idExistant != null && a.getId() == idExistant) continue;
            if (a.getNomAliment().equalsIgnoreCase(nom.trim())) return true;
        }
        return false;
    }

    private void ajouterFiltreNumerique(TextField field, boolean entier) {
        field.textProperty().addListener((obs, o, n) -> {
            if (!n.matches(entier ? "\\d*" : "\\d*[.,]?\\d*"))
                field.setText(n.replaceAll(entier ? "[^\\d]" : "[^\\d.,]", ""));
        });
    }

    private void showAlertErreur(String titre, String contenu) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(titre); a.setHeaderText(null);
        a.setContentText(contenu); a.showAndWait();
    }

    private void showAlertSucces(String contenu) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Succes"); a.setHeaderText(null);
        a.setContentText(contenu); a.showAndWait();
    }
}
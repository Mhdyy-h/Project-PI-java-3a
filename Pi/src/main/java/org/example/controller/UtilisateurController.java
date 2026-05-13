package org.example.controller;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.paint.ImagePattern;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.dao.UserDAO;
import org.example.model.User;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.example.service.PdfReportService;
import org.example.service.AvatarService;
import org.example.service.ValidationResult;
import org.example.service.ValidationService;

public class UtilisateurController {

    // ==================== SERVICES ====================
    private final ValidationService validationService = ValidationService.getInstance();
    
    // ==================== LISTE UTILISATEURS ====================
    @FXML private TextField searchField;
    @FXML private VBox usersContainer;
    @FXML private Label statusLabel;
    @FXML private Button sortNomBtn;
    @FXML private Button sortDateBtn;
    @FXML private Button sortScoreBtn;

    // ==================== FORMULAIRE NOUVEL/EDIT USER ====================
    @FXML private Label pageTitleLabel;
    @FXML private Label pageSubtitleLabel;
    @FXML private Label avatarLabel;
    @FXML private Circle avatarCircle;
    @FXML private ImageView avatarImage;
    @FXML private Label userNameDisplay;
    @FXML private Label userIdDisplay;
    @FXML private TextField nomField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisibleField;
    @FXML private Button eyeToggleBtn;
    @FXML private Button suggestPasswordBtn;
    @FXML private VBox passwordStrengthBox;
    @FXML private Region passwordStrengthTrack;
    @FXML private Region passwordStrengthFill;
    @FXML private Label passwordStrengthLabel;
    @FXML private PasswordField confirmPasswordField;
    @FXML private CheckBox roleUtilisateur;
    @FXML private CheckBox roleCoach;
    @FXML private CheckBox roleSpecialiste;
    @FXML private CheckBox roleAdmin;

    // ==================== DONNÉES ====================
    private User currentUser;
    private User targetUser;
    private List<User> allUsers = new ArrayList<>();
    private String currentSort = "nom";
    private String currentView = "list"; // "list", "create", "edit"
    private String chosenPhotoPath = null;  // path chosen by user for profile photo
    private boolean passwordVisible = false;
    private final org.example.service.PasswordStrengthService passwordStrengthService = org.example.service.PasswordStrengthService.getInstance();
    private final org.example.service.ThemeService themeService = org.example.service.ThemeService.getInstance();

    // ==================== NAVIGATION ====================
    @FXML
    public void initialize() {
        // Password strength listener
        setupPasswordStrengthListener();

        // Register scene for theme
        javafx.application.Platform.runLater(() -> {
            if (passwordField != null && passwordField.getScene() != null) {
                themeService.registerScene(passwordField.getScene());
            }
        });
    }

    // ==================== PASSWORD FEATURES ====================
    private void setupPasswordStrengthListener() {
        if (passwordField != null) {
            passwordField.textProperty().addListener((obs, oldVal, newVal) -> updatePasswordStrength(newVal));
        }
        if (passwordVisibleField != null) {
            passwordVisibleField.textProperty().addListener((obs, oldVal, newVal) -> updatePasswordStrength(newVal));
        }
    }

    private void updatePasswordStrength(String password) {
        if (passwordStrengthBox == null) return;

        boolean show = password != null && !password.isEmpty();
        passwordStrengthBox.setVisible(show);
        passwordStrengthBox.setManaged(show);

        if (suggestPasswordBtn != null) {
            suggestPasswordBtn.setVisible(show);
            suggestPasswordBtn.setManaged(show);
        }

        if (!show) return;

        double strength = passwordStrengthService.calculateStrength(password);
        String color = passwordStrengthService.getStrengthColor(strength);
        String label = passwordStrengthService.getStrengthLabel(strength);

        double trackWidth = passwordStrengthTrack != null ? passwordStrengthTrack.getWidth() : 440;
        double fillWidth = trackWidth * strength;
        if (passwordStrengthFill != null) {
            passwordStrengthFill.setPrefWidth(Math.max(0, fillWidth));
            passwordStrengthFill.setStyle("-fx-background-color: " + color + ";");
        }
        if (passwordStrengthLabel != null) {
            passwordStrengthLabel.setText(label);
            passwordStrengthLabel.setStyle("-fx-text-fill: " + color + ";");
        }
    }

    @FXML
    private void handleEyeToggle() {
        passwordVisible = !passwordVisible;
        if (passwordVisible) {
            passwordVisibleField.setText(passwordField.getText());
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            passwordVisibleField.setVisible(true);
            passwordVisibleField.setManaged(true);
            if (eyeToggleBtn != null) eyeToggleBtn.setText("🙈");
        } else {
            passwordField.setText(passwordVisibleField.getText());
            passwordVisibleField.setVisible(false);
            passwordVisibleField.setManaged(false);
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            if (eyeToggleBtn != null) eyeToggleBtn.setText("👁");
        }
    }

    @FXML
    private void handleSuggestPassword() {
        String suggested = passwordStrengthService.generateStrongPassword();
        passwordField.setText(suggested);
        if (passwordVisibleField != null) passwordVisibleField.setText(suggested);
        if (confirmPasswordField != null) confirmPasswordField.setText(suggested);
    }

    private String getPasswordText() {
        if (passwordVisible) {
            return passwordVisibleField != null ? passwordVisibleField.getText() : "";
        }
        return passwordField != null ? passwordField.getText() : "";
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (currentView.equals("list")) {
            loadUsers();
        }
    }

    public void setData(User currentUser, User targetUser) {
        this.currentUser = currentUser;
        this.targetUser = targetUser;
        if (targetUser != null) {
            this.currentView = "edit";
            populateForm();
        }
    }

    public void setMode(String mode) {
        this.currentView = mode;
        if (mode.equals("create")) {
            clearForm();
        }
    }

    // ==================== CRUD: LISTE ====================
    private void loadUsers() {
        try {
            allUsers = UserDAO.getAllUsers();
            renderUsers(allUsers);
            updateStatus(allUsers.size() + " utilisateur(s) trouvé(s)");
        } catch (Exception e) {
            updateStatus("Erreur chargement: " + e.getMessage(), "error");
        }
    }

    private void renderUsers(List<User> users) {
        usersContainer.getChildren().clear();
        for (int i = 0; i < users.size(); i++) {
            User u = users.get(i);
            HBox row = buildUserRow(u, i % 2 == 0);
            themeService.applyToNode(row);
            usersContainer.getChildren().add(row);
        }
    }

    private HBox buildUserRow(User user, boolean alternate) {
        HBox row = new HBox();
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.getStyleClass().addAll("user-row", alternate ? "user-row-alt" : "user-row-default");

        StackPane avatar = buildAvatar(user);

        VBox nameBox = new VBox(2);
        Label nameLbl = new Label(user.getNomComplet());
        nameLbl.getStyleClass().add("user-name");
        Label emailLbl = new Label(user.getEmail());
        emailLbl.getStyleClass().add("user-email");
        nameBox.getChildren().addAll(nameLbl, emailLbl);

        HBox userCol = new HBox(12);
        userCol.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        userCol.setPrefWidth(280);
        userCol.getChildren().addAll(avatar, nameBox);

        String role = extractDisplayRole(user.getRoles());
        Label roleLbl = new Label(role);
        roleLbl.getStyleClass().addAll("role-badge", "role-" + role.toLowerCase().replace("é", "e"));
        roleLbl.setPrefWidth(150);

        String date = user.getDateInscription() != null ? user.getDateInscription() : "20/02/2026";
        Label dateLbl = new Label(date);
        dateLbl.getStyleClass().add("user-date");
        dateLbl.setPrefWidth(180);

        HBox scoreBox = new HBox(4);
        scoreBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        scoreBox.setPrefWidth(160);
        Label starLbl = new Label("☆");
        starLbl.getStyleClass().add("score-star");
        Label scoreLbl = new Label(String.valueOf(user.getScoreGlobal()));
        scoreLbl.getStyleClass().add("score-value");
        scoreBox.getChildren().addAll(starLbl, scoreLbl);

        Button viewBtn = new Button("👁");
        viewBtn.getStyleClass().addAll("action-btn", "action-view");
        viewBtn.setTooltip(new Tooltip("Voir"));
        viewBtn.setOnAction(e -> handleViewUser(user));

        Button editBtn = new Button("✏");
        editBtn.getStyleClass().addAll("action-btn", "action-edit");
        editBtn.setTooltip(new Tooltip("Modifier"));
        editBtn.setOnAction(e -> handleEditUser(user));

        Button deleteBtn = new Button("🗑");
        deleteBtn.getStyleClass().addAll("action-btn", "action-delete");
        deleteBtn.setTooltip(new Tooltip("Supprimer"));
        deleteBtn.setOnAction(e -> handleDeleteUser(user));

        HBox actionsBox = new HBox(4);
        actionsBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        actionsBox.getChildren().addAll(viewBtn, editBtn, deleteBtn);
        HBox.setHgrow(actionsBox, javafx.scene.layout.Priority.ALWAYS);

        row.getChildren().addAll(userCol, roleLbl, dateLbl, scoreBox, actionsBox);
        return row;
    }

    private StackPane buildAvatar(User user) {
        StackPane sp = new StackPane();
        Circle circle = new Circle(20);
        String roleClass = getAvatarRoleClass(user.getRoles());
        circle.getStyleClass().addAll("user-avatar-circle", roleClass);

        String initials = getInitials(user.getNomComplet());
        Label initLbl = new Label(initials);
        initLbl.getStyleClass().add("user-avatar-initials");

        sp.getChildren().addAll(circle, initLbl);
        sp.getStyleClass().add("user-avatar");
        return sp;
    }

    // ==================== CRUD: CREATE ====================
    @FXML
    private void handleCreate() {
        clearFieldErrors();
        
        String nom = nomField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField != null ? confirmPasswordField.getText() : "";
        
        // Validation du nom
        ValidationResult nomValid = validationService.validateName(nom, "Le nom complet");
        if (nomValid.hasError()) {
            showFieldError(nomField, nomValid.getMessage());
            return;
        }
        
        // Validation de l'email
        ValidationResult emailValid = validationService.validateEmail(email);
        if (emailValid.hasError()) {
            showFieldError(emailField, emailValid.getMessage());
            return;
        }
        
        // Validation du mot de passe (obligatoire en création)
        ValidationResult passwordValid = validationService.validatePassword(password, true);
        if (passwordValid.hasError()) {
            showFieldError(passwordField, passwordValid.getMessage());
            return;
        }
        
        // Validation de la confirmation
        if (confirmPasswordField != null) {
            ValidationResult confirmValid = validationService.validatePasswordMatch(password, confirmPassword);
            if (confirmValid.hasError()) {
                showFieldError(confirmPasswordField, confirmValid.getMessage());
                return;
            }
        }

        String roleStr = buildRoleString();
        User newUser = new User(0, nom, email, password);
        newUser.setRoles(roleStr);

        boolean ok = UserDAO.insertUser(newUser);
        if (ok) {
            updateStatus("✓ Utilisateur créé avec succès!", "success");
            PauseTransition pause = new PauseTransition(Duration.seconds(1.2));
            pause.setOnFinished(e -> navigateToList());
            pause.play();
        } else {
            updateStatus("Erreur lors de la création (email déjà utilisé?).", "error");
        }
    }

    // ==================== CRUD: UPDATE ====================
    @FXML
    private void handleSave() {
        clearFieldErrors();
        
        String nom = nomField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField != null ? confirmPasswordField.getText() : "";

        // Validation du nom
        ValidationResult nomValid = validationService.validateName(nom, "Le nom complet");
        if (nomValid.hasError()) {
            showFieldError(nomField, nomValid.getMessage());
            return;
        }
        
        // Validation de l'email
        ValidationResult emailValid = validationService.validateEmail(email);
        if (emailValid.hasError()) {
            showFieldError(emailField, emailValid.getMessage());
            return;
        }
        
        // Validation du mot de passe (optionnel en modification)
        if (!password.isEmpty()) {
            ValidationResult passwordValid = validationService.validatePassword(password, false);
            if (passwordValid.hasError()) {
                showFieldError(passwordField, passwordValid.getMessage());
                return;
            }
            
            // Validation de la confirmation si mot de passe fourni
            if (confirmPasswordField != null) {
                ValidationResult confirmValid = validationService.validatePasswordMatch(password, confirmPassword);
                if (confirmValid.hasError()) {
                    showFieldError(confirmPasswordField, confirmValid.getMessage());
                    return;
                }
            }
        }

        String roleStr = buildRoleString();
        targetUser.setNomComplet(nom);
        targetUser.setEmail(email);
        targetUser.setRoles(roleStr);
        if (!password.isEmpty()) {
            targetUser.setMotDePasse(password);
        }
        if (chosenPhotoPath != null && !chosenPhotoPath.isEmpty()) {
            targetUser.setPhotoProfil(chosenPhotoPath);
        }

        boolean ok = UserDAO.updateUser(targetUser, password.isEmpty());
        if (ok) {
            updateStatus("✓ Modifications enregistrées avec succès!", "success");
            PauseTransition pause = new PauseTransition(Duration.seconds(1.2));
            pause.setOnFinished(e -> navigateToList());
            pause.play();
        } else {
            updateStatus("Erreur lors de la sauvegarde.", "error");
        }
    }

    // ==================== CRUD: DELETE ====================
    private void handleDeleteUser(User user) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Supprimer Utilisateur");
        confirm.setHeaderText("Supprimer " + user.getNomComplet() + "?");
        confirm.setContentText("Cette action est irréversible.");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            boolean ok = UserDAO.deleteUser(user.getId());
            if (ok) {
                allUsers.remove(user);
                renderUsers(allUsers);
                updateStatus("Utilisateur supprimé. " + allUsers.size() + " restant(s).", "success");
            } else {
                updateStatus("Erreur lors de la suppression.", "error");
            }
        }
    }

    // ==================== ACTIONS ====================
    private void handleViewUser(User user) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails Utilisateur");
        alert.setHeaderText(user.getNomComplet());
        alert.setContentText(
                "Email: " + user.getEmail() + "\n" +
                "Rôle: " + extractDisplayRole(user.getRoles()) + "\n" +
                "Score: " + user.getScoreGlobal() + "\n" +
                "Date inscription: " + (user.getDateInscription() != null ? user.getDateInscription() : "N/A")
        );
        alert.showAndWait();
    }

    private void handleEditUser(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/edit_utilisateur.fxml"));
            Parent root = loader.load();
            UtilisateurController ctrl = loader.getController();
            ctrl.setData(currentUser, user);
            Stage stage = (Stage) usersContainer.getScene().getWindow();
            Scene scene = new Scene(root, stage.getScene().getWidth(), stage.getScene().getHeight());
            stage.setScene(scene);
            stage.setTitle("BioSync - Modifier Utilisateur");
        } catch (IOException e) {
            updateStatus("Erreur: " + e.getMessage(), "error");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleNewUser() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/nouvel_utilisateur.fxml"));
            Parent root = loader.load();
            UtilisateurController ctrl = loader.getController();
            ctrl.setCurrentUser(currentUser);
            ctrl.setMode("create");
            Stage stage = (Stage) usersContainer.getScene().getWindow();
            Scene scene = new Scene(root, stage.getScene().getWidth(), stage.getScene().getHeight());
            stage.setScene(scene);
            stage.setTitle("BioSync - Nouvel Utilisateur");
        } catch (IOException e) {
            updateStatus("Erreur: " + e.getMessage(), "error");
            e.printStackTrace();
        }
    }

    // ==================== NAVIGATION ====================
    @FXML
    private void handleRetour() {
        if (currentView.equals("list")) {
            navigateToDashboard();
        } else {
            navigateToList();
        }
    }

    private void navigateToList() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/utilisateurs.fxml"));
            Parent root = loader.load();
            UtilisateursController ctrl = loader.getController();
            ctrl.setCurrentUser(currentUser);
            Stage stage = (Stage) nomField.getScene().getWindow();
            Scene scene = new Scene(root, stage.getScene().getWidth(), stage.getScene().getHeight());
            stage.setScene(scene);
            stage.setTitle("BioSync - Utilisateurs");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void navigateToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/dashboard.fxml"));
            Parent root = loader.load();
            DashboardController ctrl = loader.getController();
            ctrl.setUser(currentUser);
            
            // Get current stage from any available UI element
            Stage stage = null;
            if (searchField != null && searchField.getScene() != null) {
                stage = (Stage) searchField.getScene().getWindow();
            } else if (nomField != null && nomField.getScene() != null) {
                stage = (Stage) nomField.getScene().getWindow();
            } else if (statusLabel != null && statusLabel.getScene() != null) {
                stage = (Stage) statusLabel.getScene().getWindow();
            }
            
            if (stage != null) {
                Scene scene = new Scene(root, stage.getScene().getWidth(), stage.getScene().getHeight());
                stage.setScene(scene);
                stage.setTitle("BioSync - Administration");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ==================== UI HELPERS ====================
    @FXML
    private void handleSearch(KeyEvent event) {
        String query = searchField.getText().toLowerCase().trim();
        List<User> filtered = allUsers.stream()
                .filter(u -> u.getNomComplet().toLowerCase().contains(query)
                        || u.getEmail().toLowerCase().contains(query))
                .collect(Collectors.toList());
        renderUsers(filtered);
        updateStatus(filtered.size() + " résultat(s)");
    }

    @FXML
    private void handleSortNom() {
        currentSort = "nom";
        List<User> sorted = allUsers.stream()
                .sorted(Comparator.comparing(User::getNomComplet, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
        renderUsers(sorted);
        updateSortButtons("nom");
    }

    @FXML
    private void handleSortDate() {
        currentSort = "date";
        List<User> sorted = new ArrayList<>(allUsers);
        sorted.sort(Comparator.comparingInt(User::getId).reversed());
        renderUsers(sorted);
        updateSortButtons("date");
    }

    @FXML
    private void handleSortScore() {
        currentSort = "score";
        List<User> sorted = allUsers.stream()
                .sorted(Comparator.comparingInt(User::getScoreGlobal).reversed())
                .collect(Collectors.toList());
        renderUsers(sorted);
        updateSortButtons("score");
    }

    private void updateSortButtons(String active) {
        sortNomBtn.getStyleClass().removeAll("sort-active", "sort-normal");
        sortDateBtn.getStyleClass().removeAll("sort-active", "sort-normal");
        sortScoreBtn.getStyleClass().removeAll("sort-active", "sort-normal");

        sortNomBtn.getStyleClass().add(active.equals("nom") ? "sort-active" : "sort-normal");
        sortDateBtn.getStyleClass().add(active.equals("date") ? "sort-active" : "sort-normal");
        sortScoreBtn.getStyleClass().add(active.equals("score") ? "sort-active" : "sort-normal");
    }

    @FXML
    private void handleExportPdf() {
        try {
            // Show file chooser dialog
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer le rapport PDF");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf")
            );
            
            // Default filename
            String defaultFileName = "BioSync_Utilisateurs_" + 
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";
            fileChooser.setInitialFileName(defaultFileName);
            
            // Show save dialog
            File selectedFile = fileChooser.showSaveDialog(searchField.getScene().getWindow());
            
            // If user cancelled, don't proceed
            if (selectedFile == null) {
                return;
            }
            
            // Generate PDF at selected location
            PdfReportService pdfService = new PdfReportService();
            pdfService.generateUsersPdf(allUsers, selectedFile.getAbsolutePath());
            
            updateStatus("PDF exporté avec succès: " + selectedFile.getName(), "success");
        } catch (Exception e) {
            updateStatus("Erreur export PDF: " + e.getMessage(), "error");
            e.printStackTrace();
        }
    }

    // ==================== FORM HELPERS ====================
    private void populateForm() {
        if (targetUser == null) return;

        pageSubtitleLabel.setText("Modifier les informations de " + targetUser.getNomComplet());
        userNameDisplay.setText(targetUser.getNomComplet());
        userIdDisplay.setText("ID: #" + targetUser.getId());
        nomField.setText(targetUser.getNomComplet());
        emailField.setText(targetUser.getEmail());

        String initials = getInitials(targetUser.getNomComplet());
        avatarLabel.setText(initials);
        String roleClass = getAvatarRoleClass(targetUser.getRoles());
        avatarCircle.getStyleClass().add(roleClass);

        // Show profile photo if available
        if (avatarImage != null && targetUser.getPhotoProfil() != null && !targetUser.getPhotoProfil().isEmpty()) {
            java.io.File photoFile = new java.io.File(targetUser.getPhotoProfil());
            if (photoFile.exists()) {
                try {
                    Image img = new Image(photoFile.toURI().toString());
                    avatarImage.setImage(img);
                    avatarImage.setVisible(true);
                    avatarLabel.setVisible(false);
                    avatarCircle.setVisible(false);
                } catch (Exception ignored) {}
            }
        }

        String roles = targetUser.getRoles() != null ? targetUser.getRoles() : "";
        roleUtilisateur.setSelected(roles.contains("ROLE_USER") || roles.contains("UTILISATEUR") || (!roles.contains("COACH") && !roles.contains("ADMIN") && !roles.contains("SPECIALISTE")));
        roleCoach.setSelected(roles.contains("COACH"));
        roleSpecialiste.setSelected(roles.contains("SPECIALISTE"));
        roleAdmin.setSelected(roles.contains("ADMIN"));
    }

    private void clearForm() {
        nomField.clear();
        emailField.clear();
        passwordField.clear();
        chosenPhotoPath = null;
        roleUtilisateur.setSelected(true);
        roleCoach.setSelected(false);
        roleSpecialiste.setSelected(false);
        roleAdmin.setSelected(false);
    }

    private String buildRoleString() {
        List<String> roles = new ArrayList<>();
        if (roleAdmin.isSelected()) roles.add("ROLE_ADMIN");
        else if (roleCoach.isSelected()) roles.add("ROLE_COACH");
        else if (roleSpecialiste.isSelected()) roles.add("ROLE_SPECIALISTE");
        else roles.add("ROLE_USER");
        return "[\"" + String.join("\",\"", roles) + "\"]";
    }

    // ==================== UTILITAIRES ====================
    private String getInitials(String name) {
        if (name == null || name.isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
        }
        return name.substring(0, Math.min(2, name.length())).toUpperCase();
    }

    private String getAvatarRoleClass(String roles) {
        if (roles == null) return "avatar-default";
        if (roles.contains("ADMIN")) return "avatar-admin";
        if (roles.contains("COACH")) return "avatar-coach";
        if (roles.contains("SPECIALISTE")) return "avatar-specialiste";
        return "avatar-default";
    }

    private String extractDisplayRole(String roles) {
        if (roles == null) return "UTILISATEUR";
        if (roles.contains("ADMIN")) return "ADMIN";
        if (roles.contains("COACH")) return "COACH";
        if (roles.contains("SPECIALISTE")) return "SPÉCIALISTE";
        return "UTILISATEUR";
    }

    private void updateStatus(String message) {
        if (statusLabel != null) statusLabel.setText(message);
    }

    private void updateStatus(String message, String type) {
        if (statusLabel != null) {
            statusLabel.setText(message);
            statusLabel.getStyleClass().removeAll("status-success", "status-error", "status-warning");
            statusLabel.getStyleClass().add("status-" + type);
        }
    }

    // ==================== PHOTO PROFIL ====================
    @FXML
    private void handleChoosePhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une photo de profil");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );
        Stage stage = (Stage) (nomField != null ? nomField.getScene().getWindow() :
                              (searchField != null ? searchField.getScene().getWindow() : null));
        if (stage == null) return;
        File selected = fileChooser.showOpenDialog(stage);
        if (selected == null) return;

        chosenPhotoPath = selected.getAbsolutePath();

        // Copy to uploads folder
        try {
            java.nio.file.Path uploadsDir = java.nio.file.Paths.get("uploads");
            java.nio.file.Files.createDirectories(uploadsDir);
            String ext = selected.getName().contains(".") ?
                selected.getName().substring(selected.getName().lastIndexOf('.')) : ".jpg";
            String fileName = "avatar_" + System.currentTimeMillis() + ext;
            java.nio.file.Path dest = uploadsDir.resolve(fileName);
            java.nio.file.Files.copy(selected.toPath(), dest,
                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            chosenPhotoPath = dest.toAbsolutePath().toString();
        } catch (Exception e) {
            System.err.println("Photo copy error: " + e.getMessage());
        }

        // Preview in avatar
        showAvatarPreview(chosenPhotoPath);

        // If editing existing user, update database immediately
        if (targetUser != null) {
            targetUser.setPhotoProfil(chosenPhotoPath);
            org.example.dao.UserDAO.updateUserPhoto(targetUser.getId(), chosenPhotoPath);
            notifyUserPhotoUpdated();
        }
    }

    // ==================== AVATAR CUSTOMIZER ====================
    @FXML
    private void handleOpenAvatarCustomizer() {
        try {
            javafx.scene.control.Dialog<ButtonType> dialog = new javafx.scene.control.Dialog<>();
            dialog.setTitle("Personnaliser l'avatar");
            dialog.setHeaderText(null);

            // Load the FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/avatar_customization.fxml"));
            javafx.scene.control.DialogPane dialogPane = loader.load();
            dialog.setDialogPane(dialogPane);

            AvatarCustomizationController controller = loader.getController();
            String seed = targetUser != null ? targetUser.getEmail() : 
                         (emailField != null ? emailField.getText() : "user");
            controller.setSeed(seed);

            // Show dialog and wait for result
            java.util.Optional<ButtonType> result = dialog.showAndWait();

            if (result.isPresent() && result.get().getButtonData() == javafx.scene.control.ButtonBar.ButtonData.OK_DONE) {
                // Get the generated avatar image
                Image avatarImage_generated = controller.getGeneratedAvatar();
                if (avatarImage_generated != null) {
                    // Save the avatar to a file
                    String avatarPath = saveAvatarImage(avatarImage_generated);
                    if (avatarPath != null) {
                        chosenPhotoPath = avatarPath;
                        showAvatarPreview(avatarPath);

                        // If editing existing user, update database immediately
                        if (targetUser != null) {
                            targetUser.setPhotoProfil(avatarPath);
                            org.example.dao.UserDAO.updateUserPhoto(targetUser.getId(), avatarPath);

                                // If editing own profile, update currentUser too
                            if (currentUser != null && targetUser.getId() == currentUser.getId()) {
                                currentUser.setPhotoProfil(avatarPath);
                            }
                            notifyUserPhotoUpdated();
                        }

                        updateStatus("✓ Avatar personnalisé créé et sauvegardé!", "success");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[Avatar] Erreur ouverture customizer: " + e.getMessage());
            e.printStackTrace();
            updateStatus("Erreur lors de l'ouverture du personnalisateur d'avatar.", "error");
        }
    }

    private String saveAvatarImage(Image image) {
        try {
            // Create uploads directory
            java.nio.file.Path uploadsDir = java.nio.file.Paths.get("uploads");
            java.nio.file.Files.createDirectories(uploadsDir);

            // Generate filename
            String fileName = "avatar_custom_" + System.currentTimeMillis() + ".png";
            java.nio.file.Path dest = uploadsDir.resolve(fileName);

            // Convert JavaFX Image to BufferedImage and save
            java.awt.image.BufferedImage bufferedImage = javafx.embed.swing.SwingFXUtils.fromFXImage(image, null);
            javax.imageio.ImageIO.write(bufferedImage, "PNG", dest.toFile());

            return dest.toAbsolutePath().toString();
        } catch (Exception e) {
            System.err.println("[Avatar] Erreur sauvegarde: " + e.getMessage());
            return null;
        }
    }

    private void showAvatarPreview(String path) {
        if (avatarImage != null && path != null) {
            try {
                Image img = new Image(new java.io.File(path).toURI().toString());
                avatarImage.setImage(img);
                avatarImage.setVisible(true);
                if (avatarLabel != null) avatarLabel.setVisible(false);
                if (avatarCircle != null) avatarCircle.setVisible(false);
            } catch (Exception ignored) {}
        }
    }

    /**
     * Notify all controllers that the user photo was updated.
     * Sets a flag on AdminController so it refreshes when navigated back.
     */
    private void notifyUserPhotoUpdated() {
        // Mark that photo was updated so AdminController refreshes on next navigation
        AdminController.setPhotoUpdatedFlag(true);
    }

    // ==================== VALIDATION HELPERS ====================
    private void showFieldError(TextField field, String message) {
        if (field != null) {
            field.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 5;");
            field.setTooltip(new Tooltip(message));
            field.requestFocus();
        }
        updateStatus(message, "error");
    }

    private void clearFieldErrors() {
        clearFieldStyle(nomField);
        clearFieldStyle(emailField);
        clearFieldStyle(passwordField);
        clearFieldStyle(confirmPasswordField);
    }

    private void clearFieldStyle(TextField field) {
        if (field != null) {
            field.setStyle("");
            field.setTooltip(null);
        }
    }
    
    @FXML
    private void handleGenerateAvatar() {
        try {
            AvatarService avatarService = AvatarService.getInstance();
            String email = emailField != null ? emailField.getText() : "";
            
            if (email.isEmpty()) {
                updateStatus("Veuillez entrer un email pour générer un avatar", "error");
                return;
            }
            
            // Generate avatar with random style
            Image avatarImage = avatarService.generateAvatar(email, "lorelei");
            
            if (avatarImage != null) {
                // Display the avatar
                if (avatarCircle != null) {
                    avatarCircle.setFill(new ImagePattern(avatarImage));
                    avatarCircle.setVisible(true);
                }
                if (avatarLabel != null) {
                    avatarLabel.setVisible(false);
                }
                updateStatus("Avatar généré avec succès", "success");
            } else {
                updateStatus("Erreur lors de la génération de l'avatar", "error");
            }
        } catch (Exception e) {
            updateStatus("Erreur: " + e.getMessage(), "error");
        }
    }
}

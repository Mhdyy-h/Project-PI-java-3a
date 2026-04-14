package org.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.dao.CertificationDAO;
import org.example.model.CertificationRequest;
import org.example.model.User;

import java.io.IOException;
import java.util.List;

public class CertificationsAdminController {

    @FXML private Label pendingCountLabel;
    @FXML private Label acceptedCountLabel;
    @FXML private Label refusedCountLabel;
    @FXML private VBox certsContainer;
    @FXML private Label statusLabel;

    private User currentUser;
    private List<CertificationRequest> allRequests;

    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadData();
    }

    private void loadData() {
        allRequests = CertificationDAO.getAllRequests();
        renderStats();
        renderRows();
    }

    private void renderStats() {
        long pending  = allRequests.stream().filter(r -> "EN_ATTENTE".equals(r.getStatut())).count();
        long accepted = allRequests.stream().filter(r -> "ACCEPTE".equals(r.getStatut())).count();
        long refused  = allRequests.stream().filter(r -> "REFUSE".equals(r.getStatut())).count();
        pendingCountLabel.setText(String.valueOf(pending));
        acceptedCountLabel.setText(String.valueOf(accepted));
        refusedCountLabel.setText(String.valueOf(refused));
    }

    private void renderRows() {
        certsContainer.getChildren().clear();
        if (allRequests.isEmpty()) {
            statusLabel.setText("Aucune demande de certification.");
            return;
        }
        statusLabel.setText(allRequests.size() + " demande(s) trouvée(s)");
        for (int i = 0; i < allRequests.size(); i++) {
            certsContainer.getChildren().add(buildRow(allRequests.get(i), i % 2 == 0));
        }
    }

    private HBox buildRow(CertificationRequest req, boolean alternate) {
        HBox row = new HBox();
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 14 24; -fx-border-color: transparent transparent #f0f2f8 transparent; -fx-border-width: 0 0 1 0; "
                + (alternate ? "-fx-background-color: white;" : "-fx-background-color: #fafbfd;"));

        // Demandeur (name + email)
        VBox nameBox = new VBox(2);
        Label nameLbl = new Label(req.getNomComplet() != null ? req.getNomComplet() : "-");
        nameLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1a1a2e;");
        Label emailLbl = new Label(req.getEmail() != null ? req.getEmail() : "-");
        emailLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #9ca3af;");
        nameBox.getChildren().addAll(nameLbl, emailLbl);
        nameBox.setPrefWidth(240);

        // Spécialité badge
        String spec = req.getSpecialite() != null ? req.getSpecialite() : "?";
        Label specLbl = new Label(spec);
        specLbl.setStyle("COACH".equals(spec)
                ? "-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 4 12;"
                : "-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 4 12;");
        specLbl.setPrefWidth(140);

        // Motivation (truncated)
        String motiv = req.getMotivation();
        if (motiv == null || motiv.isBlank()) motiv = "(aucune)";
        if (motiv.length() > 60) motiv = motiv.substring(0, 57) + "...";
        Label motivLbl = new Label(motiv);
        motivLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");
        motivLbl.setWrapText(false);
        HBox.setHgrow(motivLbl, Priority.ALWAYS);
        motivLbl.setPrefWidth(260);

        // Date
        Label dateLbl = new Label(req.getDateEnvoi() != null ? req.getDateEnvoi() : "-");
        dateLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #374151;");
        dateLbl.setPrefWidth(110);

        // Statut badge
        Label statutLbl = buildStatutBadge(req.getStatut());
        statutLbl.setPrefWidth(110);

        // Actions
        HBox actions = new HBox(6);
        actions.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        actions.setPrefWidth(140);

        if (req.getCheminPdf() != null && !req.getCheminPdf().isEmpty()) {
            Button viewPdfBtn = new Button("📄 Voir");
            viewPdfBtn.setStyle("-fx-background-color: #f3f4f6; -fx-text-fill: #374151; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 5 10; -fx-cursor: hand; -fx-border-color: #e5e7eb; -fx-border-radius: 8;");
            viewPdfBtn.setOnAction(e -> {
                try {
                    java.awt.Desktop.getDesktop().open(new java.io.File(req.getCheminPdf()));
                } catch (Exception ex) {
                    System.err.println("Cannot open PDF: " + ex.getMessage());
                }
            });
            actions.getChildren().add(viewPdfBtn);
        }

        boolean isPending = "EN_ATTENTE".equals(req.getStatut());
        if (isPending) {
            Button acceptBtn = new Button("✅ Accepter");
            acceptBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 5 10; -fx-cursor: hand;");
            acceptBtn.setOnAction(e -> handleDecision(req, "ACCEPTE"));

            Button refuseBtn = new Button("❌ Refuser");
            refuseBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 5 10; -fx-cursor: hand;");
            refuseBtn.setOnAction(e -> handleDecision(req, "REFUSE"));

            actions.getChildren().addAll(acceptBtn, refuseBtn);
        } else {
            Label doneLbl = new Label("Traité");
            doneLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #9ca3af;");
            actions.getChildren().add(doneLbl);
        }

        row.getChildren().addAll(nameBox, specLbl, motivLbl, dateLbl, statutLbl, actions);
        return row;
    }

    private Label buildStatutBadge(String statut) {
        Label lbl = new Label(statut != null ? statut : "?");
        String style = "-fx-font-size: 10px; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 4 10;";
        if ("EN_ATTENTE".equals(statut))
            lbl.setStyle(style + " -fx-background-color: #fef3c7; -fx-text-fill: #92400e;");
        else if ("ACCEPTE".equals(statut))
            lbl.setStyle(style + " -fx-background-color: #d1fae5; -fx-text-fill: #065f46;");
        else
            lbl.setStyle(style + " -fx-background-color: #fee2e2; -fx-text-fill: #991b1b;");
        return lbl;
    }

    private void handleDecision(CertificationRequest req, String decision) {
        // Build new role string for user
        String newRole = null;
        if ("ACCEPTE".equals(decision)) {
            newRole = "COACH".equals(req.getSpecialite())
                    ? "[\"ROLE_COACH\"]"
                    : "[\"ROLE_SPECIALISTE\"]";
        }

        boolean ok = CertificationDAO.updateStatut(req, decision, newRole);
        if (ok) {
            req.setStatut(decision);
            // Refresh
            loadData();
            statusLabel.setText("✓ Demande " + (decision.equals("ACCEPTE") ? "acceptée" : "refusée") + " — rôle mis à jour.");
        } else {
            statusLabel.setText("Erreur lors de la mise à jour.");
        }
    }

    @FXML
    private void handleRetour() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/dashboard.fxml"));
            Parent root = loader.load();
            AdminController ctrl = loader.getController();
            ctrl.setUser(currentUser);
            Stage stage = (Stage) certsContainer.getScene().getWindow();
            Scene scene = new Scene(root, stage.getScene().getWidth(), stage.getScene().getHeight());
            stage.setScene(scene);
            stage.setTitle("BioSync - Administration");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

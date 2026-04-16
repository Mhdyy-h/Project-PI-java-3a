package org.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.example.model.CertificationRequest;
import org.example.model.User;
import org.example.service.CertificationService;
import org.example.service.NavigationService;

import java.util.List;

public class CertificationsAdminController {

    @FXML private Label pendingCountLabel;
    @FXML private Label acceptedCountLabel;
    @FXML private Label refusedCountLabel;
    @FXML private VBox certsContainer;
    @FXML private Label statusLabel;

    private final CertificationService certificationService = CertificationService.getInstance();
    private final NavigationService navigationService = NavigationService.getInstance();
    private User currentUser;
    private List<CertificationRequest> allRequests;

    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadData();
    }

    private void loadData() {
        allRequests = certificationService.getAllRequests();
        CertificationService.CertificationStats stats = certificationService.getStats();
        
        pendingCountLabel.setText(String.valueOf(stats.getPending()));
        acceptedCountLabel.setText(String.valueOf(stats.getAccepted()));
        refusedCountLabel.setText(String.valueOf(stats.getRefused()));
        
        renderRows();
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
        row.getStyleClass().addAll("cert-row", alternate ? "cert-row-alt" : "cert-row-default");

        // Demandeur (name + email)
        VBox nameBox = new VBox(2);
        Label nameLbl = new Label(req.getNomComplet() != null ? req.getNomComplet() : "-");
        nameLbl.getStyleClass().add("cert-name");
        Label emailLbl = new Label(req.getEmail() != null ? req.getEmail() : "-");
        emailLbl.getStyleClass().add("cert-email");
        nameBox.getChildren().addAll(nameLbl, emailLbl);
        nameBox.setPrefWidth(240);

        // Spécialité badge
        String spec = req.getSpecialite() != null ? req.getSpecialite() : "?";
        Label specLbl = new Label(spec);
        specLbl.getStyleClass().addAll("spec-badge", "spec-badge-" + spec.toLowerCase());
        specLbl.setPrefWidth(140);

        // Motivation (truncated)
        String motiv = certificationService.extractShortMotivation(req.getMotivation());
        Label motivLbl = new Label(motiv);
        motivLbl.getStyleClass().add("cert-motivation");
        motivLbl.setWrapText(false);
        HBox.setHgrow(motivLbl, Priority.ALWAYS);
        motivLbl.setPrefWidth(260);

        // Date
        Label dateLbl = new Label(req.getDateEnvoi() != null ? req.getDateEnvoi() : "-");
        dateLbl.getStyleClass().add("cert-date");
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
            viewPdfBtn.getStyleClass().addAll("btn-view-pdf");
            viewPdfBtn.setOnAction(e -> openPdf(req.getCheminPdf()));
            actions.getChildren().add(viewPdfBtn);
        }

        boolean isPending = "EN_ATTENTE".equals(req.getStatut());
        if (isPending) {
            Button acceptBtn = new Button("✅ Accepter");
            acceptBtn.getStyleClass().addAll("btn-accept");
            acceptBtn.setOnAction(e -> handleDecision(req, "ACCEPTE"));

            Button refuseBtn = new Button("❌ Refuser");
            refuseBtn.getStyleClass().addAll("btn-refuse");
            refuseBtn.setOnAction(e -> handleDecision(req, "REFUSE"));

            actions.getChildren().addAll(acceptBtn, refuseBtn);
        } else {
            Label doneLbl = new Label("Traité");
            doneLbl.getStyleClass().add("lbl-treated");
            actions.getChildren().add(doneLbl);
        }

        row.getChildren().addAll(nameBox, specLbl, motivLbl, dateLbl, statutLbl, actions);
        return row;
    }

    private Label buildStatutBadge(String statut) {
        Label lbl = new Label(statut != null ? statut : "?");
        lbl.getStyleClass().addAll("statut-badge", "statut-" + (statut != null ? statut.toLowerCase().replace("_", "-") : "unknown"));
        return lbl;
    }

    private void handleDecision(CertificationRequest req, String decision) {
        boolean ok = "ACCEPTE".equals(decision) 
            ? certificationService.approveRequest(req)
            : certificationService.rejectRequest(req);
            
        if (ok) {
            req.setStatut(decision);
            loadData();
            statusLabel.setText("✓ Demande " + (decision.equals("ACCEPTE") ? "acceptée" : "refusée") + " — rôle mis à jour.");
        } else {
            statusLabel.setText("Erreur lors de la mise à jour.");
        }
    }

    private void openPdf(String path) {
        try {
            java.awt.Desktop.getDesktop().open(new java.io.File(path));
        } catch (Exception ex) {
            statusLabel.setText("Impossible d'ouvrir le PDF: " + ex.getMessage());
        }
    }

    @FXML
    private void handleRetour() {
        navigationService.navigateToDashboard(certsContainer, currentUser);
    }
}

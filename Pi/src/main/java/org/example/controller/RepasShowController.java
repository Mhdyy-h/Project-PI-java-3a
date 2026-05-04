package org.example.controller;

import org.example.model.Repas;
import org.example.model.Aliment;
import org.example.model.ChronoScore;
import org.example.service.Chronoscoreservice;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class RepasShowController {

    @FXML private Label titreLabel;
    @FXML private Label momentLabel;
    @FXML private Label dateLabel;
    @FXML private Label scoreTotalLabel;
    @FXML private Label appreciationLabel;
    @FXML private VBox alimentsContainer;
    @FXML private Label timingScoreLabel;
    @FXML private Label nutritionScoreLabel;
    @FXML private Label equilibreScoreLabel;
    @FXML private Label interactionScoreLabel;
    @FXML private Label riskLabel;
    @FXML private Label messageRisqueLabel;

    public void setRepas(Repas repas) {
        ChronoScore score = Chronoscoreservice.calculerChronoScore(repas);

        titreLabel.setText(repas.getTitreRepas());
        momentLabel.setText(repas.getTypeMoment());
        dateLabel.setText(repas.getDateFormatee() + " à " + repas.getHeureFormatee());
        scoreTotalLabel.setText(String.valueOf(score.getTotalScore()));
        scoreTotalLabel.setStyle("-fx-text-fill: " + score.getCouleur() + "; -fx-font-size: 24px; -fx-font-weight: bold;");
        appreciationLabel.setText(score.getAppreciation());
        appreciationLabel.setStyle("-fx-text-fill: " + score.getCouleur() + ";");

        timingScoreLabel.setText(score.getTimingScore() + " pts");
        nutritionScoreLabel.setText(score.getNutritionScore() + " pts");
        equilibreScoreLabel.setText(score.getEquilibreBonus() + " pts");
        interactionScoreLabel.setText(score.getInteractionScore() + " pts");
        riskLabel.setText(score.getRiskPenalty() + " pts");

        if (score.getMessageRisque() != null && !score.getMessageRisque().isEmpty()) {
            messageRisqueLabel.setText(score.getMessageRisque());
            messageRisqueLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        } else {
            messageRisqueLabel.setText("Aucun risque détecté");
            messageRisqueLabel.setStyle("-fx-text-fill: #27ae60;");
        }

        // Afficher les aliments
        alimentsContainer.getChildren().clear();
        for (int i = 0; i < repas.getAliments().size(); i++) {
            Aliment a = repas.getAliments().get(i);
            int q = repas.getQuantites().get(i);
            Label alimentLabel = new Label(String.format("• %s x%d - %d cal", a.getNomAliment(), q, a.getCalories() * q));
            if (a.isEstExcitant()) {
                alimentLabel.setStyle("-fx-text-fill: #e74c3c;");
                alimentLabel.setText(alimentLabel.getText() + " ⚠️ Excitant");
            }
            alimentsContainer.getChildren().add(alimentLabel);
        }

        // Ajouter le total
        Label totalLabel = new Label("Total: " + repas.getTotalCalories() + " calories");
        totalLabel.setStyle("-fx-font-weight: bold; -fx-padding: 10 0 0 0;");
        alimentsContainer.getChildren().add(totalLabel);
    }

    @FXML
    private void fermer() {
        ((Stage) titreLabel.getScene().getWindow()).close();
    }
}
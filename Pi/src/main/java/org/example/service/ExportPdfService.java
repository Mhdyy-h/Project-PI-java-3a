package org.example.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.example.model.Aliment;
import org.example.model.Repas;
import org.example.model.ChronoScore;

import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service d'export PDF utilisant iText 5.
 * Dépendance Maven à ajouter dans pom.xml :
 *
 *   <dependency>
 *       <groupId>com.itextpdf</groupId>
 *       <artifactId>itextpdf</artifactId>
 *       <version>5.5.13.3</version>
 *   </dependency>
 */
public class ExportPdfService {

    // ── Couleurs de la charte graphique ──────────────
    private static final BaseColor COULEUR_TITRE      = new BaseColor(0x4C, 0x6F, 0xFF); // bleu
    private static final BaseColor COULEUR_ENTETE_TAB = new BaseColor(0x4C, 0x6F, 0xFF);
    private static final BaseColor COULEUR_LIGNE_PAIR = new BaseColor(0xF0, 0xF4, 0xFF);
    private static final BaseColor COULEUR_VERT       = new BaseColor(0x27, 0xAE, 0x60);
    private static final BaseColor COULEUR_ORANGE     = new BaseColor(0xF3, 0x9C, 0x12);
    private static final BaseColor COULEUR_ROUGE      = new BaseColor(0xE7, 0x4C, 0x3C);

    // ── Polices ──────────────────────────────────────
    private static final Font FONT_TITRE =
            new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, COULEUR_TITRE);
    private static final Font FONT_SOUS_TITRE =
            new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.DARK_GRAY);
    private static final Font FONT_ENTETE_TAB =
            new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE);
    private static final Font FONT_CELLULE =
            new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, BaseColor.BLACK);
    private static final Font FONT_STATS =
            new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.DARK_GRAY);
    private static final Font FONT_SMALL_GRIS =
            new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC, BaseColor.GRAY);

    // ─────────────────────────────────────────────────
    //  EXPORT 1 : Liste des repas (depuis NutritionController)
    // ─────────────────────────────────────────────────

    /**
     * Exporte la liste des repas affichée dans NutritionController.
     * Inclut : titre, moment, date/heure, calories totales, ChronoScore, aliments.
     *
     * @param repas      liste des repas à exporter (déjà filtrée/triée par le controller)
     * @param cheminFichier chemin complet du fichier PDF de sortie
     * @throws Exception en cas d'erreur iText
     */
    public static void exporterListeRepas(List<Repas> repas, String cheminFichier)
            throws Exception {

        Document document = new Document(PageSize.A4.rotate(), 30, 30, 40, 30);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(cheminFichier));
        writer.setPageEvent(new PiedDePage("Rapport des Repas"));
        document.open();

        // ── En-tête ──
        ajouterEnTete(document, "Rapport des Repas",
                "Généré le " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                repas.size() + " repas exportés");

        if (repas.isEmpty()) {
            document.add(new Paragraph("Aucun repas à afficher.", FONT_STATS));
            document.close();
            return;
        }

        // ── Statistiques globales ──
        int totalCalories  = repas.stream().mapToInt(Repas::getTotalCalories).sum();
        double moyenneScore = repas.stream()
                .mapToInt(r -> Chronoscoreservice.calculerChronoScore(r).getTotalScore())
                .average().orElse(0);
        int meilleurScore  = repas.stream()
                .mapToInt(r -> Chronoscoreservice.calculerChronoScore(r).getTotalScore())
                .max().orElse(0);

        PdfPTable statsTable = new PdfPTable(3);
        statsTable.setWidthPercentage(100);
        statsTable.setSpacingBefore(10);
        statsTable.setSpacingAfter(15);
        ajouterCelluleStat(statsTable, "Total calories", totalCalories + " cal", COULEUR_TITRE);
        ajouterCelluleStat(statsTable, "Score moyen",    String.format("%.1f/14", moyenneScore), COULEUR_VERT);
        ajouterCelluleStat(statsTable, "Meilleur score", meilleurScore + "/14", COULEUR_VERT);
        document.add(statsTable);

        // ── Tableau principal des repas ──
        PdfPTable table = new PdfPTable(new float[]{3f, 2f, 2.5f, 1.5f, 1.5f, 4f});
        table.setWidthPercentage(100);
        table.setSpacingBefore(5);

        String[] colonnes = {"Titre", "Moment", "Date & Heure", "Calories", "Score", "Aliments"};
        for (String col : colonnes) {
            PdfPCell cell = new PdfPCell(new Phrase(col, FONT_ENTETE_TAB));
            cell.setBackgroundColor(COULEUR_ENTETE_TAB);
            cell.setPadding(6);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }

        for (int i = 0; i < repas.size(); i++) {
            Repas r = repas.get(i);
            ChronoScore score = Chronoscoreservice.calculerChronoScore(r);
            BaseColor fond = (i % 2 == 0) ? BaseColor.WHITE : COULEUR_LIGNE_PAIR;

            ajouterCellule(table, r.getTitreRepas(),                          fond, Element.ALIGN_LEFT);
            ajouterCellule(table, r.getTypeMoment(),                          fond, Element.ALIGN_CENTER);
            ajouterCellule(table, r.getDateFormatee() + " " + r.getHeureFormatee(), fond, Element.ALIGN_CENTER);
            ajouterCellule(table, r.getTotalCalories() + " cal",              fond, Element.ALIGN_CENTER);

            // Cellule score colorée
            PdfPCell cellScore = new PdfPCell(new Phrase(score.getTotalScore() + "/14", FONT_CELLULE));
            cellScore.setBackgroundColor(fond);
            cellScore.setPadding(5);
            cellScore.setHorizontalAlignment(Element.ALIGN_CENTER);
            Font scoreFont = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD,
                    couleurScore(score.getTotalScore()));
            cellScore.setPhrase(new Phrase(score.getTotalScore() + "/14", scoreFont));
            table.addCell(cellScore);

            // Aliments
            StringBuilder alims = new StringBuilder();
            for (int j = 0; j < r.getAliments().size(); j++) {
                Aliment a = r.getAliments().get(j);
                alims.append(a.getNomAliment())
                        .append(" x").append(r.getQuantites().get(j));
                if (j < r.getAliments().size() - 1) alims.append(", ");
            }
            ajouterCellule(table, alims.toString(), fond, Element.ALIGN_LEFT);
        }

        document.add(table);
        document.add(new Paragraph(" "));
        document.add(new Paragraph("* Score ChronoNutrition sur 14 points  |  " +
                "Vert >= 10  •  Bleu >= 7  •  Orange >= 4  •  Rouge < 4",
                FONT_SMALL_GRIS));

        document.close();
    }

    // ─────────────────────────────────────────────────
    //  EXPORT 2 : Rapport nutritionnel (depuis NutritionDashboardController)
    // ─────────────────────────────────────────────────

    /**
     * Exporte le rapport nutritionnel complet du dashboard.
     * Inclut : statistiques, détail des scores par repas, analyse tendance.
     *
     * @param repas         liste des repas de la période sélectionnée
     * @param periode       libellé de la période ("Cette semaine", "Ce mois", "Tous")
     * @param tendance      texte d'analyse de tendance calculé par ChronoScoreService
     * @param cheminFichier chemin complet du fichier PDF de sortie
     */
    public static void exporterRapportNutritionnel(List<Repas> repas, String periode,
                                                   String tendance, String cheminFichier)
            throws Exception {

        Document document = new Document(PageSize.A4, 36, 36, 50, 36);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(cheminFichier));
        writer.setPageEvent(new PiedDePage("Rapport Nutritionnel"));
        document.open();

        // ── En-tête ──
        ajouterEnTete(document, "Rapport Nutritionnel ChronoNutrition",
                "Période : " + periode + "  |  Généré le "
                        + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                repas.size() + " repas analysés");

        if (repas.isEmpty()) {
            document.add(new Paragraph("Aucune donnée disponible pour cette période.", FONT_STATS));
            document.close();
            return;
        }

        // ── Bloc statistiques ──
        List<Integer> scores = repas.stream()
                .map(r -> Chronoscoreservice.calculerChronoScore(r).getTotalScore())
                .toList();
        double moyenne   = scores.stream().mapToInt(i -> i).average().orElse(0);
        int meilleur     = scores.stream().max(Integer::compareTo).orElse(0);
        int pire         = scores.stream().min(Integer::compareTo).orElse(0);
        int totalCalJour = repas.stream().mapToInt(Repas::getTotalCalories).sum();
        long nbAlertes   = repas.stream()
                .filter(r -> Chronoscoreservice.calculerChronoScore(r).getRiskPenalty() < 0)
                .count();

        Paragraph titreStat = new Paragraph("Statistiques de la période", FONT_SOUS_TITRE);
        titreStat.setSpacingBefore(10);
        titreStat.setSpacingAfter(6);
        document.add(titreStat);

        PdfPTable statsTable = new PdfPTable(5);
        statsTable.setWidthPercentage(100);
        statsTable.setSpacingAfter(15);
        ajouterCelluleStat(statsTable, "Score moyen",     String.format("%.1f/14", moyenne), COULEUR_TITRE);
        ajouterCelluleStat(statsTable, "Meilleur score",  meilleur + "/14",  COULEUR_VERT);
        ajouterCelluleStat(statsTable, "Score le plus bas", pire + "/14",    COULEUR_ROUGE);
        ajouterCelluleStat(statsTable, "Total calories",  totalCalJour + " cal", COULEUR_ORANGE);
        ajouterCelluleStat(statsTable, "Repas à risque",  nbAlertes + " repas",
                nbAlertes > 0 ? COULEUR_ROUGE : COULEUR_VERT);
        document.add(statsTable);

        // ── Analyse tendance ──
        Paragraph titreTendance = new Paragraph("Analyse de tendance", FONT_SOUS_TITRE);
        titreTendance.setSpacingAfter(6);
        document.add(titreTendance);

        Font fontTendance = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD,
                moyenne >= 10 ? COULEUR_VERT : moyenne >= 7 ? COULEUR_TITRE :
                        moyenne >= 4 ? COULEUR_ORANGE : COULEUR_ROUGE);
        Paragraph pTendance = new Paragraph(tendance, fontTendance);
        pTendance.setSpacingAfter(15);
        document.add(pTendance);

        // ── Tableau détaillé repas ──
        Paragraph titreDet = new Paragraph("Détail des repas", FONT_SOUS_TITRE);
        titreDet.setSpacingAfter(6);
        document.add(titreDet);

        PdfPTable table = new PdfPTable(new float[]{2.5f, 1.8f, 2f, 1.3f, 1.3f, 1.3f, 1.3f, 1.5f});
        table.setWidthPercentage(100);

        String[] colonnes = {"Titre", "Moment", "Date", "Calories", "Score", "Timing", "Nutrition", "Risque"};
        for (String col : colonnes) {
            PdfPCell cell = new PdfPCell(new Phrase(col, FONT_ENTETE_TAB));
            cell.setBackgroundColor(COULEUR_ENTETE_TAB);
            cell.setPadding(5);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }

        for (int i = 0; i < repas.size(); i++) {
            Repas r = repas.get(i);
            ChronoScore sc = Chronoscoreservice.calculerChronoScore(r);
            BaseColor fond = (i % 2 == 0) ? BaseColor.WHITE : COULEUR_LIGNE_PAIR;

            ajouterCellule(table, r.getTitreRepas(),            fond, Element.ALIGN_LEFT);
            ajouterCellule(table, r.getTypeMoment(),            fond, Element.ALIGN_CENTER);
            ajouterCellule(table, r.getDateFormatee() + "\n" + r.getHeureFormatee(), fond, Element.ALIGN_CENTER);
            ajouterCellule(table, r.getTotalCalories() + " cal", fond, Element.ALIGN_CENTER);

            // Score total coloré
            PdfPCell cScore = new PdfPCell();
            cScore.setBackgroundColor(fond);
            cScore.setPadding(5);
            cScore.setHorizontalAlignment(Element.ALIGN_CENTER);
            cScore.addElement(new Phrase(sc.getTotalScore() + "/14",
                    new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, couleurScore(sc.getTotalScore()))));
            table.addCell(cScore);

            ajouterCellule(table, sc.getTimingScore()    + " pts", fond, Element.ALIGN_CENTER);
            ajouterCellule(table, sc.getNutritionScore() + " pts", fond, Element.ALIGN_CENTER);

            // Risque
            String risque = sc.getRiskPenalty() < 0 ? sc.getRiskPenalty() + " pts" : "OK";
            Font fontRisque = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD,
                    sc.getRiskPenalty() < 0 ? COULEUR_ROUGE : COULEUR_VERT);
            PdfPCell cRisque = new PdfPCell(new Phrase(risque, fontRisque));
            cRisque.setBackgroundColor(fond);
            cRisque.setPadding(5);
            cRisque.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cRisque);
        }

        document.add(table);

        // ── Légende ──
        document.add(new Paragraph(" "));
        document.add(new Paragraph(
                "Légende scores : Excellent (>=12)  •  Bon (>=8)  •  Moyen (>=4)  •  A ameliorer (<4)",
                FONT_SMALL_GRIS));

        document.close();
    }

    // ─────────────────────────────────────────────────
    //  HELPERS COMMUNS
    // ─────────────────────────────────────────────────

    private static void ajouterEnTete(Document doc, String titre,
                                      String sousTitre, String info) throws DocumentException {
        Paragraph pTitre = new Paragraph(titre, FONT_TITRE);
        pTitre.setAlignment(Element.ALIGN_CENTER);
        pTitre.setSpacingAfter(4);
        doc.add(pTitre);

        Paragraph pSous = new Paragraph(sousTitre, FONT_STATS);
        pSous.setAlignment(Element.ALIGN_CENTER);
        pSous.setSpacingAfter(2);
        doc.add(pSous);

        Paragraph pInfo = new Paragraph(info, FONT_SMALL_GRIS);
        pInfo.setAlignment(Element.ALIGN_CENTER);
        pInfo.setSpacingAfter(12);
        doc.add(pInfo);

        // Ligne séparatrice
        PdfPTable ligne = new PdfPTable(1);
        ligne.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(COULEUR_TITRE);
        cell.setFixedHeight(2f);
        cell.setBorder(Rectangle.NO_BORDER);
        ligne.addCell(cell);
        ligne.setSpacingAfter(12);
        doc.add(ligne);
    }

    private static void ajouterCelluleStat(PdfPTable table, String label,
                                           String valeur, BaseColor couleurValeur) {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(8);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBorderColor(BaseColor.LIGHT_GRAY);

        Paragraph p = new Paragraph();
        p.add(new Chunk(label + "\n",
                new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL, BaseColor.GRAY)));
        p.add(new Chunk(valeur,
                new Font(Font.FontFamily.HELVETICA, 13, Font.BOLD, couleurValeur)));
        cell.addElement(p);
        table.addCell(cell);
    }

    private static void ajouterCellule(PdfPTable table, String texte,
                                       BaseColor fond, int alignement) {
        PdfPCell cell = new PdfPCell(new Phrase(texte != null ? texte : "", FONT_CELLULE));
        cell.setBackgroundColor(fond);
        cell.setPadding(5);
        cell.setHorizontalAlignment(alignement);
        cell.setBorderColor(BaseColor.LIGHT_GRAY);
        table.addCell(cell);
    }

    private static BaseColor couleurScore(int score) {
        if (score >= 10) return COULEUR_VERT;
        if (score >= 7)  return COULEUR_TITRE;
        if (score >= 4)  return COULEUR_ORANGE;
        return COULEUR_ROUGE;
    }

    // ─────────────────────────────────────────────────
    //  PIED DE PAGE (numérotation automatique)
    // ─────────────────────────────────────────────────

    static class PiedDePage extends PdfPageEventHelper {
        private final String nomApp;
        PiedDePage(String nomApp) { this.nomApp = nomApp; }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            Font f = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL, BaseColor.GRAY);
            Phrase pied = new Phrase(
                    nomApp + "  |  ChronoNutrition  |  Page "
                            + writer.getPageNumber(), f);
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, pied,
                    (document.left() + document.right()) / 2,
                    document.bottom() - 10, 0);
        }
    }
}
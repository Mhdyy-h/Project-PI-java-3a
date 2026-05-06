package org.example.service;

import org.example.model.FatigueLog;
import org.example.model.ProfilCognitif;
import org.example.model.QuestionAdaptive;
import org.example.model.QuizSession;
import org.example.model.ReponseUtilisateur;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.example.util.ConfigLoader;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Génération de rapports PDF d'évaluation cognitive avec Apache PDFBox 3.x.
 *
 * <h3>Structure du rapport</h3>
 * <ol>
 *   <li>Page de garde — identité, score, ELO, date</li>
 *   <li>Résumé — statistiques globales de la session</li>
 *   <li>Détail question par question — tableau avec réponse, correction, temps</li>
 *   <li>Profil de fatigue — progression textuelle du score de fatigue</li>
 *   <li>Recommandations — issues de l'AIAssistantService ou génériques</li>
 * </ol>
 *
 * <p>Exemple d'utilisation :</p>
 * <pre>
 *   ReportService svc = new ReportService();
 *   String chemin = svc.generer(session, profil, reponses, questionsMap, fatigues, recommendations);
 *   Desktop.getDesktop().open(new File(chemin));
 * </pre>
 */
public class ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportService.class);

    // ── Dimensions page A4 en points (1 pt = 1/72 inch) ─────────
    private static final float PAGE_W     = PDRectangle.A4.getWidth();   // 595.28
    private static final float PAGE_H     = PDRectangle.A4.getHeight();  // 841.89
    private static final float MARGIN     = 50f;
    private static final float CONTENT_W  = PAGE_W - 2 * MARGIN;

    // ── Couleurs (RGB normalisées 0-1) ───────────────────────────
    private static final float[] COLOR_BLEU     = {0.14f, 0.35f, 0.70f};
    private static final float[] COLOR_VERT     = {0.10f, 0.60f, 0.30f};
    private static final float[] COLOR_ROUGE    = {0.80f, 0.15f, 0.15f};
    private static final float[] COLOR_GRIS     = {0.50f, 0.50f, 0.50f};
    private static final float[] COLOR_FOND     = {0.95f, 0.95f, 0.97f};

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final PDFont fontNormal;
    private final PDFont fontBold;
    private final PDFont fontMono;

    private PDDocument doc;
    private PDPageContentStream cs;
    private float cursorY;

    public ReportService() {
        // Les polices sont créées par document — on stocke juste les noms ici
        this.fontNormal = null; // initialisé dans generer()
        this.fontBold   = null;
        this.fontMono   = null;
    }

    // ════════════════════════════════════════════════════════════════
    // Point d'entrée principal
    // ════════════════════════════════════════════════════════════════

    /**
     * Génère le rapport PDF et retourne le chemin absolu du fichier.
     *
     * @param session        session de quiz terminée
     * @param profil         profil cognitif de l'utilisateur
     * @param reponses       liste des réponses de la session
     * @param questionsMap   map questionId → QuestionAdaptive (pour afficher l'énoncé)
     * @param fatigueLogs    logs de fatigue de la session
     * @param recommandations recommandations textuelles (peut être vide)
     * @return chemin absolu du fichier PDF généré
     */
    public String generer(QuizSession session,
                          ProfilCognitif profil,
                          List<ReponseUtilisateur> reponses,
                          Map<Integer, QuestionAdaptive> questionsMap,
                          List<FatigueLog> fatigueLogs,
                          List<String> recommandations) throws IOException {

        String outputDir = ConfigLoader.reportOutputDir();
        new File(outputDir).mkdirs();
        String nomFichier = String.format("%s/rapport_session_%d_%s.pdf",
                outputDir, session.getId(),
                session.getDateDebut().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")));

        doc = new PDDocument();

        // Polices Standard14 (incluses dans PDFBox, aucune installation requise)
        PDFont bold   = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        PDFont normal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        PDFont mono   = new PDType1Font(Standard14Fonts.FontName.COURIER);

        // ── Page 1 : garde + résumé ──────────────────────────────
        PDPage page1 = new PDPage(PDRectangle.A4);
        doc.addPage(page1);
        cs = new PDPageContentStream(doc, page1);
        cursorY = PAGE_H - MARGIN;

        dessinerEntete(bold, normal, session);
        dessinerResume(bold, normal, session, profil, reponses);
        cs.close();

        // ── Page 2 : détail questions ────────────────────────────
        PDPage page2 = new PDPage(PDRectangle.A4);
        doc.addPage(page2);
        cs = new PDPageContentStream(doc, page2);
        cursorY = PAGE_H - MARGIN;

        dessinerTitrePage(bold, "Détail des réponses");
        dessinerTableauReponses(bold, normal, mono, reponses, questionsMap);
        cs.close();

        // ── Page 3 : fatigue + recommandations ───────────────────
        PDPage page3 = new PDPage(PDRectangle.A4);
        doc.addPage(page3);
        cs = new PDPageContentStream(doc, page3);
        cursorY = PAGE_H - MARGIN;

        dessinerTitrePage(bold, "Profil de fatigue & Recommandations");
        if (!fatigueLogs.isEmpty()) {
            dessinerFatigue(bold, normal, fatigueLogs);
        }
        if (!recommandations.isEmpty()) {
            dessinerRecommandations(bold, normal, recommandations);
        }
        dessinerPiedDePage(normal, session);
        cs.close();

        doc.save(nomFichier);
        doc.close();

        log.info("Rapport généré : {}", nomFichier);
        return nomFichier;
    }

    // ════════════════════════════════════════════════════════════════
    // Sections du rapport
    // ════════════════════════════════════════════════════════════════

    private void dessinerEntete(PDFont bold, PDFont normal,
                                 QuizSession session) throws IOException {
        // Bandeau titre
        cs.setNonStrokingColor(COLOR_BLEU[0], COLOR_BLEU[1], COLOR_BLEU[2]);
        cs.addRect(MARGIN, cursorY - 60, CONTENT_W, 60);
        cs.fill();

        cs.setNonStrokingColor(1f, 1f, 1f); // blanc
        ecrireTexte(bold, 18, MARGIN + 10, cursorY - 22, "Rapport d'Évaluation Cognitive");
        ecrireTexte(normal, 11, MARGIN + 10, cursorY - 42, "BioSync — Plateforme d'Apprentissage Adaptatif");
        cursorY -= 75;

        // Ligne d'info
        cs.setNonStrokingColor(0f, 0f, 0f);
        ecrireTexte(bold, 11, MARGIN, cursorY,
                "Session n°" + session.getId() + "  |  " + session.getTheme());
        cursorY -= 14;
        ecrireTexte(normal, 10, MARGIN, cursorY,
                "Date : " + (session.getDateDebut() != null ? session.getDateDebut().format(DTF) : "—"));
        cursorY -= 25;

        ligneHorizontale(COLOR_GRIS);
        cursorY -= 10;
    }

    private void dessinerResume(PDFont bold, PDFont normal,
                                 QuizSession session, ProfilCognitif profil,
                                 List<ReponseUtilisateur> reponses) throws IOException {
        ecrireTexte(bold, 13, MARGIN, cursorY, "Résumé de la session");
        cursorY -= 20;

        long correctes = reponses.stream().filter(ReponseUtilisateur::isEstCorrecte).count();
        int tempsMoy   = (int) reponses.stream()
                .mapToInt(ReponseUtilisateur::getTempsReponseMs).average().orElse(0);

        String[][] stats = {
            {"Score final",       String.format("%.1f%%", session.getScoreFinal())},
            {"Questions",         correctes + " / " + reponses.size() + " correctes"},
            {"Cote ELO",          String.valueOf(session.getScoreElo())},
            {"Niveau",            profil.getNiveauActuel()},
            {"Fatigue moyenne",   String.format("%.0f / 100", session.getScoreFatigue())},
            {"Temps/réponse moy", tempsMoy + " ms"},
            {"Durée totale",      formatDuree(session.getDureeSecondes())},
        };

        for (String[] stat : stats) {
            dessinerLigneStat(bold, normal, stat[0], stat[1],
                    stat[0].startsWith("Score") ? (session.getScoreFinal() >= 60 ? COLOR_VERT : COLOR_ROUGE) : COLOR_BLEU);
            cursorY -= 18;
        }
    }

    private void dessinerTableauReponses(PDFont bold, PDFont normal, PDFont mono,
                                          List<ReponseUtilisateur> reponses,
                                          Map<Integer, QuestionAdaptive> questionsMap) throws IOException {
        // En-têtes du tableau
        cs.setNonStrokingColor(COLOR_FOND[0], COLOR_FOND[1], COLOR_FOND[2]);
        cs.addRect(MARGIN, cursorY - 18, CONTENT_W, 18);
        cs.fill();
        cs.setNonStrokingColor(0f, 0f, 0f);

        ecrireTexte(bold, 9, MARGIN + 3,   cursorY - 12, "#");
        ecrireTexte(bold, 9, MARGIN + 20,  cursorY - 12, "QuestionAdaptive (résumé)");
        ecrireTexte(bold, 9, MARGIN + 290, cursorY - 12, "Réponse");
        ecrireTexte(bold, 9, MARGIN + 390, cursorY - 12, "Temps");
        ecrireTexte(bold, 9, MARGIN + 450, cursorY - 12, "Résultat");
        cursorY -= 20;

        int ligne = 0;
        for (ReponseUtilisateur r : reponses) {
            if (cursorY < MARGIN + 30) {
                ajouterPageSiNecessaire(doc, bold);
            }

            QuestionAdaptive q = questionsMap.get(r.getQuestionId());
            String enonce = q != null ? tronquer(q.getEnonce(), 55) : "—";

            if (ligne % 2 == 0) {
                cs.setNonStrokingColor(0.97f, 0.97f, 0.97f);
                cs.addRect(MARGIN, cursorY - 13, CONTENT_W, 14);
                cs.fill();
            }
            cs.setNonStrokingColor(0f, 0f, 0f);

            ecrireTexte(mono, 8, MARGIN + 3,   cursorY - 10, String.valueOf(r.getOrdreSession()));
            ecrireTexte(normal, 8, MARGIN + 20,  cursorY - 10, enonce);
            ecrireTexte(normal, 8, MARGIN + 290, cursorY - 10, tronquer(r.getReponseDonnee(), 30));
            ecrireTexte(mono,   8, MARGIN + 390, cursorY - 10, r.getTempsReponseMs() + " ms");

            float[] couleurRes = r.isEstCorrecte() ? COLOR_VERT : COLOR_ROUGE;
            cs.setNonStrokingColor(couleurRes[0], couleurRes[1], couleurRes[2]);
            ecrireTexte(bold, 9, MARGIN + 450, cursorY - 10, r.isEstCorrecte() ? "✓" : "✗");
            cs.setNonStrokingColor(0f, 0f, 0f);

            cursorY -= 15;
            ligne++;
        }
    }

    private void dessinerFatigue(PDFont bold, PDFont normal,
                                  List<FatigueLog> logs) throws IOException {
        ecrireTexte(bold, 12, MARGIN, cursorY, "Évolution de la fatigue");
        cursorY -= 18;

        ecrireTexte(normal, 9, MARGIN, cursorY,
                "Q# — [██] Score  |  Suggestion");
        cursorY -= 14;
        ligneHorizontale(COLOR_GRIS);
        cursorY -= 8;

        for (FatigueLog fl : logs) {
            if (cursorY < MARGIN + 30) break;
            String barre = barreTexte(fl.getScoreFatigue(), 20);
            float[] couleur = fl.getScoreFatigue() >= 85 ? COLOR_ROUGE
                    : fl.getScoreFatigue() >= 65 ? new float[]{0.9f, 0.5f, 0f}
                    : COLOR_VERT;
            cs.setNonStrokingColor(couleur[0], couleur[1], couleur[2]);
            ecrireTexte(normal, 9, MARGIN, cursorY,
                    String.format("Q%-3d [%s] %5.1f%%  %s",
                            fl.getNumeroQuestion(), barre,
                            fl.getScoreFatigue(), fl.getSuggestion()));
            cs.setNonStrokingColor(0f, 0f, 0f);
            cursorY -= 13;
        }
        cursorY -= 10;
    }

    private void dessinerRecommandations(PDFont bold, PDFont normal,
                                          List<String> recs) throws IOException {
        ligneHorizontale(COLOR_GRIS);
        cursorY -= 12;
        ecrireTexte(bold, 12, MARGIN, cursorY, "Recommandations");
        cursorY -= 18;

        for (int i = 0; i < recs.size() && i < 5; i++) {
            if (cursorY < MARGIN + 20) break;
            ecrireTexteMultiLigne(normal, 10, MARGIN + 10, CONTENT_W - 20,
                    (i + 1) + ".  " + recs.get(i));
        }
    }

    private void dessinerPiedDePage(PDFont normal, QuizSession session) throws IOException {
        cs.setNonStrokingColor(COLOR_GRIS[0], COLOR_GRIS[1], COLOR_GRIS[2]);
        float yPied = MARGIN + 15;
        ecrireTexte(normal, 8, MARGIN, yPied,
                "Généré par BioSync — Session " + session.getId()
                        + " — " + java.time.LocalDate.now());
    }

    // ════════════════════════════════════════════════════════════════
    // Primitives de dessin
    // ════════════════════════════════════════════════════════════════

    private void dessinerTitrePage(PDFont bold, String titre) throws IOException {
        cs.setNonStrokingColor(COLOR_BLEU[0], COLOR_BLEU[1], COLOR_BLEU[2]);
        ecrireTexte(bold, 14, MARGIN, cursorY, titre);
        cs.setNonStrokingColor(0f, 0f, 0f);
        cursorY -= 8;
        ligneHorizontale(COLOR_BLEU);
        cursorY -= 15;
    }

    private void dessinerLigneStat(PDFont bold, PDFont normal,
                                    String label, String valeur, float[] couleur) throws IOException {
        ecrireTexte(bold,   10, MARGIN,       cursorY, label + " :");
        cs.setNonStrokingColor(couleur[0], couleur[1], couleur[2]);
        ecrireTexte(normal, 10, MARGIN + 170, cursorY, valeur);
        cs.setNonStrokingColor(0f, 0f, 0f);
    }

    private void ligneHorizontale(float[] couleur) throws IOException {
        cs.setStrokingColor(couleur[0], couleur[1], couleur[2]);
        cs.moveTo(MARGIN, cursorY);
        cs.lineTo(MARGIN + CONTENT_W, cursorY);
        cs.stroke();
    }

    private void ecrireTexte(PDFont font, float taille, float x, float y, String texte) throws IOException {
        if (texte == null || texte.isBlank()) return;
        cs.beginText();
        cs.setFont(font, taille);
        cs.newLineAtOffset(x, y);
        cs.showText(nettoyer(texte));
        cs.endText();
    }

    private void ecrireTexteMultiLigne(PDFont font, float taille, float x, float largeur,
                                        String texte) throws IOException {
        if (texte == null || texte.isBlank()) return;
        // Découpe naïve en lignes de largeur maximale
        String[] mots = texte.split(" ");
        StringBuilder ligne = new StringBuilder();
        for (String mot : mots) {
            if (calculerLargeur(font, taille, ligne + mot + " ") > largeur) {
                ecrireTexte(font, taille, x, cursorY, ligne.toString().trim());
                cursorY -= (taille + 3);
                ligne = new StringBuilder();
            }
            ligne.append(mot).append(" ");
        }
        if (!ligne.isEmpty()) {
            ecrireTexte(font, taille, x, cursorY, ligne.toString().trim());
            cursorY -= (taille + 3);
        }
        cursorY -= 5;
    }

    private float calculerLargeur(PDFont font, float taille, String texte) {
        try {
            return font.getStringWidth(nettoyer(texte)) / 1000f * taille;
        } catch (IOException e) {
            return texte.length() * taille * 0.5f;
        }
    }

    private void ajouterPageSiNecessaire(PDDocument document, PDFont bold) throws IOException {
        cs.close();
        PDPage newPage = new PDPage(PDRectangle.A4);
        document.addPage(newPage);
        cs = new PDPageContentStream(document, newPage);
        cursorY = PAGE_H - MARGIN;
    }

    // ════════════════════════════════════════════════════════════════
    // Utilitaires
    // ════════════════════════════════════════════════════════════════

    /** Barre ASCII proportionnelle au score (0-100) sur N caractères. */
    private String barreTexte(double score, int longueur) {
        int pleins = (int) Math.round(score / 100.0 * longueur);
        return "█".repeat(Math.max(0, pleins)) + "░".repeat(Math.max(0, longueur - pleins));
    }

    private String tronquer(String texte, int max) {
        if (texte == null) return "—";
        return texte.length() <= max ? texte : texte.substring(0, max - 1) + "…";
    }

    private String formatDuree(int secondes) {
        if (secondes <= 0) return "—";
        int min = secondes / 60;
        int sec = secondes % 60;
        return min > 0 ? min + " min " + sec + " s" : sec + " s";
    }

    /** Nettoie les caractères non supportés par les polices Standard14. */
    private String nettoyer(String texte) {
        if (texte == null) return "";
        return texte
                .replace("✓", "OK").replace("✗", "X")
                .replace("█", "#").replace("░", ".")
                .replace("…", "...")
                .replaceAll("[^ -ÿ]", "?");
    }
}

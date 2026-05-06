package org.example.service;

import org.example.model.User;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import java.awt.Color;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PdfReportService {

    private static final Color PRIMARY_COLOR = new Color(76, 111, 255);
    private static final Color SECONDARY_COLOR = new Color(108, 117, 125);
    private static final Color WHITE = new Color(255, 255, 255);
    private static final Color LIGHT_GRAY = new Color(240, 242, 248);
    private static final Color ALT_ROW = new Color(250, 251, 253);

    public void generateUsersPdf(List<User> users, String filePath) throws Exception {
        Document document = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(document, new FileOutputStream(filePath));
        document.open();

        addHeader(document);
        addStats(document, users.size());
        addUsersTable(document, users);
        addFooter(document);

        document.close();
    }

    private String generateFilePath() {
        String userHome = System.getProperty("user.home");
        String desktopPath = userHome + File.separator + "Desktop";
        
        // Create Desktop directory if it doesn't exist
        File desktopDir = new File(desktopPath);
        if (!desktopDir.exists()) {
            desktopDir.mkdirs();
        }
        
        String fileName = "BioSync_Utilisateurs_" + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";
        return desktopPath + File.separator + fileName;
    }

    private void addHeader(Document document) throws DocumentException {
        Font titleFont = new Font(Font.HELVETICA, 24, Font.BOLD, PRIMARY_COLOR);
        Paragraph title = new Paragraph("Rapport des Utilisateurs - BioSync", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(10);
        document.add(title);

        Font subtitleFont = new Font(Font.HELVETICA, 12, Font.NORMAL, SECONDARY_COLOR);
        Paragraph subtitle = new Paragraph("Généré le: " + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")), subtitleFont);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(20);
        document.add(subtitle);
    }

    private void addStats(Document document, int totalUsers) throws DocumentException {
        Font statsFont = new Font(Font.HELVETICA, 11, Font.BOLD, SECONDARY_COLOR);
        Paragraph stats = new Paragraph("Total utilisateurs: " + totalUsers, statsFont);
        stats.setSpacingAfter(15);
        document.add(stats);
    }

    private void addUsersTable(Document document, List<User> users) throws DocumentException {
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        table.setWidths(new float[]{3f, 3f, 2f, 2f, 2f});

        addTableHeaders(table);
        addTableRows(table, users);

        document.add(table);
    }

    private void addTableHeaders(PdfPTable table) {
        String[] headers = {"NOM COMPLET", "EMAIL", "RÔLE", "DATE INSCRIPTION", "SCORE"};
        Font headerFont = new Font(Font.HELVETICA, 11, Font.BOLD, WHITE);

        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(PRIMARY_COLOR);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPadding(10);
            cell.setBorderColor(WHITE);
            cell.setBorderWidth(1);
            table.addCell(cell);
        }
    }

    private void addTableRows(PdfPTable table, List<User> users) {
        Font dataFont = new Font(Font.HELVETICA, 10, Font.NORMAL);
        Font dataFontBold = new Font(Font.HELVETICA, 10, Font.BOLD);

        boolean alternate = false;
        for (User user : users) {
            Color bgColor = alternate ? ALT_ROW : WHITE;
            alternate = !alternate;

            // Name
            PdfPCell nameCell = new PdfPCell(new Phrase(user.getNomComplet(), dataFontBold));
            nameCell.setBackgroundColor(bgColor);
            nameCell.setPadding(8);
            nameCell.setBorderColor(LIGHT_GRAY);
            table.addCell(nameCell);

            // Email
            PdfPCell emailCell = new PdfPCell(new Phrase(user.getEmail(), dataFont));
            emailCell.setBackgroundColor(bgColor);
            emailCell.setPadding(8);
            emailCell.setBorderColor(LIGHT_GRAY);
            table.addCell(emailCell);

            // Role
            String role = extractRole(user.getRoles());
            Color roleColor = getRoleColor(user.getRoles());
            Font roleFont = new Font(Font.HELVETICA, 9, Font.BOLD, roleColor);
            PdfPCell roleCell = new PdfPCell(new Phrase(role, roleFont));
            roleCell.setBackgroundColor(bgColor);
            roleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            roleCell.setPadding(8);
            roleCell.setBorderColor(LIGHT_GRAY);
            table.addCell(roleCell);

            // Date
            String date = user.getDateInscription() != null ? user.getDateInscription() : "N/A";
            PdfPCell dateCell = new PdfPCell(new Phrase(date, dataFont));
            dateCell.setBackgroundColor(bgColor);
            dateCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            dateCell.setPadding(8);
            dateCell.setBorderColor(LIGHT_GRAY);
            table.addCell(dateCell);

            // Score
            PdfPCell scoreCell = new PdfPCell(new Phrase(String.valueOf(user.getScoreGlobal()), dataFont));
            scoreCell.setBackgroundColor(bgColor);
            scoreCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            scoreCell.setPadding(8);
            scoreCell.setBorderColor(LIGHT_GRAY);
            table.addCell(scoreCell);
        }
    }

    private void addFooter(Document document) throws DocumentException {
        Font footerFont = new Font(Font.HELVETICA, 9, Font.ITALIC, SECONDARY_COLOR);
        Paragraph footer = new Paragraph("© BioSync - Rapport confidentiel", footerFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(30);
        document.add(footer);
    }

    private String extractRole(String roles) {
        if (roles == null) return "UTILISATEUR";
        if (roles.contains("ADMIN")) return "ADMIN";
        if (roles.contains("COACH")) return "COACH";
        if (roles.contains("SPECIALISTE")) return "SPÉCIALISTE";
        return "UTILISATEUR";
    }

    private Color getRoleColor(String roles) {
        if (roles == null) return SECONDARY_COLOR;
        if (roles.contains("ADMIN")) return new Color(239, 68, 68);
        if (roles.contains("COACH")) return new Color(16, 185, 129);
        if (roles.contains("SPECIALISTE")) return new Color(245, 158, 11);
        return new Color(124, 58, 237);
    }
}


package org.example.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Validations de saisie pour les formulaires Quiz / Question.
 */
public final class InputValidator {

    private InputValidator() {}

    /**
     * Valide les champs du formulaire Quiz.
     *
     * @return liste des messages d'erreur (vide = tout est valide)
     */
    public static List<String> validateQuiz(String titre, String categorie, String difficulte, String passingScoreStr) {
        List<String> errors = new ArrayList<>();

        if (titre == null || titre.trim().length() < 3)
            errors.add("Le titre doit contenir au moins 3 caractères.");

        if (categorie == null || categorie.trim().isEmpty())
            errors.add("La catégorie est obligatoire.");

        if (difficulte == null || difficulte.trim().isEmpty())
            errors.add("La difficulté doit être sélectionnée.");

        if (passingScoreStr != null && !passingScoreStr.trim().isEmpty()) {
            try {
                int score = Integer.parseInt(passingScoreStr.trim());
                if (score < 0 || score > 100)
                    errors.add("Le score de passage doit être entre 0 et 100.");
            } catch (NumberFormatException e) {
                errors.add("Le score de passage doit être un nombre entier.");
            }
        }

        return errors;
    }

    /**
     * Valide les champs du formulaire Question.
     *
     * @return liste des messages d'erreur (vide = tout est valide)
     */
    public static List<String> validateQuestion(String contenu, String optionA, String optionB,
                                                String bonneReponse, String pointsStr) {
        List<String> errors = new ArrayList<>();

        if (contenu == null || contenu.trim().isEmpty())
            errors.add("Le contenu de la question est obligatoire.");

        if (optionA == null || optionA.trim().isEmpty())
            errors.add("L'option A est obligatoire.");

        if (optionB == null || optionB.trim().isEmpty())
            errors.add("L'option B est obligatoire.");

        if (bonneReponse == null || bonneReponse.trim().isEmpty())
            errors.add("La bonne réponse est obligatoire.");

        if (pointsStr == null || pointsStr.trim().isEmpty()) {
            errors.add("Les points sont obligatoires.");
        } else {
            try {
                int pts = Integer.parseInt(pointsStr.trim());
                if (pts < 1) errors.add("Les points doivent être >= 1.");
            } catch (NumberFormatException e) {
                errors.add("Les points doivent être un nombre entier.");
            }
        }

        return errors;
    }
}

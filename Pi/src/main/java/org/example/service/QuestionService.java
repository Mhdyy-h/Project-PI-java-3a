package org.example.service;

import org.example.dao.QuestionDAO;
import org.example.model.Question;

import java.util.List;

/**
 * Service CRUD pour l'entite Question.
 * Utilise QuestionDAO pour l'acces aux donnees.
 */
public class QuestionService {

    // ── CREATE ─────────────────────────────────────────────────

    public boolean ajouterQuestion(Question question) {
        return QuestionDAO.insertQuestion(question);
    }

    // ── READ BY QUIZ ────────────────────────────────────────────

    public List<Question> getQuestionsByQuizId(int quizId) {
        return QuestionDAO.getQuestionsByQuizId(quizId);
    }

    // ── READ ALL ────────────────────────────────────────────────

    public List<Question> getAllQuestions() {
        return QuestionDAO.getAllQuestions();
    }

    // ── UPDATE ─────────────────────────────────────────────────

    public boolean modifierQuestion(Question question) {
        return QuestionDAO.updateQuestion(question);
    }

    // ── DELETE ─────────────────────────────────────────────────

    public boolean supprimerQuestion(int id) {
        return QuestionDAO.deleteQuestion(id);
    }

    // ── COUNT BY QUIZ ───────────────────────────────────────────

    public int countQuestionsByQuiz(int quizId) {
        return QuestionDAO.countQuestionsByQuiz(quizId);
    }

    // ── SUM POINTS BY QUIZ ──────────────────────────────────────

    public int sumPointsByQuiz(int quizId) {
        return QuestionDAO.sumPointsByQuiz(quizId);
    }
}

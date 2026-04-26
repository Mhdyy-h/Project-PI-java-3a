package org.example.service;

import org.example.dao.QuizDAO;
import org.example.model.Quiz;

import java.util.List;

/**
 * Service CRUD pour l'entite Quiz.
 * Utilise QuizDAO pour l'acces aux donnees.
 */
public class QuizService {

    // ── CREATE ─────────────────────────────────────────────────

    public boolean ajouterQuiz(Quiz quiz) {
        return QuizDAO.insertQuiz(quiz);
    }

    // ── READ ALL ───────────────────────────────────────────────

    public List<Quiz> getAllQuiz() {
        return QuizDAO.getAllQuizzes();
    }

    // ── READ ACTIFS (statut = 'disponible') ────────────────────

    public List<Quiz> getQuizActifs() {
        return QuizDAO.getActiveQuizzes();
    }

    // ── READ BY ID ─────────────────────────────────────────────

    public Quiz getQuizById(int id) {
        return QuizDAO.getQuizById(id);
    }

    // ── UPDATE ─────────────────────────────────────────────────

    public boolean modifierQuiz(Quiz quiz) {
        return QuizDAO.updateQuiz(quiz);
    }

    // ── DELETE ─────────────────────────────────────────────────

    public boolean supprimerQuiz(int id) {
        return QuizDAO.deleteQuiz(id);
    }

    // ── SEARCH ─────────────────────────────────────────────────

    public List<Quiz> rechercherQuiz(String keyword) {
        return QuizDAO.searchQuizzes(keyword);
    }
}

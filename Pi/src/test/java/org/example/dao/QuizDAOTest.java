package org.example.dao;

import org.example.DatabaseConnection;
import org.example.model.Quiz;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for QuizDAO
 * Tests CRUD operations with H2 in-memory database
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class QuizDAOTest {
    
    private Connection testConnection;
    
    @BeforeAll
    void setUpDatabase() throws SQLException {
        // Create H2 in-memory database
        testConnection = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "sa", "");
        
        // Create test tables
        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS utilisateur (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "nom_complet VARCHAR(50), " +
                    "email VARCHAR(100), " +
                    "mot_de_passe VARCHAR(100), " +
                    "roles VARCHAR(100))");
            
            stmt.execute("CREATE TABLE IF NOT EXISTS quiz_mental (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "titre VARCHAR(255) NOT NULL, " +
                    "niveau_stress_cible INT DEFAULT 0, " +
                    "score_resultat INT DEFAULT 0, " +
                    "medaille_quiz VARCHAR(50), " +
                    "date_quiz TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "utilisateur_id INT NOT NULL, " +
                    "statut VARCHAR(50) DEFAULT 'disponible', " +
                    "temps_moyen_reponse DOUBLE, " +
                    "agilite_cognitive TEXT, " +
                    "categorie VARCHAR(100))");
        }
        
        // Mock DatabaseConnection
        DatabaseConnection.setTestConnection(testConnection);
    }
    
    @AfterAll
    void tearDownDatabase() throws SQLException {
        if (testConnection != null && !testConnection.isClosed()) {
            testConnection.close();
        }
    }
    
    @BeforeEach
    void setUp() throws SQLException {
        // Clean table before each test
        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute("DELETE FROM quiz_mental");
            stmt.execute("ALTER TABLE quiz_mental ALTER COLUMN id RESTART WITH 1");
        }
    }
    
    @Test
    @DisplayName("Insert Quiz - Should create new quiz successfully")
    void testInsertQuiz() {
        // Arrange
        Quiz quiz = new Quiz();
        quiz.setTitre("Stress Management Quiz");
        quiz.setNiveauStressCible(5);
        quiz.setScoreResultat(80);
        quiz.setMedailleQuiz("Gold");
        quiz.setUtilisateurId(1);
        quiz.setStatut("disponible");
        quiz.setAgiliteCognitive("Test description");
        quiz.setCategorie("Stress");
        
        // Act
        boolean result = QuizDAO.insertQuiz(quiz);
        
        // Assert
        assertTrue(result, "Quiz should be inserted successfully");
        assertTrue(quiz.getId() > 0, "Quiz ID should be generated");
    }
    
    @Test
    @DisplayName("Get All Quizzes - Should return all quizzes")
    void testGetAllQuizzes() {
        // Arrange
        createTestQuizzes(3);
        
        // Act
        List<Quiz> quizzes = QuizDAO.getAllQuizzes();
        
        // Assert
        assertEquals(3, quizzes.size(), "Should return 3 quizzes");
    }
    
    @Test
    @DisplayName("Get Quiz By ID - Should return correct quiz")
    void testGetQuizById() {
        // Arrange
        Quiz quiz = createSingleTestQuiz("Test Quiz", 1);
        
        // Act
        Quiz found = QuizDAO.getQuizById(quiz.getId());
        
        // Assert
        assertNotNull(found, "Quiz should be found");
        assertEquals(quiz.getTitre(), found.getTitre(), "Title should match");
    }
    
    @Test
    @DisplayName("Get Quiz By ID - Should return null for non-existent ID")
    void testGetQuizByIdNotFound() {
        // Act
        Quiz found = QuizDAO.getQuizById(9999);
        
        // Assert
        assertNull(found, "Should return null for non-existent quiz");
    }
    
    @Test
    @DisplayName("Update Quiz - Should update existing quiz")
    void testUpdateQuiz() {
        // Arrange
        Quiz quiz = createSingleTestQuiz("Original Title", 1);
        quiz.setTitre("Updated Title");
        quiz.setNiveauStressCible(8);
        
        // Act
        boolean result = QuizDAO.updateQuiz(quiz);
        
        // Assert
        assertTrue(result, "Quiz should be updated");
        
        Quiz updated = QuizDAO.getQuizById(quiz.getId());
        assertEquals("Updated Title", updated.getTitre(), "Title should be updated");
        assertEquals(8, updated.getNiveauStressCible(), "Stress level should be updated");
    }
    
    @Test
    @DisplayName("Delete Quiz - Should remove quiz")
    void testDeleteQuiz() {
        // Arrange
        Quiz quiz = createSingleTestQuiz("To Delete", 1);
        
        // Act
        boolean result = QuizDAO.deleteQuiz(quiz.getId());
        
        // Assert
        assertTrue(result, "Quiz should be deleted");
        assertNull(QuizDAO.getQuizById(quiz.getId()), "Quiz should not exist after deletion");
    }
    
    @Test
    @DisplayName("Get Active Quizzes - Should return only active quizzes")
    void testGetActiveQuizzes() {
        // Arrange
        Quiz active1 = createSingleTestQuiz("Active 1", 1);
        Quiz active2 = createSingleTestQuiz("Active 2", 1);
        Quiz inactive = createSingleTestQuiz("Inactive", 1);
        inactive.setStatut("complete");
        QuizDAO.updateQuiz(inactive);
        
        // Act
        List<Quiz> activeQuizzes = QuizDAO.getActiveQuizzes();
        
        // Assert
        assertEquals(2, activeQuizzes.size(), "Should return only active quizzes");
    }
    
    @Test
    @DisplayName("Search Quizzes - Should find matching quizzes")
    void testSearchQuizzes() {
        // Arrange
        createSingleTestQuiz("Stress Management", 1);
        createSingleTestQuiz("Anxiety Quiz", 1);
        createSingleTestQuiz("Depression Test", 1);
        
        // Act
        List<Quiz> results = QuizDAO.searchQuizzes("Stress");
        
        // Assert
        assertEquals(1, results.size(), "Should find 1 matching quiz");
        assertTrue(results.get(0).getTitre().contains("Stress"), "Should contain search term");
    }
    
    // Helper methods
    private void createTestQuizzes(int count) {
        for (int i = 1; i <= count; i++) {
            createSingleTestQuiz("Quiz " + i, i);
        }
    }
    
    private Quiz createSingleTestQuiz(String title, int userId) {
        Quiz quiz = new Quiz();
        quiz.setTitre(title);
        quiz.setNiveauStressCible(5);
        quiz.setScoreResultat(70);
        quiz.setUtilisateurId(userId);
        quiz.setStatut("disponible");
        QuizDAO.insertQuiz(quiz);
        return quiz;
    }
}

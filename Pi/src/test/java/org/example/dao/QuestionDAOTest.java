package org.example.dao;

import org.example.DatabaseConnection;
import org.example.model.Question;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for QuestionDAO
 * Tests CRUD operations with H2 in-memory database
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class QuestionDAOTest {
    
    private Connection testConnection;
    
    @BeforeAll
    void setUpDatabase() throws SQLException {
        // Create H2 in-memory database
        testConnection = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "sa", "");
        
        // Create test tables
        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS quiz_mental (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "titre VARCHAR(255) NOT NULL, " +
                    "utilisateur_id INT NOT NULL)");
            
            stmt.execute("CREATE TABLE IF NOT EXISTS question (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "quiz_id INT NOT NULL, " +
                    "enonce TEXT NOT NULL, " +
                    "reponse_correcte VARCHAR(255) NOT NULL, " +
                    "options_fausses TEXT, " +
                    "points_valeur INT DEFAULT 50, " +
                    "FOREIGN KEY (quiz_id) REFERENCES quiz_mental(id) ON DELETE CASCADE)");
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
        // Clean tables before each test
        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute("DELETE FROM question");
            stmt.execute("ALTER TABLE question ALTER COLUMN id RESTART WITH 1");
        }
    }
    
    @Test
    @DisplayName("Insert Question - Should create new question successfully")
    void testInsertQuestion() {
        // Arrange
        Question question = new Question();
        question.setQuizId(1);
        question.setEnonce("What is stress?");
        question.setReponseCorrecte("A state of mental tension");
        question.setOptionsFausses("Option A|Option B|Option C");
        question.setPointsValeur(100);
        
        // Act
        boolean result = QuestionDAO.insertQuestion(question);
        
        // Assert
        assertTrue(result, "Question should be inserted successfully");
        assertTrue(question.getId() > 0, "Question ID should be generated");
    }
    
    @Test
    @DisplayName("Get Questions By Quiz ID - Should return questions for specific quiz")
    void testGetQuestionsByQuizId() {
        // Arrange
        createTestQuestions(1, 3); // 3 questions for quiz 1
        createTestQuestions(2, 2); // 2 questions for quiz 2
        
        // Act
        List<Question> questions = QuestionDAO.getQuestionsByQuizId(1);
        
        // Assert
        assertEquals(3, questions.size(), "Should return 3 questions for quiz 1");
        assertTrue(questions.stream().allMatch(q -> q.getQuizId() == 1), 
                "All questions should belong to quiz 1");
    }
    
    @Test
    @DisplayName("Get All Questions - Should return all questions")
    void testGetAllQuestions() {
        // Arrange
        createTestQuestions(1, 5);
        
        // Act
        List<Question> questions = QuestionDAO.getAllQuestions();
        
        // Assert
        assertEquals(5, questions.size(), "Should return all 5 questions");
    }
    
    @Test
    @DisplayName("Get Question By ID - Should return correct question")
    void testGetQuestionById() {
        // Arrange
        Question question = createSingleTestQuestion(1, "Test Question");
        
        // Act
        Question found = QuestionDAO.getQuestionById(question.getId());
        
        // Assert
        assertNotNull(found, "Question should be found");
        assertEquals(question.getEnonce(), found.getEnonce(), "Question text should match");
    }
    
    @Test
    @DisplayName("Update Question - Should update existing question")
    void testUpdateQuestion() {
        // Arrange
        Question question = createSingleTestQuestion(1, "Original Question");
        question.setEnonce("Updated Question");
        question.setPointsValeur(200);
        
        // Act
        boolean result = QuestionDAO.updateQuestion(question);
        
        // Assert
        assertTrue(result, "Question should be updated");
        
        Question updated = QuestionDAO.getQuestionById(question.getId());
        assertEquals("Updated Question", updated.getEnonce(), "Question text should be updated");
        assertEquals(200, updated.getPointsValeur(), "Points should be updated");
    }
    
    @Test
    @DisplayName("Delete Question - Should remove question")
    void testDeleteQuestion() {
        // Arrange
        Question question = createSingleTestQuestion(1, "To Delete");
        
        // Act
        boolean result = QuestionDAO.deleteQuestion(question.getId());
        
        // Assert
        assertTrue(result, "Question should be deleted");
        assertNull(QuestionDAO.getQuestionById(question.getId()), 
                "Question should not exist after deletion");
    }
    
    @Test
    @DisplayName("Count Questions By Quiz - Should return correct count")
    void testCountQuestionsByQuiz() {
        // Arrange
        createTestQuestions(1, 5); // 5 questions for quiz 1
        createTestQuestions(2, 3); // 3 questions for quiz 2
        
        // Act
        int count = QuestionDAO.countQuestionsByQuiz(1);
        
        // Assert
        assertEquals(5, count, "Should count 5 questions for quiz 1");
    }
    
    @Test
    @DisplayName("Sum Points By Quiz - Should return correct total")
    void testSumPointsByQuiz() {
        // Arrange
        Question q1 = createSingleTestQuestion(1, "Q1");
        q1.setPointsValeur(50);
        QuestionDAO.updateQuestion(q1);
        
        Question q2 = createSingleTestQuestion(1, "Q2");
        q2.setPointsValeur(100);
        QuestionDAO.updateQuestion(q2);
        
        // Act
        int total = QuestionDAO.sumPointsByQuiz(1);
        
        // Assert
        assertEquals(150, total, "Should sum points to 150");
    }
    
    @Test
    @DisplayName("Delete Questions By Quiz - Should remove all questions for a quiz")
    void testDeleteQuestionsByQuiz() {
        // Arrange
        createTestQuestions(1, 3); // 3 questions for quiz 1
        createTestQuestions(2, 2); // 2 questions for quiz 2
        
        // Act
        boolean result = QuestionDAO.deleteQuestionsByQuiz(1);
        
        // Assert
        assertTrue(result, "Questions should be deleted");
        assertEquals(0, QuestionDAO.countQuestionsByQuiz(1), 
                "Quiz 1 should have no questions");
        assertEquals(2, QuestionDAO.countQuestionsByQuiz(2), 
                "Quiz 2 should still have 2 questions");
    }
    
    // Helper methods
    private void createTestQuestions(int quizId, int count) {
        for (int i = 1; i <= count; i++) {
            createSingleTestQuestion(quizId, "Question " + i);
        }
    }
    
    private Question createSingleTestQuestion(int quizId, String enonce) {
        Question question = new Question();
        question.setQuizId(quizId);
        question.setEnonce(enonce);
        question.setReponseCorrecte("Correct Answer");
        question.setOptionsFausses("Wrong 1|Wrong 2|Wrong 3");
        question.setPointsValeur(50);
        QuestionDAO.insertQuestion(question);
        return question;
    }
}

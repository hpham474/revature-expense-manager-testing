package com.revature.repository_tests;

import com.revature.repository.DatabaseConnection;
import com.revature.repository.Expense;
import com.revature.repository.ExpenseRepository;
import com.revature.repository.ExpenseWithUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TestExpenseRepository {
    @Mock
    private DatabaseConnection db;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @InjectMocks
    private ExpenseRepository expenseRepo;

    @BeforeEach
    public void setUp() throws Exception {
        when(db.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
    }

    /****************************************************************************************************
     * FIND BY ID TESTS                                                                                 *
     ****************************************************************************************************/

    @Test
    @DisplayName("Test findById throws RuntimeException")
    public void testFindById_databaseException() throws Exception {
        int expenseId = 1;

        // Arrange
        when(preparedStatement.executeQuery()).thenThrow(new SQLException("DB failure"));

        // Act + Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> expenseRepo.findById(expenseId)
        );

        assertTrue(exception.getMessage().contains("Error finding expense by ID: " + expenseId));
        assertNotNull(exception.getCause());
        assertInstanceOf(SQLException.class, exception.getCause());
    }

    @Test
    @DisplayName("Test findById returns empty result")
    public void testFindById_emptyResult() throws SQLException {
        //Arrange
        int expenseId = 999;
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false); // No rows found

        // Act
        Optional<Expense> result = expenseRepo.findById(expenseId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Test findById Positive")
    public void testFindById_Positive() throws SQLException {
        //Arrange
        int expenseId = 1;

        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);

        // Act
        Optional<Expense> result = expenseRepo.findById(expenseId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isPresent());
    }

    /****************************************************************************************************
     * FIND PENDING EXPENSES WITH USERS TESTS                                                                                 *
     ****************************************************************************************************/

    @Test
    @DisplayName("Test findPendingExpensesWithUsers throws RuntimeException")
    public void testFindPendingExpensesWithUsers_throwsException() throws SQLException {
        // Arrange
        when(preparedStatement.executeQuery()).thenThrow(new SQLException("DB failure"));

        // Act + Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> expenseRepo.findPendingExpensesWithUsers()
        );

        assertTrue(exception.getMessage().contains("Error finding pending expenses"));
        assertNotNull(exception.getCause());
        assertInstanceOf(SQLException.class, exception.getCause());
    }

    @Test
    @DisplayName("Test findPendingExpensesWithUsers returns Empty List")
    public void testFindPendingExpensesWithUsers_emptyList() throws SQLException {
        //Arrange
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false); // No rows found

        // Act
        List<ExpenseWithUser> result = expenseRepo.findPendingExpensesWithUsers();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Test findPendingExpensesWithUsers Positive")
    public void testFindPendingExpensesWithUsers_Positive() throws SQLException {
        //Arrange
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);

        // Act
        List<ExpenseWithUser> result = expenseRepo.findPendingExpensesWithUsers();

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    /****************************************************************************************************
     * FIND EXPENSES BY USER TESTS                                                                                 *
     ****************************************************************************************************/
    @Test
    @DisplayName("Test findExpensesByUser Throws Exception")
    public void testFindExpensesByUser_Exception() throws SQLException {
        // Arrange
        int userId = 1;
        when(preparedStatement.executeQuery()).thenThrow(new SQLException("DB failure"));

        // Act + Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> expenseRepo.findExpensesByUser(userId)
        );

        assertTrue(exception.getMessage().contains("Error finding expenses for user: " + userId));
        assertNotNull(exception.getCause());
        assertInstanceOf(SQLException.class, exception.getCause());
    }

    @Test
    @DisplayName("Test findExpensesByUser returns Empty List")
    public void testFindExpensesByUser_emptyList() throws SQLException {
        //Arrange
        int userId = 1;
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false); // No rows found

        // Act
        List<ExpenseWithUser> result = expenseRepo.findExpensesByUser(userId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Test findExpensesByUser Positive")
    public void testFindExpensesByUser_positive(){

    }

}

package com.codealpha.student;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StudentService Unit Tests")
public class StudentServiceTest {

    private StudentService service;

    @BeforeEach
    void setUp() {
        service = new StudentService();
    }

    @Test
    @DisplayName("Should add valid student successfully")
    void testAddValidStudent() {
        Student student = new Student("STU-001", "John Doe", "john.doe@example.com", "Computer Science", 3.75);
        Student result = service.addStudent(student);

        assertNotNull(result);
        assertEquals("STU-001", result.getId());
        assertEquals(1, service.getStudentCount());

        Optional<Student> fetched = service.getStudentById("STU-001");
        assertTrue(fetched.isPresent());
        assertEquals("John Doe", fetched.get().getName());
    }

    @Test
    @DisplayName("Should throw exception when adding duplicate student ID")
    void testDuplicateStudentIdThrowsException() {
        Student s1 = new Student("STU-001", "John Doe", "john.doe@example.com", "Computer Science", 3.75);
        Student s2 = new Student("STU-001", "Jane Doe", "jane.doe@example.com", "Software Engineering", 3.90);

        service.addStudent(s1);

        StudentValidationException exception = assertThrows(StudentValidationException.class, () -> {
            service.addStudent(s2);
        });

        assertTrue(exception.getMessage().contains("already exists"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid-email", "test@", "@domain.com", "plainaddress", "user@site"})
    @DisplayName("Should throw exception when student email format is invalid")
    void testInvalidEmailFormats(String invalidEmail) {
        Student student = new Student("STU-002", "Invalid Email", invalidEmail, "IT", 3.2);

        StudentValidationException ex = assertThrows(StudentValidationException.class, () -> {
            service.addStudent(student);
        });

        assertTrue(ex.getMessage().contains("Invalid email format"));
    }

    @Test
    @DisplayName("Should throw exception when GPA is out of range")
    void testGpaOutOfRangeThrowsException() {
        Student negativeGpa = new Student("STU-003", "Test Neg", "test@test.com", "Math", -0.5);
        assertThrows(StudentValidationException.class, () -> service.addStudent(negativeGpa));

        Student excessiveGpa = new Student("STU-004", "Test High", "test@test.com", "Math", 4.01);
        assertThrows(StudentValidationException.class, () -> service.addStudent(excessiveGpa));
    }

    @Test
    @DisplayName("Should throw exception when student name or ID is blank")
    void testBlankFieldsThrowException() {
        Student blankId = new Student("", "Valid Name", "valid@domain.com", "Physics", 3.0);
        assertThrows(StudentValidationException.class, () -> service.addStudent(blankId));

        Student blankName = new Student("STU-005", "  ", "valid@domain.com", "Physics", 3.0);
        assertThrows(StudentValidationException.class, () -> service.addStudent(blankName));
    }

    @Test
    @DisplayName("Should calculate average GPA correctly")
    void testCalculateAverageGpa() {
        assertEquals(0.0, service.calculateAverageGpa());

        service.addStudent(new Student("STU-01", "S1", "s1@domain.com", "CS", 3.0));
        service.addStudent(new Student("STU-02", "S2", "s2@domain.com", "CS", 4.0));

        assertEquals(3.5, service.calculateAverageGpa(), 0.001);
    }

    @Test
    @DisplayName("Should delete student by ID")
    void testDeleteStudent() {
        Student s = new Student("STU-01", "S1", "s1@domain.com", "CS", 3.0);
        service.addStudent(s);

        assertTrue(service.deleteStudent("STU-01"));
        assertEquals(0, service.getStudentCount());
        assertFalse(service.deleteStudent("STU-NONEXISTENT"));
    }

    @Test
    @DisplayName("Should update existing student successfully")
    void testUpdateStudent() {
        Student original = new Student("STU-01", "Original Name", "orig@domain.com", "CS", 3.2);
        service.addStudent(original);

        Student updated = new Student("STU-01", "Updated Name", "updated@domain.com", "Data Science", 3.8);
        service.updateStudent(updated);

        Optional<Student> fetched = service.getStudentById("STU-01");
        assertTrue(fetched.isPresent());
        assertEquals("Updated Name", fetched.get().getName());
        assertEquals("Data Science", fetched.get().getDepartment());
        assertEquals(3.8, fetched.get().getGpa(), 0.001);
    }
}

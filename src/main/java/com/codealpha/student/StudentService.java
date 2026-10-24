package com.codealpha.student;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Service class managing student records and business validations.
 */
public class StudentService {

    private final Map<String, Student> studentStore = new LinkedHashMap<>();

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    /**
     * Validates a student instance according to business rules.
     */
    public void validateStudent(Student student) {
        if (student == null) {
            throw new StudentValidationException("Student object cannot be null.");
        }
        if (student.getId() == null || student.getId().trim().isEmpty()) {
            throw new StudentValidationException("Student ID cannot be null or empty.");
        }
        if (student.getName() == null || student.getName().trim().isEmpty()) {
            throw new StudentValidationException("Student name cannot be null or empty.");
        }
        if (student.getEmail() == null || !EMAIL_PATTERN.matcher(student.getEmail()).matches()) {
            throw new StudentValidationException("Invalid email format: " + student.getEmail());
        }
        if (student.getDepartment() == null || student.getDepartment().trim().isEmpty()) {
            throw new StudentValidationException("Department cannot be null or empty.");
        }
        if (student.getGpa() < 0.0 || student.getGpa() > 4.0) {
            throw new StudentValidationException("GPA must be between 0.0 and 4.0. Given: " + student.getGpa());
        }
    }

    /**
     * Adds a new student record after validation.
     */
    public Student addStudent(Student student) {
        validateStudent(student);

        String id = student.getId().trim();
        if (studentStore.containsKey(id)) {
            throw new StudentValidationException("Student with ID '" + id + "' already exists.");
        }

        studentStore.put(id, student);
        return student;
    }

    /**
     * Retrieves student by ID.
     */
    public Optional<Student> getStudentById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(studentStore.get(id.trim()));
    }

    /**
     * Returns an unmodifiable list of all students.
     */
    public List<Student> getAllStudents() {
        return Collections.unmodifiableList(new ArrayList<>(studentStore.values()));
    }

    /**
     * Removes student by ID.
     */
    public boolean deleteStudent(String id) {
        if (id == null) return false;
        return studentStore.remove(id.trim()) != null;
    }

    /**
     * Updates an existing student record.
     */
    public Student updateStudent(Student student) {
        validateStudent(student);
        String id = student.getId().trim();
        if (!studentStore.containsKey(id)) {
            throw new StudentValidationException("Cannot update. Student with ID '" + id + "' not found.");
        }
        studentStore.put(id, student);
        return student;
    }

    /**
     * Calculates the average GPA of all registered students.
     */
    public double calculateAverageGpa() {
        if (studentStore.isEmpty()) {
            return 0.0;
        }
        double sum = 0.0;
        for (Student s : studentStore.values()) {
            sum += s.getGpa();
        }
        return sum / studentStore.size();
    }

    /**
     * Returns total count of students.
     */
    public int getStudentCount() {
        return studentStore.size();
    }
}

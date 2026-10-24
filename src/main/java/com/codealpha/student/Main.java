package com.codealpha.student;

import java.util.List;

/**
 * Main application class demonstrating student management functionality.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("  CodeAlpha - Student Management Application      ");
        System.out.println("  DevOps Task 2: Jenkins Remoting Build System    ");
        System.out.println("==================================================");

        StudentService service = new StudentService();

        // 1. Add sample students
        try {
            System.out.println("\n[1] Adding sample students...");
            service.addStudent(new Student("STU-101", "Alice Johnson", "alice@codealpha.org", "Computer Science", 3.85));
            service.addStudent(new Student("STU-102", "Bob Smith", "bob@codealpha.org", "Information Technology", 3.60));
            service.addStudent(new Student("STU-103", "Charlie Davis", "charlie@codealpha.org", "Cyber Security", 3.92));
            System.out.println("Successfully added " + service.getStudentCount() + " students.");
        } catch (StudentValidationException ex) {
            System.err.println("Error adding students: " + ex.getMessage());
        }

        // 2. Display student details
        System.out.println("\n[2] Current Student Roster:");
        List<Student> students = service.getAllStudents();
        for (Student s : students) {
            System.out.println(" -> " + s);
        }

        // 3. Display GPA average
        System.out.printf("\n[3] Class Average GPA: %.2f\n", service.calculateAverageGpa());

        // 4. Test validation logic
        System.out.println("\n[4] Testing Validation (Invalid GPA):");
        try {
            service.addStudent(new Student("STU-999", "Invalid Student", "test@domain.com", "Engineering", 4.50));
        } catch (StudentValidationException e) {
            System.out.println(" Caught expected validation error: " + e.getMessage());
        }

        System.out.println("\n==================================================");
        System.out.println(" Application execution finished successfully!    ");
        System.out.println("==================================================");
    }
}

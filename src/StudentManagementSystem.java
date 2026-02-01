import java.sql.*;
import java.util.Scanner;

public class StudentManagementSystem {
    private static final String URL = "jdbc:postgresql://localhost:5432/student_management";
    private static final String USER = "student_db";
    private static final String PASSWORD = "12345678";
    
    private static Connection conn = null;
    private static Scanner scanner = new Scanner(System.in);
    
    public static void main(String[] args) {
        try {
            // Load PostgreSQL JDBC Driver
            Class.forName("org.postgresql.Driver");
            
            // Establish connection
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connected to PostgreSQL database successfully!");
            
            // Create tables if not exists
            createTables();
            
            // Main menu loop
            boolean running = true;
            while (running) {
                System.out.println("\n=== Student Management System ===");
                System.out.println("1. Add Student");
                System.out.println("2. View All Students");
                System.out.println("3. Search Student by ID");
                System.out.println("4. Update Student");
                System.out.println("5. Delete Student");
                System.out.println("6. Add Marks for Student");
                System.out.println("7. View Student Marks");
                System.out.println("8. Update Marks");
                System.out.println("9. View Student Report Card");
                System.out.println("10. View Class Performance");
                System.out.println("11. Exit");
                System.out.print("Enter your choice: ");
                
                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline
                
                switch (choice) {
                    case 1:
                        addStudent();
                        break;
                    case 2:
                        viewAllStudents();
                        break;
                    case 3:
                        searchStudent();
                        break;
                    case 4:
                        updateStudent();
                        break;
                    case 5:
                        deleteStudent();
                        break;
                    case 6:
                        addMarks();
                        break;
                    case 7:
                        viewStudentMarks();
                        break;
                    case 8:
                        updateMarks();
                        break;
                    case 9:
                        viewReportCard();
                        break;
                    case 10:
                        viewClassPerformance();
                        break;
                    case 11:
                        running = false;
                        System.out.println("Thank you for using Student Management System!");
                        break;
                    default:
                        System.out.println("Invalid choice! Please try again.");
                }
            }
            
        } catch (ClassNotFoundException e) {
            System.out.println("PostgreSQL JDBC Driver not found!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Database connection error!");
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) conn.close();
                scanner.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    private static void createTables() throws SQLException {
        // Create students table
        String studentsTable = "CREATE TABLE IF NOT EXISTS students (" +
                    "id SERIAL PRIMARY KEY, " +
                    "name VARCHAR(100) NOT NULL, " +
                    "age INT NOT NULL, " +
                    "email VARCHAR(100) UNIQUE NOT NULL, " +
                    "course VARCHAR(100) NOT NULL, " +
                    "phone VARCHAR(15))";
        
        // Create marks table
        String marksTable = "CREATE TABLE IF NOT EXISTS marks (" +
                    "mark_id SERIAL PRIMARY KEY, " +
                    "student_id INT NOT NULL, " +
                    "subject VARCHAR(100) NOT NULL, " +
                    "marks_obtained DECIMAL(5,2) NOT NULL, " +
                    "total_marks DECIMAL(5,2) NOT NULL, " +
                    "exam_type VARCHAR(50) NOT NULL, " +
                    "exam_date DATE, " +
                    "FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE, " +
                    "CONSTRAINT marks_range CHECK (marks_obtained >= 0 AND marks_obtained <= total_marks))";
        
        Statement stmt = conn.createStatement();
        stmt.execute(studentsTable);
        stmt.execute(marksTable);
        stmt.close();
    }
    
    private static void addStudent() {
        try {
            System.out.println("\n--- Add New Student ---");
            System.out.print("Enter Name: ");
            String name = scanner.nextLine();
            
            System.out.print("Enter Age: ");
            int age = scanner.nextInt();
            scanner.nextLine();
            
            System.out.print("Enter Email: ");
            String email = scanner.nextLine();
            
            System.out.print("Enter Course: ");
            String course = scanner.nextLine();
            
            System.out.print("Enter Phone: ");
            String phone = scanner.nextLine();
            
            String sql = "INSERT INTO students (name, age, email, course, phone) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);
            pstmt.setInt(2, age);
            pstmt.setString(3, email);
            pstmt.setString(4, course);
            pstmt.setString(5, phone);
            
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Student added successfully!");
            }
            pstmt.close();
            
        } catch (SQLException e) {
            System.out.println("Error adding student: " + e.getMessage());
        }
    }
    
    private static void viewAllStudents() {
        try {
            System.out.println("\n--- All Students ---");
            String sql = "SELECT * FROM students ORDER BY id";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            System.out.printf("%-5s %-20s %-5s %-30s %-20s %-15s%n", 
                            "ID", "Name", "Age", "Email", "Course", "Phone");
            System.out.println("=".repeat(100));
            
            boolean hasRecords = false;
            while (rs.next()) {
                hasRecords = true;
                System.out.printf("%-5d %-20s %-5d %-30s %-20s %-15s%n",
                                rs.getInt("id"),
                                rs.getString("name"),
                                rs.getInt("age"),
                                rs.getString("email"),
                                rs.getString("course"),
                                rs.getString("phone"));
            }
            
            if (!hasRecords) {
                System.out.println("No students found in the database.");
            }
            
            rs.close();
            stmt.close();
            
        } catch (SQLException e) {
            System.out.println("Error retrieving students: " + e.getMessage());
        }
    }
    
    private static void searchStudent() {
        try {
            System.out.print("\nEnter Student ID: ");
            int id = scanner.nextInt();
            scanner.nextLine();
            
            String sql = "SELECT * FROM students WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                System.out.println("\n--- Student Details ---");
                System.out.println("ID: " + rs.getInt("id"));
                System.out.println("Name: " + rs.getString("name"));
                System.out.println("Age: " + rs.getInt("age"));
                System.out.println("Email: " + rs.getString("email"));
                System.out.println("Course: " + rs.getString("course"));
                System.out.println("Phone: " + rs.getString("phone"));
            } else {
                System.out.println("Student not found!");
            }
            
            rs.close();
            pstmt.close();
            
        } catch (SQLException e) {
            System.out.println("Error searching student: " + e.getMessage());
        }
    }
    
    private static void updateStudent() {
        try {
            System.out.print("\nEnter Student ID to update: ");
            int id = scanner.nextInt();
            scanner.nextLine();
            
            // Check if student exists
            String checkSql = "SELECT * FROM students WHERE id = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setInt(1, id);
            ResultSet rs = checkStmt.executeQuery();
            
            if (!rs.next()) {
                System.out.println("Student not found!");
                rs.close();
                checkStmt.close();
                return;
            }
            rs.close();
            checkStmt.close();
            
            System.out.print("Enter New Name: ");
            String name = scanner.nextLine();
            
            System.out.print("Enter New Age: ");
            int age = scanner.nextInt();
            scanner.nextLine();
            
            System.out.print("Enter New Email: ");
            String email = scanner.nextLine();
            
            System.out.print("Enter New Course: ");
            String course = scanner.nextLine();
            
            System.out.print("Enter New Phone: ");
            String phone = scanner.nextLine();
            
            String sql = "UPDATE students SET name=?, age=?, email=?, course=?, phone=? WHERE id=?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);
            pstmt.setInt(2, age);
            pstmt.setString(3, email);
            pstmt.setString(4, course);
            pstmt.setString(5, phone);
            pstmt.setInt(6, id);
            
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Student updated successfully!");
            }
            pstmt.close();
            
        } catch (SQLException e) {
            System.out.println("Error updating student: " + e.getMessage());
        }
    }
    
    private static void deleteStudent() {
        try {
            System.out.print("\nEnter Student ID to delete: ");
            int id = scanner.nextInt();
            scanner.nextLine();
            
            System.out.print("Are you sure you want to delete this student? (yes/no): ");
            String confirm = scanner.nextLine();
            
            if (confirm.equalsIgnoreCase("yes")) {
                String sql = "DELETE FROM students WHERE id = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, id);
                
                int rows = pstmt.executeUpdate();
                if (rows > 0) {
                    System.out.println("Student and all associated marks deleted successfully!");
                } else {
                    System.out.println("Student not found!");
                }
                pstmt.close();
            } else {
                System.out.println("Deletion cancelled.");
            }
            
        } catch (SQLException e) {
            System.out.println("Error deleting student: " + e.getMessage());
        }
    }
    
    private static void addMarks() {
        try {
            System.out.println("\n--- Add Marks ---");
            System.out.print("Enter Student ID: ");
            int studentId = scanner.nextInt();
            scanner.nextLine();
            
            // Check if student exists
            String checkSql = "SELECT name FROM students WHERE id = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setInt(1, studentId);
            ResultSet rs = checkStmt.executeQuery();
            
            if (!rs.next()) {
                System.out.println("Student not found!");
                rs.close();
                checkStmt.close();
                return;
            }
            String studentName = rs.getString("name");
            System.out.println("Adding marks for: " + studentName);
            rs.close();
            checkStmt.close();
            
            System.out.print("Enter Subject: ");
            String subject = scanner.nextLine();
            
            System.out.print("Enter Marks Obtained: ");
            double marksObtained = scanner.nextDouble();
            
            System.out.print("Enter Total Marks: ");
            double totalMarks = scanner.nextDouble();
            scanner.nextLine();
            
            System.out.print("Enter Exam Type (Midterm/Final/Quiz/Assignment): ");
            String examType = scanner.nextLine();
            
            System.out.print("Enter Exam Date (YYYY-MM-DD): ");
            String examDate = scanner.nextLine();
            
            String sql = "INSERT INTO marks (student_id, subject, marks_obtained, total_marks, exam_type, exam_date) " +
                        "VALUES (?, ?, ?, ?, ?, ?::date)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);
            pstmt.setString(2, subject);
            pstmt.setDouble(3, marksObtained);
            pstmt.setDouble(4, totalMarks);
            pstmt.setString(5, examType);
            pstmt.setString(6, examDate);
            
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                double percentage = (marksObtained / totalMarks) * 100;
                System.out.printf("Marks added successfully! Percentage: %.2f%%%n", percentage);
            }
            pstmt.close();
            
        } catch (SQLException e) {
            System.out.println("Error adding marks: " + e.getMessage());
        }
    }
    
    private static void viewStudentMarks() {
        try {
            System.out.print("\nEnter Student ID: ");
            int studentId = scanner.nextInt();
            scanner.nextLine();
            
            String sql = "SELECT s.name, m.mark_id, m.subject, m.marks_obtained, m.total_marks, " +
                        "m.exam_type, m.exam_date, " +
                        "ROUND((m.marks_obtained / m.total_marks * 100)::numeric, 2) as percentage " +
                        "FROM students s " +
                        "JOIN marks m ON s.id = m.student_id " +
                        "WHERE s.id = ? " +
                        "ORDER BY m.exam_date DESC";
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            
            boolean hasRecords = false;
            String studentName = "";
            
            System.out.println("\n--- Student Marks ---");
            System.out.printf("%-8s %-20s %-15s %-10s %-12s %-15s %-12s%n",
                            "Mark ID", "Subject", "Marks", "Total", "Percentage", "Exam Type", "Date");
            System.out.println("=".repeat(95));
            
            while (rs.next()) {
                if (!hasRecords) {
                    studentName = rs.getString("name");
                    System.out.println("Student: " + studentName);
                    System.out.println("=".repeat(95));
                }
                hasRecords = true;
                
                System.out.printf("%-8d %-20s %-15.2f %-10.2f %-12.2f%% %-15s %-12s%n",
                                rs.getInt("mark_id"),
                                rs.getString("subject"),
                                rs.getDouble("marks_obtained"),
                                rs.getDouble("total_marks"),
                                rs.getDouble("percentage"),
                                rs.getString("exam_type"),
                                rs.getDate("exam_date"));
            }
            
            if (!hasRecords) {
                System.out.println("No marks found for this student.");
            }
            
            rs.close();
            pstmt.close();
            
        } catch (SQLException e) {
            System.out.println("Error retrieving marks: " + e.getMessage());
        }
    }
    
    private static void updateMarks() {
        try {
            System.out.print("\nEnter Mark ID to update: ");
            int markId = scanner.nextInt();
            scanner.nextLine();
            
            // Check if mark exists
            String checkSql = "SELECT * FROM marks WHERE mark_id = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setInt(1, markId);
            ResultSet rs = checkStmt.executeQuery();
            
            if (!rs.next()) {
                System.out.println("Mark record not found!");
                rs.close();
                checkStmt.close();
                return;
            }
            rs.close();
            checkStmt.close();
            
            System.out.print("Enter New Marks Obtained: ");
            double marksObtained = scanner.nextDouble();
            
            System.out.print("Enter New Total Marks: ");
            double totalMarks = scanner.nextDouble();
            scanner.nextLine();
            
            System.out.print("Enter New Exam Type: ");
            String examType = scanner.nextLine();
            
            String sql = "UPDATE marks SET marks_obtained=?, total_marks=?, exam_type=? WHERE mark_id=?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setDouble(1, marksObtained);
            pstmt.setDouble(2, totalMarks);
            pstmt.setString(3, examType);
            pstmt.setInt(4, markId);
            
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Marks updated successfully!");
            }
            pstmt.close();
            
        } catch (SQLException e) {
            System.out.println("Error updating marks: " + e.getMessage());
        }
    }
    
    private static void viewReportCard() {
        try {
            System.out.print("\nEnter Student ID: ");
            int studentId = scanner.nextInt();
            scanner.nextLine();
            
            // Get student info
            String studentSql = "SELECT * FROM students WHERE id = ?";
            PreparedStatement studentStmt = conn.prepareStatement(studentSql);
            studentStmt.setInt(1, studentId);
            ResultSet studentRs = studentStmt.executeQuery();
            
            if (!studentRs.next()) {
                System.out.println("Student not found!");
                return;
            }
            
            System.out.println("\n╔════════════════════════════════════════════╗");
            System.out.println("║           STUDENT REPORT CARD              ║");
            System.out.println("╚════════════════════════════════════════════╝");
            System.out.println("Name: " + studentRs.getString("name"));
            System.out.println("Course: " + studentRs.getString("course"));
            System.out.println("Email: " + studentRs.getString("email"));
            System.out.println("─".repeat(90));
            
            studentRs.close();
            studentStmt.close();
            
            // Get marks summary
            String marksSql = "SELECT subject, " +
                            "AVG(marks_obtained) as avg_marks, " +
                            "AVG(total_marks) as avg_total, " +
                            "ROUND(AVG(marks_obtained / total_marks * 100)::numeric, 2) as avg_percentage, " +
                            "COUNT(*) as exam_count " +
                            "FROM marks " +
                            "WHERE student_id = ? " +
                            "GROUP BY subject";
            
            PreparedStatement marksStmt = conn.prepareStatement(marksSql);
            marksStmt.setInt(1, studentId);
            ResultSet marksRs = marksStmt.executeQuery();
            
            System.out.printf("%-20s %-12s %-12s %-12s %-10s%n",
                            "Subject", "Avg Marks", "Avg Total", "Percentage", "Exams");
            System.out.println("─".repeat(90));
            
            double totalPercentage = 0;
            int subjectCount = 0;
            
            while (marksRs.next()) {
                subjectCount++;
                double percentage = marksRs.getDouble("avg_percentage");
                totalPercentage += percentage;
                
                System.out.printf("%-20s %-12.2f %-12.2f %-12.2f%% %-10d%n",
                                marksRs.getString("subject"),
                                marksRs.getDouble("avg_marks"),
                                marksRs.getDouble("avg_total"),
                                percentage,
                                marksRs.getInt("exam_count"));
            }
            
            if (subjectCount > 0) {
                double overallPercentage = totalPercentage / subjectCount;
                System.out.println("─".repeat(90));
                System.out.printf("Overall Percentage: %.2f%%%n", overallPercentage);
                System.out.println("Grade: " + calculateGrade(overallPercentage));
            } else {
                System.out.println("No marks recorded for this student.");
            }
            
            marksRs.close();
            marksStmt.close();
            
        } catch (SQLException e) {
            System.out.println("Error generating report card: " + e.getMessage());
        }
    }
    
    private static void viewClassPerformance() {
        try {
            System.out.println("\n--- Class Performance Summary ---");
            
            String sql = "SELECT s.id, s.name, s.course, " +
                        "COUNT(m.mark_id) as total_exams, " +
                        "ROUND(AVG(m.marks_obtained / m.total_marks * 100)::numeric, 2) as avg_percentage " +
                        "FROM students s " +
                        "LEFT JOIN marks m ON s.id = m.student_id " +
                        "GROUP BY s.id, s.name, s.course " +
                        "ORDER BY avg_percentage DESC NULLS LAST";
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            System.out.printf("%-5s %-20s %-20s %-12s %-15s %-10s%n",
                            "ID", "Name", "Course", "Total Exams", "Avg %", "Grade");
            System.out.println("=".repeat(85));
            
            while (rs.next()) {
                int totalExams = rs.getInt("total_exams");
                double avgPercentage = rs.getDouble("avg_percentage");
                String grade = totalExams > 0 ? calculateGrade(avgPercentage) : "N/A";
                
                System.out.printf("%-5d %-20s %-20s %-12d %-15.2f %-10s%n",
                                rs.getInt("id"),
                                rs.getString("name"),
                                rs.getString("course"),
                                totalExams,
                                avgPercentage,
                                grade);
            }
            
            rs.close();
            stmt.close();
            
        } catch (SQLException e) {
            System.out.println("Error retrieving class performance: " + e.getMessage());
        }
    }
    
    private static String calculateGrade(double percentage) {
        if (percentage >= 90) return "A+";
        else if (percentage >= 80) return "A";
        else if (percentage >= 70) return "B";
        else if (percentage >= 60) return "C";
        else if (percentage >= 50) return "D";
        else return "F";
    }
}
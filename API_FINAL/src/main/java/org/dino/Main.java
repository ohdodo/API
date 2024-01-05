package org.dino;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

class Attendance {
    private String date;
    private boolean status;

    public Attendance(String date, boolean status) {
        this.date = date;
        this.status = status;
    }

    public String getDate() {
        return date;
    }

    public boolean getStatus() {
        return status;
    }
}

class Student {
    private int id;
    private String name;
    private List<Attendance> attendanceList;

    public Student(int id, String name) {
        this.id = id;
        this.name = name;
        this.attendanceList = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Attendance> getAttendanceList() {
        return attendanceList;
    }

    public void addAttendance(Attendance attendance) {
        attendanceList.add(attendance);
    }
}

class Section {
    private String sectionCode;
    private List<Student> studentList;

    public Section(String sectionCode) {
        this.sectionCode = sectionCode;
        this.studentList = new ArrayList<>();
    }

    public String getSectionCode() {
        return sectionCode;
    }

    public List<Student> getStudentList() {
        return studentList;
    }

    public void addStudent(Student student) {
        studentList.add(student);
    }
}

class Course {
    private List<Section> sectionList;

    public Course() {
        this.sectionList = new ArrayList<>();
    }

    public List<Section> getSectionList() {
        return sectionList;
    }

    public void addSection(Section section) {
        sectionList.add(section);
    }
}

public class Main {
    private static final String PASTEBIN_URL = "https://pastebin.com/raw/TytE0smz";
    private Course course;

    public static void main(String[] args) {
        try {
            String pastebinData = fetchData();
            Main main = new Main();
            main.course = parseData(pastebinData);
            main.sortSectionsAlphabetically(main.course);
            main.handleUserInput();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to fetch data.");
        }
    }
    private void displayAllData() {
        StringBuilder display = new StringBuilder();
        for (Section section : course.getSectionList()) {
            display.setLength(0); // Clear the StringBuilder
            display.append(displaySectionData(section));
            System.out.println(display.toString());
        }
    }
    private void sortSectionsAlphabetically(Course course) {
        List<Section> sectionList = course.getSectionList();
        Collections.sort(sectionList, Comparator.comparing(Section::getSectionCode));
    }

    private String displaySectionData(Section section) {
        StringBuilder display = new StringBuilder();
        display.append("\nSection: ").append(section.getSectionCode()).append("\n");
        display.append("Total Students: ").append(section.getStudentList().size()).append("\n\n");

        for (Student student : section.getStudentList()) {
            display.append(" Student ID: ").append(student.getId()).append("\n");
            display.append(" Student Name: ").append(student.getName()).append("\n");

            for (Attendance attendance : student.getAttendanceList()) {
                display.append("  Date: ").append(attendance.getDate());
                display.append("  Status: ").append(attendance.getStatus() ? "Present" : "Absent").append("\n");
            }

            display.append("\n");
        }

        return display.toString();
    }

    private static String fetchData() throws IOException {
        URL url = new URL(Main.PASTEBIN_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            return response.toString();
        } else {
            throw new IOException("Failed to fetch data. Response code: " + responseCode);
        }
    }

    private static Course parseData(String data) {
        Course course = new Course();

        JSONObject courseData = new JSONObject(data);

        for (String courseCode : courseData.keySet()) {
            JSONObject sectionsData = courseData.getJSONObject(courseCode);

            for (String sectionCode : sectionsData.keySet()) {
                JSONObject sectionInfo = sectionsData.getJSONObject(sectionCode);
                int totalNumberOfStudents = sectionInfo.getInt("total_students");

                Section section = new Section(sectionCode);

                JSONArray studentsData = sectionInfo.getJSONArray("data");
                for (int i = 0; i < studentsData.length(); i++) {
                    JSONObject studentInfo = studentsData.getJSONObject(i);
                    int studentId = studentInfo.getInt("id");
                    String studentName = studentInfo.getString("name");

                    Student student = new Student(studentId, studentName);

                    JSONArray attendanceData = studentInfo.getJSONArray("attendance");
                    for (int j = 0; j < attendanceData.length(); j++) {
                        JSONObject attendanceInfo = attendanceData.getJSONObject(j);
                        boolean isPresent = attendanceInfo.getBoolean("is_present");
                        String date = attendanceInfo.getString("date");

                        Attendance attendance = new Attendance(date, isPresent);
                        student.addAttendance(attendance);
                    }

                    section.addStudent(student);
                }

                course.addSection(section);
            }
        }

        return course;
    }

    private void handleUserInput() {
        Scanner scanner = new Scanner(System.in);
        String userChoice;
        while (true) {
            System.out.println("S C H O O L  A T T E N D A N C E");
            System.out.println("\nPlease choose an operation to perform:");
            System.out.println("1. Search for a student by name");
            System.out.println("2. Select all students in a section");
            System.out.println("3. Select all students");
            System.out.println("4. Exit");
            System.out.print("Enter your choice: ");
            userChoice = scanner.nextLine();

            switch (userChoice) {
                case "1": ;
                    System.out.print("Enter the name of the student to search for: ");
                    displayStudentsByName(scanner.nextLine());
                    System.out.println("Press Enter to continue...");
                    scanner.nextLine();
                    handleUserInput();
                    break;
                case "2":
                    System.out.print("Enter the section code: ");
                    selectAllStudentsInSection(scanner.nextLine());
                    System.out.println("Press Enter to continue...");
                    scanner.nextLine();
                    handleUserInput();
                    break;
                case "3":
                    displayAllData();
                    System.out.println("Press Enter to continue...");
                    scanner.nextLine();
                    handleUserInput();
                    break;
                case "4":
                    return;
                default:
                    System.out.println("Invalid choice. Please choose a valid operation.");
                    break;
            }
        }
    }

    private void displayStudentsByName(String studentName) {
        boolean studentFound = false;

        for (Section section : course.getSectionList()) {
            for (Student student : section.getStudentList()) {
                if (student.getName().toLowerCase().contains(studentName.toLowerCase())) {
                    studentFound = true;
                    System.out.println("\nStudent ID: " + student.getId() + "\nStudent Name: " + student.getName() +
                            "\nSection " + section.getSectionCode() + ":\n");
                    for (Attendance attendance : student.getAttendanceList()) {
                        System.out.println("Date: " + attendance.getDate() +
                                "\tStatus: " + (attendance.getStatus() ? "Present" : "Absent"));
                    }
                    System.out.println("\n");
                }
            }
        }

        if (!studentFound) {
            System.out.println("No students found with the name containing '" + studentName + "'.");
        }
    }

    private void selectAllStudentsInCourse(String courseCode) {
        for (Section section : course.getSectionList()) {
            if (section.getSectionCode().equalsIgnoreCase(courseCode)) {
                System.out.println("\nStudents in Course " + courseCode + ":");
                for (Student student : section.getStudentList()) {
                    System.out.println("Student ID: " + student.getId() + ", Name: " + student.getName());
                }
                return;
            }
        }
        System.out.println("No course found with the code '" + courseCode + "'.");
    }

    private void selectAllStudentsInSection(String sectionCode) {
        for (Section section : course.getSectionList()) {
            if (section.getSectionCode().equalsIgnoreCase(sectionCode)) {
                System.out.println("\nStudents in Section " + sectionCode + ":");
                for (Student student : section.getStudentList()) {
                    System.out.println("Student ID: " + student.getId() + "\nStudent Name: " + student.getName());
                }
                return;
            }
        }
        System.out.println("No section found with the code '" + sectionCode + "'.");
    }

    private void selectAllStudentsWithAttendance(boolean attendanceStatus) {
        System.out.println("\nStudents with attendance status: " + (attendanceStatus ? "Present" : "Absent") + ":");
        for (Section section : course.getSectionList()) {
            for (Student student : section.getStudentList()) {
                for (Attendance attendance : student.getAttendanceList()) {
                    if (attendance.getStatus() == attendanceStatus) {
                        System.out.println("Student ID: " + student.getId() + ", Name: " + student.getName());
                    }
                }
            }
        }
    }
}

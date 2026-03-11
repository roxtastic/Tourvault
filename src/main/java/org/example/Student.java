package org.example;
public class Student extends Person {
    private String school;
    private int studyYear;

    public Student(String surname, String name, String role) {
        super(surname, name, role);
    }

    public String getSchool() { return school; }
    public void setSchool(String school) { this.school = school; }

    public int getStudyYear() { return studyYear; }
    public void setStudyYear(int studyYear) { this.studyYear = studyYear; }
    public String toString() {
        return super.toString() + String.format(", school=%s, studyYear=%d", school, studyYear);
    }
}



package org.example;
public class Professor extends Person {
    private int experience;
    private String school;

    public Professor(String surname, String name, String role) {
        super(surname, name, role);
    }

    public String getName() {
        return super.getName();
    }

    public void setExperience(int experience) { this.experience = experience; }
    public void setSchool(String school) { this.school = school; }

    public int getExperience() { return experience; }
    public String getSchool() { return school; }
    public String toString() {
        return super.toString() + String.format(", school=%s, experience=%d", school, experience);
    }
}

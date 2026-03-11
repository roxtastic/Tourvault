package org.example;
import java.util.ArrayList;
import java.util.List;

public class Group {
    private List<Person> members = new ArrayList<>();
    private Professor guide;
    private long museumCode;
    private String timetable;

    public void addGuide(Professor guide) throws GuideExistsException, IllegalArgumentException {
        if (guide == null) {
            throw new IllegalArgumentException("Ghidul nu poate fi null.");
        }
        if (this.guide != null) {
            throw new GuideExistsException("Guide already exists.");
        }
        this.guide = guide;
    }

    public void addMember(Person member) throws GroupThresholdException {
        members.add(member);
    }

    public List<Person> getMembers() { return new ArrayList<>(members); }

    public Professor getGuide() { return guide; }

    public long getMuseumCode() { return museumCode; }
    public void setMuseumCode(long museumCode) { this.museumCode = museumCode; }

    public String getTimetable() { return timetable; }
    public void setTimetable(String timetable) { this.timetable = timetable; }
    public String getGuideName(){
        String name = guide.getName();
        return name;
    }
    public void resetGuide() {
        this.guide = null;
    }

    public void setGuide(Professor guide)
    {
        this.guide = guide;
    }
    public boolean removeMember(Person member) {
        if (members.contains(member)) {
            members.remove(member);
            return true;
        }
        return false;
    }
}
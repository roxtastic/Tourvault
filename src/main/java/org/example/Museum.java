package org.example;
import java.util.ArrayList;
import java.util.List;

public class Museum {
    private String name;
    private long code;
    private long supervisorCode;
    private Location location;
    private Person manager;
    private Integer foundingYear;
    private String phoneNumber;
    private String fax;
    private String email;
    private String url;
    private String profile;
    private List<String> events = new ArrayList<>();

    public Museum(String name, long code, long supervisorCode, Location location) {
        this.name = name;
        this.code = code;
        this.supervisorCode = supervisorCode;
        this.location = location;
    }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public long getCode() { return code; }
    public void setCode(long code) { this.code = code; }

    public long getSupervisorCode() { return supervisorCode; }
    public void setSupervisorCode(long supervisorCode) { this.supervisorCode = supervisorCode; }

    public Location getLocation() { return location; }
    public void setLocation(Location location) { this.location = location; }

    public Person getManager() { return manager; }
    public void setManager(Person manager) { this.manager = manager; }

    public Integer getFoundingYear() { return foundingYear; }
    public void setFoundingYear(Integer foundingYear) { this.foundingYear = foundingYear; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getFax() { return fax; }
    public void setFax(String fax) { this.fax = fax; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getProfile() { return profile; }
    public void setProfile(String profile) { this.profile = profile; }

    public List<String> getEvents() { return new ArrayList<>(events); }
}
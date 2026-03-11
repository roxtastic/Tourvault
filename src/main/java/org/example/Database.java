package org.example;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

public class Database {
    private static Database instance;
    private Set<Museum> museums = new HashSet<>();
    private Set<Group> groups = new HashSet<>();

    private Database() {
    }

    public static Database getInstance() {
        if (instance == null) {
            synchronized (Database.class) {
                if (instance == null) {
                    instance = new Database();
                }
            }
        }
        return instance;
    }

    public Museum getMuseumByCode(long code) throws IllegalArgumentException {
        return museums.stream()
                .filter(m -> m.getCode() == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Museum with code " + code + " not found"));
    }

    public void notifyGuides(long museumCode, String message, PrintWriter out) {
        groups.stream()
                .filter(g -> g.getMuseumCode() == museumCode)
                .forEach(g -> {
                    Professor guide = g.getGuide();
                    if (guide != null) {
                        out.println("To: " + guide.getEmail() + " ## Message: " + message);
                    }
                });
    }

    public void addMuseum(Museum museum) {
        museums.add(museum);
    }

    public void addMuseums(Set<Museum> museums) {
        this.museums.addAll(museums);
    }

    public void addGroup(Group group) {
        groups.add(group);
    }

    public void addGroups(Set<Group> groups) {
        this.groups.addAll(groups);
    }

    public Set<Group> getGroups() {
        return new HashSet<>(groups);
    }

    public Set<Museum> getMuseums() {
        return new HashSet<>(museums);
    }

    public void resetDatabase() {
        museums.clear();
        groups.clear();
    }

    public boolean removePersonCompletely(Person removePerson, long museumCode, String timetable) {
        boolean removed = false;
        boolean foundInSpecificGroup = false;

        for (Group group : groups) {
            // Verificăm dacă persoana este membru în grup
            if (group.getMembers().remove(removePerson)) {
                removed = true;
                // Verificăm dacă este în grupul specific cerut (cu muzeul și ora corespunzătoare)
                if (group.getMuseumCode() == museumCode && group.getTimetable().equals(timetable)) {
                    foundInSpecificGroup = true;
                }
            }

            // Verificăm dacă persoana este ghidul grupului
            if (group.getGuide() != null && group.getGuide().equals(removePerson)) {
                group.setGuide(null);
                removed = true;
            }
        }

        // Eliminăm grupurile goale (dacă nu mai au nici ghid, nici membri)
        groups.removeIf(group -> group.getGuide() == null && group.getMembers().isEmpty());

        // Dacă a fost eliminat din baza de date, dar nu a fost găsit în grupul cerut, returnăm false
        if (removed && !foundInSpecificGroup) {
            throw new IllegalArgumentException("PersonNotExistsException: Person was not found in the group.");
        }

        return removed;
    }

}
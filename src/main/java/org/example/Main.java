package org.example;

import java.io.*;
import java.time.LocalTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Main {
    private static final Database database = Database.getInstance();
    private static final Map<String, PrintWriter> outputWriters = new HashMap<>();
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        if (args.length != 2 && args.length != 4) {
            System.err.println("Usage: java Main <pathType> <outputPath> OR java Main <pathType> <museumPath> <groupPath> <eventPath>");
            return;
        }
        try {
            if (args.length == 2) {
                processSingleFile(args[0], args[1]);
            } else {
                processMultipleFiles(args[1], args[2], args[3]);
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Eroare IO", e);
        } finally {
            outputWriters.values().forEach(writer -> {
                if (writer != null) writer.close();
            });
        }
    }

    private static void processSingleFile(String pathType, String outputPath) throws IOException {
        database.resetDatabase();
        PathTypes type;
        try {
            type = PathTypes.valueOf(pathType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Tip invalid: " + pathType);
        }

        if (!outputPath.endsWith(".out")) {
            outputPath += ".out";
        }

        String inputPath = outputPath.replace(".out", ".in");
        File inputFile = new File(inputPath);

        if (!inputFile.exists()) {
            throw new FileNotFoundException("Fisierul de intrare nu exista: " + inputPath);
        }
        try (PrintWriter writer = new PrintWriter(outputPath)) {
            outputWriters.put(type.name(), writer);
            processCommands(readLines(inputPath), writer);
        }
    }

    private static void processMultipleFiles(String museumPath, String groupPath, String eventPath) throws IOException {
        processFileSet(PathTypes.MUSEUMS, museumPath);
        processFileSet(PathTypes.GROUPS, groupPath);
        processFileSet(PathTypes.LISTENER, eventPath);
    }

    private static void processFileSet(PathTypes type, String basePath) throws IOException {
        int index = 1;
        String inputPath = String.format("%s_%02d.in", basePath, index);
        File file = new File(inputPath);

        while (file.exists()) {
            String outputPath = inputPath.replace(".in", ".out");
            PrintWriter writer = new PrintWriter(outputPath);
            outputWriters.put(type.name(), writer);
            processCommands(readLines(inputPath), writer);

            index++;
            inputPath = String.format("%s_%02d.in", basePath, index);
            file = new File(inputPath);
        }
    }

    private static void processCommands(List<String> commands, PrintWriter out) {
        boolean isFirstLine = true;
        for (String cmd : commands) {
            if (isFirstLine) {
                isFirstLine = false;
                if (cmd.startsWith("com") || cmd.startsWith("Com")) {
                    continue;
                }
            }

            String[] parts = cmd.split("\\|");
            String command = parts[0].trim();
            String outputLine = "";

            try {
                switch (command) {
                    case "ADD MUSEUM":
                        Museum museum = parseMuseum(parts);
                        database.addMuseum(museum);
                        outputLine = parts[1] + ": " + museum.getName();
                        break;

                    case "ADD GUIDE":
                        try {
                            long museumCode = Long.parseLong(parts[9].trim());
                            String timetable = parts[10].trim();

                            Group group = database.getGroups().stream()
                                    .filter(g -> g.getMuseumCode() == museumCode && g.getTimetable().equals(timetable))
                                    .findFirst()
                                    .orElseGet(() -> {
                                        Group newGroup = new Group();
                                        newGroup.setMuseumCode(museumCode);
                                        newGroup.setTimetable(timetable);
                                        database.addGroup(newGroup);
                                        return newGroup;
                                    });

                            String guideRole = parts[3].trim();
                            if (!guideRole.equalsIgnoreCase("profesor") && !guideRole.equalsIgnoreCase("ghid")) {
                                throw new GuideTypeException("Guide must be a professor.");
                            }

                            if (group.getGuide() != null) {
                                outputLine = String.format("%s ## %s ## GuideExistsException: Guide already exists. ## (new guide: %s)",
                                        parts[9].trim(), parts[10].trim(), group.getGuide());
                                break;
                            }

                            Professor guide = parseProfessor(parts);
                            group.addGuide(guide);
                            outputLine = String.format("%s ## %s ## new guide: %s",
                                    parts[9].trim(), parts[10].trim(), guide);
                        } catch (GuideTypeException e) {
                            outputLine = String.format("%s ## %s ## GuideTypeException: Guide must be a professor. ## (new guide: %s)",
                                    parts[9].trim(), parts[10].trim(), parsePerson(parts));
                        } catch (Exception e) {
                            outputLine = "Exception: Data is broken. ## (" + Arrays.toString(parts) + ")";
                        }
                        break;

                    case "FIND GUIDE":
                        long museumCode = Long.parseLong(parts[1].trim());
                        Group targetGroup = findGroup(museumCode, parts[2].trim());
                        Professor foundGuide = parseProfessor(parts);
                        outputLine = foundGuide != null ?
                                formatOutput(parts[1], parts[2], "guide found: " + foundGuide) :
                                formatOutput(parts[1], parts[2], "guide not exists");
                        break;

                    case "ADD MEMBER":
                        museumCode = Long.parseLong(parts[9].trim());
                        String timetable = parts[10].trim();
                        Person member = parsePerson(parts);

                        Group memberGroup = database.getGroups().stream()
                                .filter(g -> g.getMuseumCode() == museumCode && g.getTimetable().equals(timetable) && g.getGuide() != null)
                                .findFirst()
                                .orElse(null);

                        if (memberGroup == null) {
                            outputLine = String.format("%d ## %s ## GroupNotExistsException: Group does not exist. ## (new member: %s)",
                                    museumCode, timetable, member);
                            break;
                        }

                        if (memberGroup.getMembers().size() >= 10) {
                            outputLine = String.format("%d ## %s ## GroupThresholdException: Group cannot have more than 10 members. ## (new member: %s)",
                                    museumCode, timetable, member);
                            break;
                        }
                        memberGroup.addMember(member);
                        outputLine = String.format("%d ## %s ## new member: %s", museumCode, timetable, member);
                        break;

                    case "REMOVE GUIDE":
                        try {
                            museumCode = Long.parseLong(parts[9].trim());
                            timetable = parts[10].trim();

                            Group guideGroup = findGroup(museumCode, timetable);
                            Professor removedGuide = guideGroup.getGuide();
                            guideGroup.resetGuide();
                            outputLine = String.format("%s ## %s ## removed guide: %s",
                                    parts[9].trim(), parts[10].trim(), removedGuide);
                        } catch (Exception e) {
                            outputLine = "Exception: Data is broken. ## (" + Arrays.toString(parts) + ")";
                        }
                        break;

                    case "FIND MEMBER":
                        // Se presupune că pentru FIND MEMBER, argumentele sunt:
                        // parts[1] = museum code, parts[2] = timetable, iar restul datelor reprezintă detaliile persoanei
                        museumCode = Long.parseLong(parts[1].trim());
                        String memberTimetable = parts[2].trim();
                        Person searchMember = parsePerson(parts);
                        memberGroup = null;
                        try {
                            memberGroup = findGroup(museumCode, memberTimetable);
                            boolean found = memberGroup.getMembers().stream().anyMatch(p -> p.equals(searchMember));
                            if (found) {
                                outputLine = formatOutput(String.valueOf(museumCode), memberTimetable, "member found: " + formatPerson(searchMember));
                            } else {
                                outputLine = formatOutput(String.valueOf(museumCode), memberTimetable, "member not exists: " + formatPerson(searchMember));
                            }
                        } catch (GroupNotExistsException e) {
                            outputLine = formatOutput(String.valueOf(museumCode), memberTimetable, "member not exists: " + formatPerson(searchMember));
                        }
                        break;

                    case "REMOVE MEMBER":
                        try {
                            museumCode = Long.parseLong(parts[9].trim());
                            timetable = parts[10].trim();

                            Person removePerson = parsePerson(parts);
                            Group targetGroupForRemoval = database.getGroups().stream()
                                    .filter(g -> g.getMuseumCode() == museumCode && g.getTimetable().equals(timetable))
                                    .findFirst()
                                    .orElse(null);

                            if (targetGroupForRemoval == null) {
                                outputLine = String.format("%d ## %s ## GroupNotExistsException: Group does not exist. ## (removed member: %s)",
                                        museumCode, timetable, removePerson);
                                break;
                            }

                            boolean personExists = (targetGroupForRemoval.getGuide() != null &&
                                    targetGroupForRemoval.getGuide().equals(removePerson))
                                    || targetGroupForRemoval.getMembers().stream().anyMatch(p -> p.equals(removePerson));

                            if (!personExists) {
                                throw new PersonNotExistsException(String.format("PersonNotExistsException: Person was not found in the group. ## (%s)", removePerson));
                            }

                            boolean removed = false;
                            if (targetGroupForRemoval.getGuide() != null &&
                                    targetGroupForRemoval.getGuide().equals(removePerson)) {
                                targetGroupForRemoval.resetGuide();
                                removed = true;
                            } else {

                                removed = targetGroupForRemoval.removeMember(removePerson);
                            }

                            if (removed) {
                                outputLine = String.format("%d ## %s ## removed member: %s", museumCode, timetable, removePerson);
                            } else {
                                outputLine = String.format("%d ## %s ## Error: Could not remove member: %s", museumCode, timetable, removePerson);
                            }
                        } catch (PersonNotExistsException e) {

                            outputLine = String.format("%s ## %s ## %s", parts[9].trim(), parts[10].trim(), e.getMessage());
                        } catch (Exception e) {
                            outputLine = "Exception: Data is broken. ## (" + Arrays.toString(parts) + ")";
                        }
                        break;

                    case "ADD EVENT":
                        museumCode = Long.parseLong(parts[9].trim());
                        Museum museumEvent = database.getMuseumByCode(museumCode);
                        String message = String.format("To: %s ## Message: %s (%d) %s",
                                parts[2], museumEvent.getName(), museumCode, parts[3]);
                        database.notifyGuides(museumCode, message, out);
                        break;

                    default:
                        throw new IllegalArgumentException("Unknown command");
                }
            } catch (IndexOutOfBoundsException | NullPointerException | NumberFormatException e) {
                outputLine = "Exception: Data is broken. ## (" + cmd + ")";
            } catch (Exception e) {
                outputLine = "ERROR: " + e.getMessage();
            }
            out.println(outputLine);
        }
    }

    private static boolean intervalsOverlap(String existingInterval, String newInterval) {
        String[] existingTimes = existingInterval.split("-");
        String[] newTimes = newInterval.split("-");

        int existingStart = parseTimeToMinutes(existingTimes[0]);
        int existingEnd = parseTimeToMinutes(existingTimes[1]);
        int newStart = parseTimeToMinutes(newTimes[0]);
        int newEnd = parseTimeToMinutes(newTimes[1]);

        return newStart < existingEnd && newEnd > existingStart;
    }

    private static int parseTimeToMinutes(String time) {
        String[] parts = time.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        return hours * 60 + minutes;
    }
    private static String formatPerson(Person p) {
        StringBuilder sb = new StringBuilder();
        sb.append("surname=").append(p.getSurname()).append(", ");
        sb.append("name=").append(p.getName()).append(", ");
        sb.append("role=").append(p.getRole()).append(", ");
        sb.append("age=").append(p.getAge()).append(", ");
        sb.append("email=").append(p.getEmail()).append(", ");
        sb.append("school=").append(p.getSchool());

        if (p instanceof Professor) {
            sb.append(", experience=").append(((Professor) p).getExperience());
        } else if (p instanceof Student) {
            sb.append(", studyYear=").append(((Student) p).getStudyYear());
        }
        return sb.toString();
    }
    private static String convertToStandardTimeFormat(String timeRange) {
        String[] times = timeRange.split("-");
        if (times.length != 2) {
            throw new IllegalArgumentException("Invalid time format: " + timeRange);
        }
        return times[0] + ":00-" + times[1] + ":00";
    }

    private static Museum parseMuseum(String[] parts) {
        String name = parts[2].trim();
        long code = Long.parseLong(parts[1].trim());
        long supervisorCode = Long.parseLong(parts[14].trim());

        String county = parts[3].trim();
        Integer sirutaCode = parseOptionalInt(parts[16]);
        Location location = new Location(county, sirutaCode);

        return new Museum(name, code, supervisorCode, location);
    }

    private static Integer parseOptionalInt(String value) {
        return (value == null || value.trim().isEmpty()) ? null : Integer.parseInt(value.trim());
    }

    private static Professor parseProfessor(String[] parts) {
        Professor professor = new Professor(
                parts[1].trim(),
                parts[2].trim(),
                parts[8].trim()
        );
        professor.setAge(Integer.parseInt(parts[4].trim()));
        professor.setEmail(parts[5].isEmpty() ? null : parts[5].trim());
        professor.setSchool(parts[6].trim());
        professor.setExperience(Integer.parseInt(parts[7].trim()));
        return professor;
    }

    private static boolean doTimeRangesOverlap(String timetable1, String timetable2) {
        if (!timetable1.contains(":") || !timetable2.contains(":")) {
            throw new IllegalArgumentException("Invalid time format: " + timetable1 + " or " + timetable2);
        }

        try {
            String[] times1 = timetable1.split("-");
            String[] times2 = timetable2.split("-");
            LocalTime start1 = LocalTime.parse(times1[0].trim());
            LocalTime end1 = LocalTime.parse(times1[1].trim());
            LocalTime start2 = LocalTime.parse(times2[0].trim());
            LocalTime end2 = LocalTime.parse(times2[1].trim());

            return start1.isBefore(end2) && end1.isAfter(start2);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error parsing time: " + timetable1 + " or " + timetable2, e);
        }
    }

    private static Person parsePerson(String[] parts) {
        String role = "vizitator";

        if (parts[3].trim().equalsIgnoreCase("student")) {
            Student student = new Student(
                    parts[1].trim(),
                    parts[2].trim(),
                    role
            );
            student.setAge(Integer.parseInt(parts[4].trim()));
            student.setEmail(parts[5].isEmpty() ? null : parts[5].trim());
            student.setSchool(parts[6].trim());
            student.setStudyYear(Integer.parseInt(parts[7].trim()));
            return student;
        } else {
            Professor professor = new Professor(
                    parts[1].trim(),
                    parts[2].trim(),
                    role
            );
            professor.setAge(Integer.parseInt(parts[4].trim()));
            professor.setEmail(parts[5].isEmpty() ? null : parts[5].trim());
            professor.setSchool(parts[6].trim());
            professor.setExperience(Integer.parseInt(parts[7].trim()));
            return professor;
        }
    }

    private static Map<String, String> parseProperties(String str) {
        return Arrays.stream(str.split(", "))
                .collect(Collectors.toMap(
                        kv -> kv.split("=")[0],
                        kv -> kv.split("=")[1]
                ));
    }

    private static Group findOrCreateGroup(long museumCode, String timetable) {
        return database.getGroups().stream()
                .filter(g -> g.getMuseumCode() == museumCode && g.getTimetable().equals(timetable))
                .findFirst()
                .orElseGet(() -> {
                    Group newGroup = new Group();
                    newGroup.setMuseumCode(museumCode);
                    newGroup.setTimetable(timetable);
                    newGroup.resetGuide();
                    database.addGroup(newGroup);
                    return newGroup;
                });
    }

    private static Group findGroup(long museumCode, String timetable) throws GroupNotExistsException {
        return database.getGroups().stream()
                .filter(g -> g.getMuseumCode() == museumCode && g.getTimetable().equals(timetable))
                .findFirst()
                .orElseThrow(() -> new GroupNotExistsException("Group not found"));
    }

    private static String formatOutput(String museumCode, String timetable, String message) {
        return museumCode + ": " + timetable + " ## " + message;
    }

    private static String formatExceptionOutput(String[] parts, Exception e) {
        return parts.length >= 3 ?
                parts[1] + " ## " + parts[2] + " ## " + e.getMessage() :
                "ERROR: " + e.getMessage();
    }

    private static List<String> readLines(String path) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) lines.add(line);
        }
        return lines;
    }
}

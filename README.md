# Tourvault
Java back-end for a tourist application that manages museums, tourist groups, and guide scheduling via a file-based command system. Features Singleton, Factory Method, Command, and Observer design patterns with full custom exception handling.

## Overview

This project implements the server-side logic of a tourist application. It supports dynamic creation and management of museums, tourist groups, and guided visits, as well as an event notification system that broadcasts museum announcements to registered guides.

Data is loaded from structured CSV-like input files and processed through a command interpreter, with results written to corresponding output files for validation.

---

## Architecture & Design

### Class Structure

```
├── Main                          # Entry point: argument parsing, file I/O, command dispatch
├── Database                      # Singleton data store (museums, groups)
├── Museum                        # Museum entity with location and optional metadata
├── Location                      # Geographic/administrative data for a museum
├── Person                        # Base class for all people in the system
│   ├── Student                   # Visitor subclass (school, studyYear)
│   └── Professor                 # Guide/visitor subclass (school, experience)
├── Group                         # Tourist group (guide + up to 10 members)
├── PathTypes                     # Enum for differentiating input file types
└── exceptions/
    ├── GroupNotExistsException
    ├── GroupThresholdException
    ├── GuideExistsException
    ├── GuideTypeException
    └── PersonNotExistsException
```

---

## Core Features

### Commands Supported

| Command | Description |
|---------|-------------|
| `ADD MUSEUM` | Parses and registers a museum entity from input data |
| `ADD GUIDE` | Assigns a professor as guide to a group; creates group if absent |
| `FIND GUIDE` | Looks up the guide assigned to a specific group |
| `REMOVE GUIDE` | Removes the current guide from a group |
| `ADD MEMBER` | Adds a student or professor to a group (max 10 members) |
| `FIND MEMBER` | Searches for a specific member within a group |
| `REMOVE MEMBER` | Removes a member from a group |
| `ADD EVENT` | Notifies all guides registered to a museum of a new event |

### Input / Output

- Input files follow a CSV-like format using `|` as a separator
- Each command type maps to its own set of input/output files (`museums_xx.in` → `museums_xx.out`, etc.)
- The `main` method accepts either **2 parameters** (single file mode) or **4 parameters** (multi-file mode covering museums, groups, and events)

---

## OOP Concepts Applied

- **Inheritance** — `Student` and `Professor` both extend `Person`, adding role-specific fields while sharing base attributes and constructor logic
- **Polymorphism** — group members are stored as `List<Person>`, allowing both students and professors to coexist; type-specific logic is resolved at runtime
- **Encapsulation** — all fields are `private` with access through getters/setters; internal database state is never directly exposed
- **Exception Handling** — five custom exceptions model domain-specific error cases cleanly, each with a standardised message format

---

## Design Patterns

### 1. Singleton — `Database`
The `Database` class is instantiated once and accessed globally through a static method. This ensures a single consistent source of truth for all museums and groups across the application, avoiding data duplication or state conflicts.

### 2. Factory Method — `parsePerson`, `parseMuseum`, `parseProfessor`
Object creation is centralised in dedicated parse methods in `Main`. Rather than scattering `new Student(...)` or `new Museum(...)` calls throughout the command handler, construction logic lives in one place — making it straightforward to extend or modify without touching the command dispatch logic.

### 3. Command — `processCommands`
The command interpreter uses a `switch` statement on the first token of each input line to dispatch to the appropriate handler. This separates *what* to do from *how* to do it, keeps each case focused on a single responsibility, and makes it easy to add new commands without restructuring existing ones.

### 4. Observer — `ADD EVENT` / `notifyGuides`
When a museum adds an event, it notifies all guides currently registered to its groups. The museum acts as the subject and the guides act as observers. This decouples event sources from recipients — adding or removing guides from groups automatically affects who gets notified, without changing the notification logic itself.

---

## Exception Handling

| Exception | Trigger Condition |
|-----------|-------------------|
| `GroupNotExistsException` | Operation targets a group that hasn't been created yet |
| `GroupThresholdException` | Adding a member to a group that already has 10 members |
| `GuideExistsException` | Attempting to assign a guide when one is already set |
| `GuideTypeException` | Attempting to assign a student as a guide |
| `PersonNotExistsException` | Removing a member who isn't in the group |

Exceptions thrown by `Group` and `Database` are caught in `Main` and written to the relevant output file in a standardised format.

---

## Project Structure

```
src/
└── main/
    ├── java/
    │   └── org/example/
    │       ├── Main.java
    │       ├── Database.java
    │       ├── Museum.java
    │       ├── Location.java
    │       ├── Person.java
    │       ├── Student.java
    │       ├── Professor.java
    │       ├── Group.java
    │       ├── PathTypes.java
    │       └── exceptions/
    │           ├── GroupNotExistsException.java
    │           ├── GroupThresholdException.java
    │           ├── GuideExistsException.java
    │           ├── GuideTypeException.java
    │           └── PersonNotExistsException.java
    └── resources/
        ├── museums_01.in / museums_01.out
        ├── groups_01.in  / groups_01.out
        └── events_01.in  / events_01.out
```

---

## Getting Started

### Build & Run

```bash
# Clone the repository
git clone https://github.com/<your-username>/tourist-app-backend.git
cd tourist-app-backend

# Build
./gradlew build

# Run — single file mode
./gradlew run --args="MUSEUMS path/to/museums_01"

# Run — multi-file mode
./gradlew run --args="MUSEUMS path/to/museums path/to/groups path/to/events"
```

### Input Format

Each line in an input file follows the pattern:

```
COMMAND | param1 | param2 | ...
```

The first line of each file is a header describing the columns and is automatically skipped by the parser.

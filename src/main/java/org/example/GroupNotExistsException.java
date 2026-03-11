package org.example;
public class GroupNotExistsException extends Exception {
    public GroupNotExistsException() {
        super("Group does not exist.");
    }

    public GroupNotExistsException(String message) {
        super(message);
    }
}

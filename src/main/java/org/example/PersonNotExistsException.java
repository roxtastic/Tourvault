package org.example;
public class PersonNotExistsException extends Exception {
    public PersonNotExistsException(String message) {
        super(message);
    }
}

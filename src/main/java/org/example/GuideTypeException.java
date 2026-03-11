package org.example;
public class GuideTypeException extends Exception {
    public GuideTypeException() {
        super("Guide must be a professor.");
    }

    public GuideTypeException(String message) {
        super(message);
    }
}
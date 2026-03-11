package org.example;

import java.util.Objects;

public class Person {
    private String surname;
    private String name;
    private String role;
    private String email;
    private Integer age;

    public Person(String surname, String name, String role) {
        this.surname = surname;
        this.name = name;
        this.role = role;
    }
    public String getSurname() { return surname; }
    public String getName() { return name; }
    public String getRole() { return role; }
    public void setSurname(String surname) { this.surname = surname; }
    public void setName(String name) { this.name = name; }
    public void setRole(String role) { this.role = role; }
    public String getEmail(){ return email; }
    public void setEmail(String email){this.email = email;}
    public Integer getAge() { return age;}
    public void setAge(Integer age) {this.age = age;}
    @Override
    public String toString() {
        return String.format(
                "surname=%s, name=%s, role=%s, age=%d, email=%s",
                surname, name, role, age, (email == null ? "null" : email)
        );
    }
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Person person = (Person) o;
        return age == person.age &&
                Objects.equals(surname, person.surname) &&
                Objects.equals(name, person.name) &&
                Objects.equals(role, person.role) &&
                Objects.equals(email, person.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(surname, name, role, age, email);
    }

    public char[] getSchool() {
    }
}

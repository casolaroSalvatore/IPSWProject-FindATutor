package logic.model.domain;

import java.time.LocalDate;

public class Student extends Account {

    private String institute;

    public Student(String email, String name, String surname, LocalDate birthday, String institute) {
        // Richiama il costruttore di Account
        super(email, "Student", name, surname, birthday);
        this.institute = institute;
    }

    public String getInstitute() { return institute; }
    public void setInstitute(String institute) { this.institute = institute; }

}

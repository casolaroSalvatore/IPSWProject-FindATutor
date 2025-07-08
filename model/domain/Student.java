package logic.model.domain;

import java.time.LocalDate;

// Rappresenta un account utente con ruolo di Studente.
// Estende la classe Account aggiungendo l'attributo specifico "institute".
public class Student extends Account {

    private String institute;

    public Student(String email, String name, String surname, LocalDate birthday, String institute) {
        super(email, "Student", name, surname, birthday);
        this.institute = institute;
    }

    public String getInstitute() { return institute; }
    public void setInstitute(String institute) { this.institute = institute; }

}

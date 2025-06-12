package logic.model.domain;

import logic.model.domain.state.TutoringSession;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Student extends Account {

    private String institute;

    // private List<TutoringSession> bookedSession;

    public Student(String email, String name, String surname, LocalDate birthday, String institute) {
        // Richiama il costruttore di Account
        super(email, "Student", name, surname, birthday);
        this.institute = institute;
        // Inizializza la lista di sessioni prenotate
        // this.bookedSession = new ArrayList<>();
    }

    /* public List<TutoringSession> getBookedSession() {
        return bookedSession;
    } */

    public String getInstitute() { return institute; }
    public void setInstitute(String institute) { this.institute = institute; }

}

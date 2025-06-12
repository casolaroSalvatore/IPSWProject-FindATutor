package logic.model.domain;

import java.time.LocalDate;

public class Tutor extends Account {

    private String educationalTitle;
    private String location;
    private Availability availability;
    private String subject;
    private float hourlyRate;
    private float rating;
    private boolean offersInPerson;
    private boolean offersOnline;
    private boolean offersGroup;
    private boolean firstLessonFree;

    public Tutor (String email) {
        super(email, "Tutor");
        this.subject = null;
        this.location = null;
        this.availability = null;
        this.hourlyRate = 0;
    }

    public Tutor(String email, String name, String surname, LocalDate birthday,
                 String educationalTitle, String location, Availability availability, String subject, float hourlyRate,
                 boolean offersInPerson, boolean offersOnline, boolean offersGroup, boolean firstLessonFree) {
        super(email, "Tutor", name, surname, birthday);
        this.educationalTitle = educationalTitle;
        this.location = location;
        this.availability = availability;
        this.subject = subject;
        this.hourlyRate = hourlyRate;
        this.offersInPerson = offersInPerson;
        this.offersOnline = offersOnline;
        this.offersGroup = offersGroup;
        this.firstLessonFree = firstLessonFree;
    }

    public String getEducationalTitle() { return educationalTitle; }
    public void setEducationalTitle(String educationalTitle) { this.educationalTitle = educationalTitle; }

    public String getLocation() {
        return location;
    }

    public Availability getAvailability() {
        return availability;
    }

    public void setAvailability(Availability availability) {
        this.availability = availability;
    }

    public String getSubject() {
        return subject;
    }

    public float getHourlyRate() { return this.hourlyRate; }

    public float getRating() { return this.rating; }

    public void setRating(float rating) { this.rating = rating; }

    public boolean offersInPerson() { return offersInPerson; }

    public boolean offersOnline() { return offersOnline; }

    public boolean offersGroup() { return offersGroup; }

    public boolean isFirstLessonFree() { return firstLessonFree; }

}

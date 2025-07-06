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

    private Tutor(Builder builder) {
        super(builder.email, "Tutor", builder.name, builder.surname, builder.birthday);
        this.educationalTitle = builder.educationalTitle;
        this.location = builder.location;
        this.availability = builder.availability;
        this.subject = builder.subject;
        this.hourlyRate = builder.hourlyRate;
        this.offersInPerson = builder.offersInPerson;
        this.offersOnline = builder.offersOnline;
        this.offersGroup = builder.offersGroup;
        this.firstLessonFree = builder.firstLessonFree;
    }

    public static class Builder {
        private String email;
        private String name;
        private String surname;
        private LocalDate birthday;
        private String educationalTitle;
        private String location;
        private Availability availability;
        private String subject;
        private float hourlyRate;
        private boolean offersInPerson;
        private boolean offersOnline;
        private boolean offersGroup;
        private boolean firstLessonFree;

        public Builder(String email) {
            this.email = email;
        }

        public Builder name(String name) { this.name = name; return this; }
        public Builder surname(String surname) { this.surname = surname; return this; }
        public Builder birthday(LocalDate birthday) { this.birthday = birthday; return this; }
        public Builder educationalTitle(String educationalTitle) { this.educationalTitle = educationalTitle; return this; }
        public Builder location(String location) { this.location = location; return this; }
        public Builder availability(Availability availability) { this.availability = availability; return this; }
        public Builder subject(String subject) { this.subject = subject; return this; }
        public Builder hourlyRate(float hourlyRate) { this.hourlyRate = hourlyRate; return this; }
        public Builder offersInPerson(boolean offersInPerson) { this.offersInPerson = offersInPerson; return this; }
        public Builder offersOnline(boolean offersOnline) { this.offersOnline = offersOnline; return this; }
        public Builder offersGroup(boolean offersGroup) { this.offersGroup = offersGroup; return this; }
        public Builder firstLessonFree(boolean firstLessonFree) { this.firstLessonFree = firstLessonFree; return this; }

        public Tutor build() {
            return new Tutor(this);
        }
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

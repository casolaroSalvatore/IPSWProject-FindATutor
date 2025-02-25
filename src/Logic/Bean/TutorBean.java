package Logic.Bean;

import java.util.List;

public class TutorBean {

    private String tutorId;
    private String name;
    private String location;
    private String subject;
    private String availability;

    public String getTutorId() {
        return tutorId;
    }

    public void setTutorId(String tutorId) {
        this.tutorId = tutorId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    /* public boolean isLocationValid(String location) {

    } */

    public String getSubject() {
        return this.subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    /* public boolean isSubjectValid(String subject) {

    }

    public List<String> getAvailabilty() {

    }

    public void setAvailability(List<String>) {

    }

    public boolean isAvailabilityValid(List<String>) {

    } */

}
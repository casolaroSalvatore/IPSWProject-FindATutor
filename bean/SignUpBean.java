package logic.bean;

import logic.model.domain.Availability;

import java.time.LocalDate;
import java.util.regex.Pattern;

public class SignUpBean {

    private String id;
    private String username;
    private String password;
    private String confirmPassword;
    private String email;
    private String role;
    private String name;
    private String surname;
    private LocalDate birthday;
    private String institute;
    private String educationalTitle;
    private String profilePicturePath;
    private String profileComment;

    // Parametri necessari per i Tutor
    private String location;
    private AvailabilityBean availabilityBean;
    private String subject;
    private float hourlyRate;
    private boolean offersInPerson;
    private boolean offersOnline;
    private boolean offersGroup;
    private boolean firstLessonFree;


    public SignUpBean() {}

    public SignUpBean(String username, String email, String password, String role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) { this.username = username; }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) { this.email = email; }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) { this.password = password; }

    public String getConfirmPassword() { return confirmPassword; }

    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }

    public String getRole() {
        return role;
    }

    public void setRole(String role) { this.role = role; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

    public LocalDate getBirthday() { return birthday; }
    public void setBirthday(LocalDate birthday) { this.birthday = birthday; }

    public String getProfilePicturePath() {
        return profilePicturePath;
    }

    public void setProfilePicturePath(String profilePicturePath) {
        this.profilePicturePath = profilePicturePath;
    }

    public String getProfileComment() {
        return profileComment;
    }

    public void setProfileComment(String profileComment) {
        this.profileComment = profileComment;
    }

    public String getLocation() { return location; }

    public void setLocation(String location) {this.location = location; }

    public AvailabilityBean getAvailabilityBean() { return availabilityBean; }
    public void setAvailabilityBean(AvailabilityBean availabilityBean) { this.availabilityBean = availabilityBean; }

    public String getSubject() { return this.subject; }

    public void setSubject(String subject) {this.subject = subject; }

    public float getHourlyRate() { return this.hourlyRate; }

    public void setHourlyRate(float hourlyRate) { this.hourlyRate = hourlyRate; }

    public boolean getOffersInPerson() { return offersInPerson; }
    public void setOffersInPerson(boolean val) { this.offersInPerson = val; }

    public boolean getOffersOnline() { return offersOnline; }
    public void setOffersOnline(boolean val) { this.offersOnline = val; }

    public boolean getOffersGroup() { return offersGroup; }
    public void setOffersGroup(boolean val) { this.offersGroup = val; }

    public boolean getFirstLessonFree() { return firstLessonFree; }
    public void setFirstLessonFree(boolean val) { this.firstLessonFree = val; }

    public String getEducationalTitle() { return educationalTitle; }
    public void setEducationalTitle(String educationalTitle) { this.educationalTitle = educationalTitle; }

    public String getInstitute() { return institute; }
    public void setInstitute(String institute) { this.institute = institute; }

    // Validazione sintattica della e-mail (tramite l'utilizzo di un Pattern), password e del costo orario inserito
    private static final Pattern MAIL_RX = Pattern.compile(".+@.+\\..+");

    public void checkSyntax() {
        if (email == null || !MAIL_RX.matcher(email).matches()) {
            throw new IllegalArgumentException("Invalid e-mail address.");
        }
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters.");
        }
        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("Passwords do not match.");
        }
        if (hourlyRate < 0f)
            throw new IllegalArgumentException("Hourly rate cannot be negative.");
    }
}

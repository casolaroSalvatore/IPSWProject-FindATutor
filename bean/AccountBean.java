package logic.bean;

import logic.model.domain.Account;

import java.time.LocalDate;
import java.time.Period;

public class AccountBean {

    private String accountId;
    private String role;
    private String password;
    private String confirmPassword;
    private String name;
    private String surname;
    private LocalDate birthday;
    private String profilePicturePath;
    private String profileComment;

    // Attributi specifici per studenti
    private String institute;

    // Attributi specifici per tutor
    private String location;
    private AvailabilityBean availabilityBean;
    private String subject;
    private String educationalTitle;
    private float hourlyRate;
    private boolean offersInPerson;
    private boolean offersOnline;
    private boolean offersGroup;
    private boolean firstLessonFree;

    public AccountBean() {}

    public AccountBean(Account account) {
        this.accountId = account.getAccountId();
        this.role = account.getRole();
        this.password = account.getPassword();
        this.name = account.getName();
        this.surname = account.getSurname();
        this.birthday = account.getBirthday();
        this.profilePicturePath = account.getProfilePicturePath();
        this.profileComment = account.getProfileComment();
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

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

    public String getInstitute() {
        return institute;
    }

    public void setInstitute(String institute) {
        this.institute = institute;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public AvailabilityBean getAvailabilityBean() {
        return availabilityBean;
    }

    public void setAvailabilityBean(AvailabilityBean availabilityBean) {
        this.availabilityBean = availabilityBean;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getEducationalTitle() {
        return educationalTitle;
    }

    public void setEducationalTitle(String educationalTitle) {
        this.educationalTitle = educationalTitle;
    }

    public float getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(float hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public boolean isOffersInPerson() {
        return offersInPerson;
    }

    public void setOffersInPerson(boolean offersInPerson) {
        this.offersInPerson = offersInPerson;
    }

    public boolean isOffersOnline() {
        return offersOnline;
    }

    public void setOffersOnline(boolean offersOnline) {
        this.offersOnline = offersOnline;
    }

    public boolean isOffersGroup() {
        return offersGroup;
    }

    public void setOffersGroup(boolean offersGroup) {
        this.offersGroup = offersGroup;
    }

    public boolean isFirstLessonFree() {
        return firstLessonFree;
    }

    public void setFirstLessonFree(boolean firstLessonFree) {
        this.firstLessonFree = firstLessonFree;
    }

    // Validazione sintattica e semantica incapsulata
    public void checkBasicSyntax() {
        if (name == null || !name.matches("[A-Za-z\\u00C0-\\u00D6\\u00D8-\\u00F6\\u00F8-\\u00FF' \\-]{2,30}")) {
            throw new IllegalArgumentException("Name must be alphabetic (2–30 chars).");
        }
        if (surname == null || !surname.matches("[A-Za-z\\u00C0-\\u00D6\\u00D8-\\u00F6\\u00F8-\\u00FF' \\-]{2,30}")) {
            throw new IllegalArgumentException("Surname must be alphabetic (2–30 chars).");
        }
        if (birthday == null || birthday.isAfter(LocalDate.now()) ||
                Period.between(birthday, LocalDate.now()).getYears() < 13)
            throw new IllegalArgumentException("Invalid birth date (min 13 years).");
        if (role == null || role.isBlank())
            throw new IllegalArgumentException("Role required.");
        if (profileComment.length() > 250){
            throw new IllegalArgumentException("Comment max 250 characters.");
        }
        if (profilePicturePath != null
                && !profilePicturePath.isEmpty()
                && !profilePicturePath.matches(".*\\.(png|jpg|jpeg)$")) {

            throw new IllegalArgumentException("Profile picture must be PNG/JPG.");
        }
    }

    public void checkPasswordSyntax() {
        if (password == null || password.isBlank())
            throw new IllegalArgumentException("Password is required.");
        // ≥10 caratteri con almeno 1 maiuscola, 1 minuscola, 1 cifra, 1 simbolo; no spazi
        if (!password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d\\s]).{10,}$"))
            throw new IllegalArgumentException(
                    "Password ≥10 chars with upper, lower, digit & symbol – no spaces.");
        if (password.contains(" "))
            throw new IllegalArgumentException("Password cannot contain spaces.");
        if (confirmPassword != null && !password.equals(confirmPassword))
            throw new IllegalArgumentException("Password and confirmation do not match.");
    }

    // STUDENT-specific checks
    public void checkStudentSyntax() {
        checkBasicSyntax();
        checkPasswordSyntax();
        if (institute == null || institute.isBlank())
            throw new IllegalArgumentException("Institute is required for students.");
        if (!institute.matches("[A-Za-z0-9' .-]{2,50}"))
            throw new IllegalArgumentException("Institute name is invalid.");
    }

    // TUTOR-specific checks
    public void checkTutorSyntax() {
        checkBasicSyntax();
        checkPasswordSyntax();
        if (availabilityBean == null)
            throw new IllegalArgumentException("Availability must be specified.");
        availabilityBean.checkSyntax();  // reuses existing checks
        if (subject == null || subject.isBlank())
            throw new IllegalArgumentException("Subject is required for tutors.");
        if (educationalTitle == null || educationalTitle.isBlank())
            throw new IllegalArgumentException("Educational title is required.");
        if (hourlyRate <= 0)
            throw new IllegalArgumentException("Hourly rate must be positive.");
        if (location == null || location.isBlank())
            throw new IllegalArgumentException("Location is required for tutors.");
        if (hourlyRate < 5 || hourlyRate > 200)
            throw new IllegalArgumentException("Hourly rate must be between 5$ and 200$.");
    }
}


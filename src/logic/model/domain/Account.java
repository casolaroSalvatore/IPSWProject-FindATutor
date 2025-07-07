package logic.model.domain;

import java.time.LocalDate;
import java.time.Period;

public class Account {

    // Identificatore univoco dell'account: formato come "email_ruolo"
    private String accountId;

    // Email dell’utente a cui è associato l’account
    private String email;

    // Ruolo dell'account: ad esempio "Student" o "Tutor"
    private String role;

    private String password;
    private String name;
    private String surname;
    private LocalDate birthday;
    private String profilePicturePath;
    private String profileComment;

    public Account(String email, String role) {
        this.accountId = email + "_" + role;
        this.email = email;
        this.role = role;
    }

    public Account(String email, String role, String name, String surname, LocalDate birthday) {
        this(email, role, name, surname, birthday, null, null);
    }

    public Account(String email, String role, String name, String surname, LocalDate birthday, String profilePicturePath, String profileComment) {
        this.accountId = email + "_" + role;
        this.email = email;
        this.role = role;
        this.name = name;
        this.surname = surname;
        this.birthday = birthday;
        this.profilePicturePath = profilePicturePath;
        this.profileComment = profileComment;
    }

    public String getAccountId() {
        return this.accountId;
    }

    public String getEmail() {
        return this.email;
    }

    public String getRole() {
        return this.role;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public int getAge() {
        if (birthday == null) return -1;
        return Period.between(birthday, LocalDate.now()).getYears();
    }

    public String getProfilePicturePath() {
        return profilePicturePath;
    }

    public void setProfilePicturePath(String path) {
        this.profilePicturePath = path;
    }

    public String getProfileComment() {
        return profileComment;
    }

    public void setProfileComment(String comment) {
        this.profileComment = comment;
    }
}

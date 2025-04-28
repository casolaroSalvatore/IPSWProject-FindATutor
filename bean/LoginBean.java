package logic.bean;

import java.util.regex.Pattern;

public class LoginBean {

    private String email;
    private String password;
    private String selectedRole;

    public LoginBean(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // Validazione incapsulata
    private static final Pattern MAIL_RX = Pattern.compile(".+@.+\\..+");
    public void checkSyntax() {
        if (email == null || !MAIL_RX.matcher(email).matches())
            throw new IllegalArgumentException("Malformed e-mail.");
        if (password == null || password.isBlank())
            throw new IllegalArgumentException("Password required.");
    }

    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getSelectedRole() { return selectedRole; }
    public void setSelectedRole(String selectedRole) { this.selectedRole = selectedRole; }
}


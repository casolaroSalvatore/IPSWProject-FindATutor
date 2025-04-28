package logic.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class UserBean {

    private String email;
    private String username;
    private String password;
    private String confirmPassword;
    // Non appartiene all'utente, ma ci servirà per l'autenticazione
    private String selectedRole;
    private List<AccountBean> accounts = new ArrayList<>();

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() { return password; }
    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getSelectedRole() {
        return selectedRole;
    }

    public void setSelectedRole(String selectedRole) {
        this.selectedRole = selectedRole;
    }

    public List<AccountBean> getAccounts() { return accounts; }
    public void setAccounts(List<AccountBean> accounts) { this.accounts = accounts; }

    public void addAccount(AccountBean accountBean) {
        accounts.add(accountBean);
    }

    // Validazione sintattica e semantica incapsulata
    private static final Pattern MAIL_RX = Pattern.compile(".+@.+\\..+");
    public void checkSyntax() {
        if (email == null || !MAIL_RX.matcher(email).matches())
            throw new IllegalArgumentException("Malformed e-mail.");
        if (password == null || password.isBlank())
            throw new IllegalArgumentException("Password is required.");
        if (!password.equals(confirmPassword))
            throw new IllegalArgumentException("Password and confirmation do not match.");
    }

    public void checkLoginSyntax() {
        if (email == null || !MAIL_RX.matcher(email).matches())
            throw new IllegalArgumentException("Malformed e-mail.");
        if (password == null || password.isBlank())
            throw new IllegalArgumentException("Password is required.");
    }
}

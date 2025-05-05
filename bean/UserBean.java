package logic.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class UserBean {

    private String email;
    private String username;
    private List<AccountBean> accounts = new ArrayList<>();

    // Pattern necessari per i controlli sintattici
    private static final Pattern EMAIL_PATTERN    = Pattern.compile("^[\\w.-]+@[\\w.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z\\d_]{3,}$");

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

    public List<AccountBean> getAccounts() { return accounts; }
    public void setAccounts(List<AccountBean> accounts) { this.accounts = accounts; }

    public void addAccount(AccountBean accountBean) {
        accounts.add(accountBean);
    }

    public void checkEmailSyntax() {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches())
            throw new IllegalArgumentException("Invalid e‑mail format.");
        if (email.endsWith("."))                        // dominio non può terminare con '.'
            throw new IllegalArgumentException("E‑mail domain cannot end with a dot.");
    }

    public void checkUsernameSyntax() {
        if (username == null || !USERNAME_PATTERN.matcher(username).matches())
            throw new IllegalArgumentException("Invalid username (≥3 chars, letters/digits/_).");
    }
}

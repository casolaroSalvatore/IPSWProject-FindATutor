package logic.model.domain;

import java.util.ArrayList;
import java.util.List;

// Rappresenta un utente registrato nel sistema, identificato dalla propria email.
// Ogni utente può avere più account associati, ciascuno con un ruolo specifico (es. "Student", "Tutor").
public class User {

    private final String email;
    private String username;
    List<Account> accounts = new ArrayList<>();

    public User(String email) {
        this.email = email;
    }

    public User(String email, String username) {
        this.email = email;
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public Account getAccount(String accountId) {
        for (Account account : accounts) {
            if (account.getAccountId().equals(accountId)) {
                return account;
            }
        }
        return null;
    }

    public boolean hasAccount(String role) {
        for (Account acc : accounts) {
            if (acc.getRole().equals(role)) {
                return true;
            }
        }
        return false;
    }

    public void addAccount(Account account) {
        accounts.add(account);
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }
}

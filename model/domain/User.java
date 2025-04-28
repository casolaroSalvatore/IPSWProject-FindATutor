package logic.model.domain;

import java.util.ArrayList;
import java.util.List;

public class User {

    public String email;
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

    public String getUsername() { return username; }

    public Account getAccount(String accountId) {
        for (Account account: accounts) {
            if(account.getAccountId().equals(accountId)) {
                return account;
            }
        } return null;
    }

    public boolean hasAccount(String role) {
        for (Account acc : accounts) {
            if (acc.getRole().equals(role)) {
                return true; // Se troviamo un account con il ruolo cercato, restituiamo true
            }
        }
        return false; // Nessun account con il ruolo trovato
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

package Logic.Model.Domain;

public class Account {

    private String accountId; // Identificatore univoco: email + ruolo
    private String email;     // Collegamento all'utente
    private String role;      // Es. "Student" o "Tutor"

    public Account(String email, String role) {
        this.accountId = generateAccountId(email, role);
        this.email = email;
        this.role = role;
    }

    private String generateAccountId(String email, String role) {
        return email + "_" + role.toLowerCase(); // Es. mario.rossi@example.com_student
    }

    public String getAccountId() { return this.accountId; }
    public String getEmail() { return this.email; }
    public String getRole() { return this.role; }
}

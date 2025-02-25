package Logic.Bean;

public class SignUpBean {

    private String username;
    private String password;
    private String email;
    private String role;
    private String subject; // Solo per i tutor

    public SignUpBean(String username, String email, String password, String role, String subject) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
        this.subject = subject;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    public String getSubject() {
        return subject;
    }
}

package logic.bean;

// SharedReviewBean è un DTO che rappresenta i dati di una recensione condivisa tra studente e tutor.
// Trasporta le informazioni tra controller e view senza accedere al dominio.
public class SharedReviewBean {

    private String reviewId;
    private String studentId;
    private String tutorId;
    // Ci consente di far si che, quando si clicca sul profilo dello Student/Tutor, questo appaia a schermo
    private AccountBean counterpartAccount;
    // Ci consente di mostrare le informazioni relative al tutor, ovvero la materia e la locazione
    private AccountBean tutorAccount;

    private int studentStars;
    private String studentTitle;
    private String studentComment;
    private boolean studentSubmitted;

    private String tutorTitle;
    private String tutorComment;
    private boolean tutorSubmitted;

    private static final int TITLE_MAX = 60;
    private static final int COMMENT_MAX = 300;
    private static final String CHARS = "chars.";

    private ReviewStatus status;

    // Enum indicante il ruolo del sender della recensione
    private SenderRole senderRole;

    private String counterpartyInfo;

    public enum SenderRole {STUDENT, TUTOR}
    public enum ReviewStatus { NOT_STARTED, PENDING, COMPLETE }

    public SharedReviewBean() {
        // Costruttore dello SharedReviewBean (per eliminare l'errore su SonarQube)
    }

    public String getReviewId() {
        return reviewId;
    }

    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getTutorId() {
        return tutorId;
    }

    public void setTutorId(String tutorId) {
        this.tutorId = tutorId;
    }

    public AccountBean getCounterpartAccount() {
        return counterpartAccount;
    }

    public void setCounterpartAccount(AccountBean counterpartAccount) {
        this.counterpartAccount = counterpartAccount;
    }

    public AccountBean getTutorAccount() {
        return tutorAccount;
    }

    public void setTutorAccount(AccountBean tutorAccount) {
        this.tutorAccount = tutorAccount;
    }

    public int getStudentStars() {
        return studentStars;
    }

    public void setStudentStars(int studentStars) {
        this.studentStars = studentStars;
    }

    public String getStudentTitle() {
        return studentTitle;
    }

    public void setStudentTitle(String studentTitle) {
        this.studentTitle = studentTitle;
    }

    public String getStudentComment() {
        return studentComment;
    }

    public void setStudentComment(String comment) {
        this.studentComment = comment;
    }

    public SenderRole getSenderRole() {
        return senderRole;
    }

    public void setSenderRole(SenderRole senderRole) {
        this.senderRole = senderRole;
    }

    public ReviewStatus getStatus() {
        return status;
    }

    public void setStatus(ReviewStatus status) {
        this.status = status;
    }

    public boolean isStudentSubmitted() {
        return studentSubmitted;
    }

    public void setStudentSubmitted(boolean studentSubmitted) {
        this.studentSubmitted = studentSubmitted;
    }

    public boolean isTutorSubmitted() {
        return tutorSubmitted;
    }

    public void setTutorSubmitted(boolean tutorSubmitted) {
        this.tutorSubmitted = tutorSubmitted;
    }

    public String getCounterpartyInfo() {
        return counterpartyInfo;
    }

    public void setCounterpartyInfo(String counterpartyInfo) {
        this.counterpartyInfo = counterpartyInfo;
    }

    public String getTutorTitle() {
        return tutorTitle;
    }

    public void setTutorTitle(String tutorTitle) {
        this.tutorTitle = tutorTitle;
    }

    public String getTutorComment() {
        return tutorComment;
    }

    public void setTutorComment(String tutorComment) {
        this.tutorComment = tutorComment;
    }


    // Effettua i controlli sintattici in base al ruolo del mittente della recensione
    public void checkSyntax() {
        if (senderRole == null) {
            throw new IllegalArgumentException("SenderRole must be specified.");
        }
        if (senderRole == SenderRole.STUDENT) {
            validateStudentFields();
        } else {
            validateTutorFields();
        }
    }

    // Validazione dei dati specifici dello studente (sintassi titolo, commento, stelle)
    private void validateStudentFields() {
        if (studentStars < 1 || studentStars > 5) {
            throw new IllegalArgumentException("Stars must be 1‑5.");
        }

        if (studentTitle == null || studentTitle.isBlank() || studentTitle.length() > TITLE_MAX) {
            throw new IllegalArgumentException("Student title ≤ " + TITLE_MAX + CHARS);
        }

        if (studentComment == null || studentComment.isBlank() || studentComment.length() > COMMENT_MAX) {
            throw new IllegalArgumentException("Student comment 1‑" + COMMENT_MAX + CHARS);
        }
    }

    // Validazione dei dati specifici del tutor (sintassi titolo, commento)
    private void validateTutorFields() {
        if (tutorTitle == null || tutorTitle.isBlank() || tutorTitle.length() > TITLE_MAX) {
            throw new IllegalArgumentException("Tutor title ≤ " + TITLE_MAX + CHARS);
        }

        if (tutorComment == null || tutorComment.isBlank() || tutorComment.length() > COMMENT_MAX) {
            throw new IllegalArgumentException("Tutor comment 1‑" + COMMENT_MAX + CHARS);
        }
    }
}


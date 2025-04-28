package logic.bean;

import logic.model.domain.ReviewStatus;
import logic.model.domain.SharedReview;

public class SharedReviewBean {

    private String reviewId;
    private String studentId;
    private String tutorId;
    // Ci consente di far si che, quando si clicca sul profilo dello Student/Tutor, questo appaia a schermo
    private AccountBean counterpartAccount;
    // Ci consente di mostrare le informazioni relative al tutor, ovvero la materia e la locazione
    private AccountBean tutorAccount;

    private int    studentStars;
    private String studentTitle;
    private String studentComment;
    private boolean studentSubmitted;

    private String tutorTitle;
    private String tutorComment;
    private boolean tutorSubmitted;

    private ReviewStatus status;
    private SenderRole senderRole;
    private String counterpartyInfo;

    public SharedReviewBean() { }

    public SharedReviewBean(SharedReview sr, String counterpartInfo){
        this.reviewId = sr.getReviewId();
        this.studentId     = sr.getStudentId();
        this.tutorId = sr.getTutorId();
        this.studentStars    = sr.getStudentStars();
        this.studentComment = sr.getStudentComment();
        this.studentSubmitted = sr.isStudentSubmitted();
        this.studentTitle = sr.getStudentTitle();
        this.tutorTitle = sr.getTutorTitle();
        this.tutorComment = sr.getTutorComment();
        this.tutorSubmitted   = sr.isTutorSubmitted();
        this.status   = sr.getStatus();
        this.counterpartyInfo = counterpartInfo;
    }

    // Validazione della forma dei dati
    public void checkSyntax() {
        if (senderRole == null) {
            throw new IllegalArgumentException("SenderRole must be specified.");
        }

        if (senderRole == SenderRole.STUDENT) {
            if (studentStars < 1 || studentStars > 5)
                throw new IllegalArgumentException("Stars must be between 1 and 5.");
            if (studentTitle == null || studentTitle.isBlank() || studentTitle.length() > 60)
                throw new IllegalArgumentException("Student Title required (≤60 chars)");
            if (studentComment == null || studentComment.isBlank())
                throw new IllegalArgumentException("Student Comment cannot be empty");
        } else if (senderRole == SenderRole.TUTOR) {
            if (tutorTitle == null || tutorTitle.isBlank() || tutorTitle.length() > 60)
                throw new IllegalArgumentException("Tutor Title required (≤60 chars)");
            if (tutorComment == null || tutorComment.isBlank())
                throw new IllegalArgumentException("Tutor Comment cannot be empty");
        }
    }


    // Enum indicante il ruolo del sender della recensione
    public enum SenderRole { STUDENT, TUTOR }

    public String getReviewId() {
        return reviewId;
    }
    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
    }

    public String getStudentId()               { return studentId; }
    public void setStudentId(String studentId)      { this.studentId = studentId; }

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

    public AccountBean getTutorAccount() { return tutorAccount; }
    public void setTutorAccount(AccountBean tutorAccount) { this.tutorAccount = tutorAccount;}

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

    public boolean isStudentSubmitted() { return studentSubmitted; }
    public void setStudentSubmitted(boolean studentSubmitted){ this.studentSubmitted = studentSubmitted; }

    public boolean isTutorSubmitted()   { return tutorSubmitted; }
    public void setTutorSubmitted(boolean tutorSubmitted){ this.tutorSubmitted = tutorSubmitted; }

    public String getCounterpartyInfo() { return counterpartyInfo; }
    public void setCounterpartyInfo(String counterpartyInfo){ this.counterpartyInfo = counterpartyInfo; }

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

    // Utility tramite cui copia nel Model solo il lato del mittente
    public void copyToEntity(SharedReview sr){
        if (senderRole == SenderRole.STUDENT){
            sr.setStudentStars(studentStars);
            sr.setStudentTitle(studentTitle);
            sr.setStudentComment(studentComment);
            sr.setStudentSubmitted(true);
        }else{
            sr.setTutorTitle(tutorTitle);
            sr.setTutorComment(tutorComment);
            sr.setTutorSubmitted(true);
        }
    }
}


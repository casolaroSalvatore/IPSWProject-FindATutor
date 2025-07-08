package logic.model.domain;

public class SharedReview {

    private String reviewId;
    private String studentId;
    private String tutorId;

    // Parte dello studente
    private int studentStars;
    private String studentTitle;
    private String studentComment;
    // Flag per conoscere se lo studente ha già inviato la recensione
    private boolean studentSubmitted;

    // Parte del tutor
    private String tutorTitle;
    private String tutorComment;
    // Flag per conoscere se lo studente ha già inviato la recensione
    private boolean tutorSubmitted;

    // Stato della recensione
    private ReviewStatus status = ReviewStatus.NOT_STARTED;

    public SharedReview() { }

    public SharedReview(String reviewId) {
        this.reviewId = reviewId;
    }

    public SharedReview(String reviewId, String studentId, String tutorId) {
        this(reviewId);  // Chiama il costruttore sopra
        this.studentId = studentId;
        this.tutorId = tutorId;
    }

    public String getReviewId() { return reviewId; }
    public void setReviewId(String reviewId) { this.reviewId = reviewId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getTutorId() { return tutorId; }
    public void setTutorId(String tutorId) { this.tutorId = tutorId; }

    public int getStudentStars() { return studentStars; }
    public void setStudentStars(int studentStars) { this.studentStars = studentStars; }

    public String getStudentTitle() { return studentTitle; }
    public void setStudentTitle(String studentTitle) { this.studentTitle = studentTitle; }

    public String getStudentComment() { return studentComment; }
    public void setStudentComment(String studentComment) { this.studentComment = studentComment; }

    public boolean isStudentSubmitted() { return studentSubmitted; }
    public void setStudentSubmitted(boolean studentSubmitted) { this.studentSubmitted = studentSubmitted; }

    public String getTutorTitle() { return tutorTitle; }
    public void setTutorTitle(String tutorTitle) { this.tutorTitle = tutorTitle; }

    public String getTutorComment() { return tutorComment; }
    public void setTutorComment(String tutorComment) { this.tutorComment = tutorComment; }

    public boolean isTutorSubmitted() { return tutorSubmitted; }
    public void setTutorSubmitted(boolean tutorSubmitted) { this.tutorSubmitted = tutorSubmitted; }

    public ReviewStatus getStatus() { return status; }
    public void setStatus(ReviewStatus status) { this.status = status; }
}

package logic.bean;

import java.time.LocalDate;
import java.time.LocalTime;

// TutoringSessionBean è un Bean che trasporta i dati di una sessione di tutoraggio tra controller e view.
// Include i dati della sessione, eventuali proposte di modifica/cancellazione e metodi di validazione sintattica.
public class TutoringSessionBean {

    private String sessionId;
    private String tutorId;
    private String studentId;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String location;
    private String subject;
    private String comment;
    private String modificationReason;
    private String cancellationReason;

    // Necessaria per separare il Bean dal Model
    public enum TutoringSessionStatusBean {
        DRAFT,
        PENDING,
        ACCEPTED,
        REFUSED,
        MOD_REQUESTED,
        CANCEL_REQUESTED,
        CANCELLED
    }

    private TutoringSessionStatusBean status;

    private LocalDate proposedDate;
    private LocalTime proposedStartTime;
    private LocalTime proposedEndTime;

    private String modifiedBy;
    private String modifiedTo;

    // Getters & Setters
    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getTutorId() {
        return tutorId;
    }

    public void setTutorId(String tutorId) {
        this.tutorId = tutorId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public TutoringSessionStatusBean getStatus() {
        return status;
    }

    public void setStatus(TutoringSessionStatusBean status) {
        this.status = status;
    }

    public LocalDate getProposedDate() {
        return proposedDate;
    }

    public void setProposedDate(LocalDate proposedDate) {
        this.proposedDate = proposedDate;
    }

    public LocalTime getProposedStartTime() {
        return proposedStartTime;
    }

    public void setProposedStartTime(LocalTime proposedStartTime) {
        this.proposedStartTime = proposedStartTime;
    }

    public LocalTime getProposedEndTime() {
        return proposedEndTime;
    }

    public void setProposedEndTime(LocalTime proposedEndTime) {
        this.proposedEndTime = proposedEndTime;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public String getModifiedTo() {
        return modifiedTo;
    }

    public void setModifiedTo(String modifiedTo) {
        this.modifiedTo = modifiedTo;
    }

    // Validazione della forma dei dati
    public void checkSyntax() {
        checkRequiredFields();
        checkTimeValidity();
        checkOptionalTextFields();
    }

    private void checkRequiredFields() {
        if (tutorId == null || tutorId.isBlank()) {
            throw new IllegalArgumentException("TutorId empty");
        }
        if (studentId == null || studentId.isBlank()) {
            throw new IllegalArgumentException("StudentId empty");
        }
        if (date == null || date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Date must be today or later");
        }
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("Times required");
        }
        if (location == null || location.isBlank()) {
            throw new IllegalArgumentException("Location required");
        }
        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("Subject required");
        }
    }

    private void checkTimeValidity() {
        if (startTime != null && endTime != null && !startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("Start ≥ End");
        }

        if (proposedStartTime != null && proposedEndTime != null &&
                !proposedStartTime.isBefore(proposedEndTime)) {
            throw new IllegalArgumentException("Proposed start ≥ end");
        }
    }

    private void checkOptionalTextFields() {
        if (comment != null && comment.length() > 300) {
            throw new IllegalArgumentException("Comment ≤ 300 chars");
        }

        if (modificationReason != null && modificationReason.length() > 300) {
            throw new IllegalArgumentException("Modification reason ≤ 300 chars");
        }

        if (cancellationReason != null && cancellationReason.length() > 300) {
            throw new IllegalArgumentException("Cancellation reason ≤ 300 chars");
        }
    }

    public void checkProposedTimes() {
        if (proposedDate == null || proposedDate.isBefore(LocalDate.now()))
            throw new IllegalArgumentException("Proposed date must be today or later");
        if (proposedStartTime == null || proposedEndTime == null)
            throw new IllegalArgumentException("Both proposed times required");
        if (!proposedStartTime.isBefore(proposedEndTime))
            throw new IllegalArgumentException("Proposed start ≥ end");
    }

}

package logic.model.domain;

import java.time.LocalDate;
import java.time.LocalTime;

public class TutoringSession {

    public String sessionId;
    public String tutorId;
    public String studentId;
    public String location;
    public String subject;
    public LocalDate date;
    public LocalTime startTime;
    public LocalTime endTime;
    public String comment;
    public String status;     // "PENDING", "ACCEPTED", "REFUSED", ...
    public String modifiedBy;
    public String modifiedTo;
    public LocalDate proposedDate;
    public LocalTime proposedStartTime;
    public LocalTime proposedEndTime;

    // Booleani per implementare l'animazione della notifica
    private boolean tutorSeen;
    private boolean studentSeen;

    public TutoringSession() {}

    public TutoringSession(String sessionId) { this.sessionId = sessionId; }

    public TutoringSession(String tutorId, String studentId, LocalDate date,
                           LocalTime startTime, LocalTime endTime,
                           String comment, String status) {
        this.tutorId = tutorId;
        this.studentId = studentId;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.comment = comment;
        this.status = status;
    }

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

    public String getComment() {
        return comment;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isTutorSeen() {
        return tutorSeen;
    }
    public void setTutorSeen(boolean tutorSeen) {
        this.tutorSeen = tutorSeen;
    }

    public boolean isStudentSeen() {
        return studentSeen;
    }
    public void setStudentSeen(boolean studentSeen) {
        this.studentSeen = studentSeen;
    }

    public LocalDate getProposedDate() {
        return proposedDate;
    }
    public void setProposedDate(LocalDate date) {
        this.proposedDate = date;
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

    public LocalTime getProposedStartTime() {
        return proposedStartTime;
    }
    public void setProposedStartTime(LocalTime startTime) {
        this.proposedStartTime = startTime;
    }
    public LocalTime getProposedEndTime() {
        return proposedEndTime;
    }
    public void setProposedEndTime(LocalTime endTime) {
        this.proposedEndTime = endTime;
    }
}


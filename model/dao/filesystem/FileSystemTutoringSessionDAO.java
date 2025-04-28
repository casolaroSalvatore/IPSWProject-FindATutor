package logic.model.dao.filesystem;

import logic.model.dao.TutoringSessionDAO;
import logic.model.domain.TutoringSession;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

class FileSystemTutoringSessionDAO extends FileSystemDAO<String, TutoringSession> implements TutoringSessionDAO {

    FileSystemTutoringSessionDAO(Path root) throws IOException { super(root,"sessions"); }

    @Override protected String getId(TutoringSession s) {
        if (s.getSessionId()==null || s.getSessionId().isBlank())
            s.setSessionId(UUID.randomUUID().toString());
        return s.getSessionId();
    }

    @Override protected List<String> encode(TutoringSession s) {
        return List.of(
                "sessionId:"        + s.getSessionId(),
                "tutorId:"          + s.getTutorId(),
                "studentId:"        + s.getStudentId(),
                "location:"         + nullSafe(s.getLocation()),
                "subject:"          + nullSafe(s.getSubject()),
                "date:"             + nullSafe(s.getDate()),
                "startTime:"        + nullSafe(s.getStartTime()),
                "endTime:"          + nullSafe(s.getEndTime()),
                "comment:"          + nullSafe(s.getComment()),
                "status:"           + nullSafe(s.getStatus()),
                "modifiedBy:"       + nullSafe(s.getModifiedBy()),
                "modifiedTo:"       + nullSafe(s.getModifiedTo()),
                "proposedDate:"     + nullSafe(s.getProposedDate()),
                "proposedStartTime:"+ nullSafe(s.getProposedStartTime()),
                "proposedEndTime:"  + nullSafe(s.getProposedEndTime()),
                "tutorSeen:"        + s.isTutorSeen(),
                "studentSeen:"      + s.isStudentSeen()
        );
    }

    @Override protected TutoringSession decode(List<String> l) {
        Map<String,String> m = toMap(l);
        TutoringSession s = new TutoringSession(m.get("sessionId"));
        s.setTutorId(m.get("tutorId"));
        s.setStudentId(m.get("studentId"));
        s.setLocation(m.get("location"));
        s.setSubject(m.get("subject"));
        s.setDate(parseDate(m.get("date")));
        s.setStartTime(parseTime(m.get("startTime")));
        s.setEndTime(parseTime(m.get("endTime")));
        s.setComment(m.get("comment"));
        s.setStatus(m.get("status"));
        s.setModifiedBy(m.get("modifiedBy"));
        s.setModifiedTo(m.get("modifiedTo"));
        s.setProposedDate(parseDate(m.get("proposedDate")));
        s.setProposedStartTime(parseTime(m.get("proposedStartTime")));
        s.setProposedEndTime(parseTime(m.get("proposedEndTime")));
        s.setTutorSeen(Boolean.parseBoolean(def(m.get("tutorSeen"),"false")));
        s.setStudentSeen(Boolean.parseBoolean(def(m.get("studentSeen"),"false")));
        return s;
    }

    @Override public List<TutoringSession> loadAllTutoringSession() { return scan(e->e); }

    /* helper */
    private Map<String,String> toMap(List<String> ls){ Map<String,String>m=new HashMap<>(); for(String s:ls){int i=s.indexOf(':'); if(i>0)m.put(s.substring(0,i),s.substring(i+1));} return m;}
    private LocalDate parseDate(String s){ return (s==null||s.isBlank())?null:LocalDate.parse(s); }
    private LocalTime parseTime(String s){ return (s==null||s.isBlank())?null:LocalTime.parse(s); }
    private String nullSafe(Object o){ return o==null?"":o.toString(); }
    private String def(String v,String d){ return v==null||v.isBlank()?d:v; }
}

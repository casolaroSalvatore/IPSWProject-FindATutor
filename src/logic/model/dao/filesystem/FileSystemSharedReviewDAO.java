package logic.model.dao.filesystem;

import logic.model.dao.SharedReviewDAO;
import logic.model.domain.ReviewStatus;
import logic.model.domain.SharedReview;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FileSystemSharedReviewDAO extends FileSystemDAO<String, SharedReview> implements SharedReviewDAO {

    private static FileSystemSharedReviewDAO instance;

    FileSystemSharedReviewDAO(Path root) throws IOException { super(root,"reviews"); }

    public static synchronized FileSystemSharedReviewDAO getInstance(Path root) throws IOException {
        if (instance == null) {
            instance = new FileSystemSharedReviewDAO(root);
        }
        return instance;
    }

    @Override protected String getId(SharedReview r) {
        if (r.getReviewId()==null || r.getReviewId().isBlank())
            r.setReviewId(UUID.randomUUID().toString());
        return r.getReviewId();
    }

    @Override protected List<String> encode(SharedReview r) {
        return List.of(
                "reviewId:"        + r.getReviewId(),
                "studentId:"       + r.getStudentId(),
                "tutorId:"         + r.getTutorId(),
                "studentStars:"    + r.getStudentStars(),
                "studentTitle:"    + nullSafe(r.getStudentTitle()),
                "studentComment:"  + nullSafe(r.getStudentComment()),
                "tutorTitle:"      + nullSafe(r.getTutorTitle()),
                "tutorComment:"    + nullSafe(r.getTutorComment()),
                "studentSubmitted:"+ r.isStudentSubmitted(),
                "tutorSubmitted:"  + r.isTutorSubmitted(),
                "status:"          + r.getStatus()
        );
    }

    @Override protected SharedReview decode(List<String> l) {
        Map<String,String> m = toMap(l);
        SharedReview r = new SharedReview(m.get("reviewId"), m.get("studentId"), m.get("tutorId"));
        r.setStudentStars(Integer.parseInt(def(m.get("studentStars"),"0")));
        r.setStudentTitle(m.get("studentTitle"));
        r.setStudentComment(m.get("studentComment"));
        r.setTutorTitle(m.get("tutorTitle"));
        r.setTutorComment(m.get("tutorComment"));
        r.setStudentSubmitted(Boolean.parseBoolean(def(m.get("studentSubmitted"),"false")));
        r.setTutorSubmitted  (Boolean.parseBoolean(def(m.get("tutorSubmitted"),"false")));
        r.setStatus(ReviewStatus.valueOf(m.get("status")));
        return r;
    }

    /* --- query --- */
    @Override public List<SharedReview> loadAll()                    { return scan(e->e); }
    @Override public List<SharedReview> loadForStudent(String sid)   { return filter(e->sid.equals(e.getStudentId())); }
    @Override public List<SharedReview> loadForTutor(String tid)     { return filter(e->tid.equals(e.getTutorId())); }

    private List<SharedReview> filter(java.util.function.Predicate<SharedReview> p){
        List<SharedReview> all = scan(e->e);
        all.removeIf(p.negate()); return all;
    }

    /* helper */
    private Map<String,String> toMap(List<String> ls){ Map<String,String>m=new HashMap<>(); for(String s:ls){int i=s.indexOf(':'); if(i>0)m.put(s.substring(0,i),s.substring(i+1));} return m;}
    private String nullSafe(Object o){ return o==null?"":o.toString(); }
    private String def(String v,String d){ return v==null||v.isBlank()?d:v; }
}

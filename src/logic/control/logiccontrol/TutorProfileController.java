package logic.control.logiccontrol;

import logic.bean.TutorBean;
import logic.bean.SharedReviewBean;
import logic.model.dao.DaoFactory;
import logic.model.dao.*;
import logic.model.domain.*;

import java.util.ArrayList;
import java.util.List;

public class TutorProfileController {

    private final AccountDAO accDao = DaoFactory.getInstance().getAccountDAO();
    private final SharedReviewDAO reviewDao = DaoFactory.getInstance().getSharedReviewDAO();

    // Carica un Tutor dal DAO e lo converte in TutorBean per la view
    public TutorBean loadTutorBean(String accountId){

        Account acc = accDao.load(accountId);
        if(!(acc instanceof Tutor t))
            throw new IllegalArgumentException("Not a tutor account: "+accountId);

        TutorBean b = new TutorBean();
        b.setAccountId(accountId);
        b.setName(t.getName());
        b.setSurname(t.getSurname());
        b.setHourlyRate(t.getHourlyRate());
        b.setSubject(t.getSubject());
        b.setLocation(t.getLocation());
        b.setRating(t.getRating());
        b.setEducationalTitle(t.getEducationalTitle());
        b.setProfilePicturePath(t.getProfilePicturePath());
        b.setProfileComment(t.getProfileComment());
        return b;
    }

    // Carica le recensioni completate per un tutor e le converte in SharedReviewBean
    public List<SharedReviewBean> loadCompletedReviews(String tutorId) {

        List<SharedReviewBean> out = new ArrayList<>();
        for (SharedReview r : reviewDao.loadForTutor(tutorId)) {

            if (r.getStatus() != ReviewStatus.COMPLETE) continue;

            SharedReviewBean b = new SharedReviewBean();
            b.setReviewId(r.getReviewId());
            b.setStudentId(r.getStudentId());
            b.setTutorId(r.getTutorId());
            b.setStudentStars(r.getStudentStars());
            b.setStudentTitle(r.getStudentTitle());
            b.setStudentComment(r.getStudentComment());
            b.setStudentSubmitted(r.isStudentSubmitted());
            b.setTutorTitle(r.getTutorTitle());
            b.setTutorComment(r.getTutorComment());
            b.setTutorSubmitted(r.isTutorSubmitted());
            b.setStatus(SharedReviewBean.ReviewStatus.valueOf(r.getStatus().name()));

            Account studAcc = accDao.load(r.getStudentId());
            if (studAcc != null) {
                b.setCounterpartyInfo(studAcc.getName() + " " + studAcc.getSurname());
            }
            out.add(b);
        }
        return out;
    }
}


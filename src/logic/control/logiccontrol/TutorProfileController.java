package logic.control.logiccontrol;

import logic.bean.TutorBean;
import logic.bean.SharedReviewBean;
import logic.model.dao.*;
import logic.model.domain.*;
import java.util.List;

public class TutorProfileController {

    private final AccountDAO accDao = DaoFactory.getInstance().getAccountDAO();
    private final SharedReviewDAO reviewDao = DaoFactory.getInstance().getSharedReviewDAO();

    // Info anagrafiche + rating */
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

    // Solo recensioni COMPLETED vengono convertite in Bean
    public List<SharedReviewBean> loadCompletedReviews(String tutorId){
        return reviewDao.loadForTutor(tutorId).stream()
                .filter(r -> r.getStatus()==ReviewStatus.COMPLETE)
                .map(r -> {
                    SharedReviewBean b = new SharedReviewBean(r);
                    Account s = accDao.load(r.getStudentId());
                    if(s!=null) b.setCounterpartyInfo(s.getName()+" "+s.getSurname());
                    return b;
                })
                .toList();
    }
}


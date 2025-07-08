package logic.control.logiccontrol;

import logic.bean.AccountBean;
import logic.bean.SharedReviewBean;
import logic.model.dao.AccountDAO;
import logic.model.dao.DaoFactory;
import logic.model.dao.SharedReviewDAO;
import logic.model.domain.Account;
import logic.model.domain.ReviewStatus;
import logic.model.domain.SharedReview;
import logic.model.domain.Student;

import java.util.ArrayList;
import java.util.List;

// NOTA: Questa parte viola il principio MVC/BCE perché ad ogni Controller logico abbiamo fatto corrispondere
// un caso d'uso. Tuttavia, qui preferiamo mantenere modularità separando la logica di visualizzazione
// del profilo dallo specifico caso d'uso, poiché può essere invocata in più contesti.

public class StudentProfileController {

    private final AccountDAO accDao = DaoFactory.getInstance().getAccountDAO();
    private final SharedReviewDAO reviewDao = DaoFactory.getInstance().getSharedReviewDAO();

    // Carica uno Student dal DAO e lo converte in AccountBean
    public AccountBean loadStudentBean(String accountId) {
        Account acc = accDao.load(accountId);
        if (!(acc instanceof Student s))
            throw new IllegalArgumentException("Not a student: "+accountId);

        AccountBean accountBean = new AccountBean();
        accountBean.setAccountId(accountId);
        accountBean.setName(s.getName());
        accountBean.setSurname(s.getSurname());
        accountBean.setBirthday(s.getBirthday());
        accountBean.setInstitute(s.getInstitute());
        accountBean.setProfilePicturePath(s.getProfilePicturePath());
        accountBean.setProfileComment(s.getProfileComment());
        return accountBean;
    }

    // Carica le recensioni COMPLETE ricevute dallo studente
    public List<SharedReviewBean> loadCompletedReviews(String studentId) {

        List<SharedReviewBean> out = new ArrayList<>();
        for (SharedReview r : reviewDao.loadForStudent(studentId)) {

            if (r.getStatus() != ReviewStatus.COMPLETE) continue;

            SharedReviewBean b = new SharedReviewBean();              // ← bean “puro”
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

            Account tutorAcc = accDao.load(r.getTutorId());
            if (tutorAcc != null) {
                b.setCounterpartyInfo(tutorAcc.getName() + " " + tutorAcc.getSurname());
            }
            out.add(b);
        }
        return out;
    }
}


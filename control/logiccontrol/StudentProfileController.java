package logic.control.logiccontrol;

import logic.bean.AccountBean;
import logic.model.dao.AccountDAO;
import logic.model.dao.DaoFactory;
import logic.model.domain.Account;
import logic.model.domain.Student;

public class StudentProfileController {

    private final AccountDAO accDao = DaoFactory.getInstance().getAccountDAO();

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
}


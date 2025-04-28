package logic.model.dao.inmemory;

import logic.model.dao.*;

public class InMemoryDAOFactory extends DaoFactory {

    @Override
    public AccountDAO getAccountDAO() {
        return InMemoryAccountDAO.getInstance();
    }

    @Override
    public UserDAO getUserDAO() {
        return InMemoryUserDAO.getInstance();
    }

    @Override
    public TutoringSessionDAO getTutoringSessionDAO() {
        return InMemoryTutoringSessionDAO.getInstance();
    }

    @Override
    public SharedReviewDAO getSharedReviewDAO() {
        return InMemorySharedReviewDAO.getInstance();
    }
}

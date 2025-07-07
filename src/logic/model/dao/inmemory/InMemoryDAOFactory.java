package logic.model.dao.inmemory;

import logic.model.dao.*;

// Factory che restituisce i DAO in-memory (singleton)
public class InMemoryDAOFactory extends DaoFactory {

    // Restituisce l'istanza singleton del DAO in-memory per Account
    @Override
    public AccountDAO getAccountDAO() {
        return InMemoryAccountDAO.getInstance();
    }

    // Restituisce l'istanza singleton del DAO in-memory per User
    @Override
    public UserDAO getUserDAO() {
        return InMemoryUserDAO.getInstance();
    }

    // Restituisce l'istanza singleton del DAO in-memory per TutoringSession
    @Override
    public TutoringSessionDAO getTutoringSessionDAO() {
        return InMemoryTutoringSessionDAO.getInstance();
    }

    // Restituisce l'istanza singleton del DAO in-memory per SharedReview
    @Override
    public SharedReviewDAO getSharedReviewDAO() {
        return InMemorySharedReviewDAO.getInstance();
    }

}

package logic.model.dao.db;

import logic.model.dao.*;

public class DBDaoFactory extends DaoFactory {

    private DBUserDAO dbUserDAO;
    private DBAccountDAO dbAccountDAO;
    private DBTutoringSessionDAO dbTutoringSessionDAO;
    private DBSharedReviewDAO dbSharedReviewDAO;

    @Override
    public UserDAO getUserDAO() {
        if (dbUserDAO == null) {
            dbUserDAO = new DBUserDAO();
        }
        return dbUserDAO;
    }

    @Override
    public AccountDAO getAccountDAO() {
        if (dbAccountDAO == null) {
            dbAccountDAO = new DBAccountDAO();
        }
        return dbAccountDAO;
    }

    @Override
    public TutoringSessionDAO getTutoringSessionDAO() {
        if (dbTutoringSessionDAO == null) {
            dbTutoringSessionDAO = new DBTutoringSessionDAO();
        }
        return dbTutoringSessionDAO;
    }

    @Override
    public SharedReviewDAO getSharedReviewDAO() {
        if (dbSharedReviewDAO == null) {
            dbSharedReviewDAO = new DBSharedReviewDAO();
        }
        return dbSharedReviewDAO;
    }
}

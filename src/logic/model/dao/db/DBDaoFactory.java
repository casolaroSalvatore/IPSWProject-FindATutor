package logic.model.dao.db;

import logic.model.dao.DaoFactory;
import logic.model.dao.*;

// Factory concreta per la modalità Database (DB): fornisce DAO che accedono al database tramite JDBC
public class DBDaoFactory extends DaoFactory {

    private DBUserDAO dbUserDAO;
    private DBAccountDAO dbAccountDAO;
    private DBTutoringSessionDAO dbTutoringSessionDAO;
    private DBSharedReviewDAO dbSharedReviewDAO;

    // Restituisce un singleton per il DAO degli utenti
    @Override
    public UserDAO getUserDAO() {
        if (dbUserDAO == null) {
            dbUserDAO = new DBUserDAO();
        }
        return dbUserDAO;
    }

    // Restituisce un singleton per il DAO degli account
    @Override
    public AccountDAO getAccountDAO() {
        if (dbAccountDAO == null) {
            dbAccountDAO = new DBAccountDAO();
        }
        return dbAccountDAO;
    }

    // Restituisce un singleton per il DAO delle sessioni di tutoraggio
    @Override
    public TutoringSessionDAO getTutoringSessionDAO() {
        if (dbTutoringSessionDAO == null) {
            dbTutoringSessionDAO = new DBTutoringSessionDAO();
        }
        return dbTutoringSessionDAO;
    }

    // Restituisce un singleton per il DAO delle recensioni condivise
    @Override
    public SharedReviewDAO getSharedReviewDAO() {
        if (dbSharedReviewDAO == null) {
            dbSharedReviewDAO = new DBSharedReviewDAO();
        }
        return dbSharedReviewDAO;
    }
}

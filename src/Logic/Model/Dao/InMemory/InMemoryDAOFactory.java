package Logic.Model.Dao.InMemory;

import Logic.Model.Dao.AccountDAO;
import Logic.Model.Dao.DaoFactory;
import Logic.Model.Dao.UserDAO;

public class InMemoryDAOFactory extends DaoFactory {

    @Override
    public AccountDAO getAccountDAO() {
        return InMemoryAccountDAO.getInstance();
    }

    @Override
    public UserDAO getUserDAO() {
        return InMemoryUserDAO.getInstance();
    }
}

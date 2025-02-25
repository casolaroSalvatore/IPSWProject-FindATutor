package Logic.Model.Dao.InMemory;

import Logic.Model.Dao.DAO;
import Logic.Model.Dao.DaoFactory;
import Logic.Model.Dao.UserDAO;
import Logic.Model.Domain.Account;
import Logic.Model.Domain.User;

import java.util.ArrayList;
import java.util.List;

public class InMemoryUserDAO extends InMemoryDAO<String, User> implements UserDAO {

    private static InMemoryUserDAO instance;

    private InMemoryUserDAO() {
        // Private constructor in order to prevent instatiation
    }

    public static InMemoryUserDAO getInstance() {
        if (instance == null) {
            instance = new InMemoryUserDAO();
        }
        return instance;
    }

    // References to the right AccountDAO
    private DAO<String, Account> accountDAO = DaoFactory.getInstance().getAccountDAO();

    @Override
    protected String getKey(User user) {
        return user.getEmail();
    }

    @Override
    public User load(String id) {
        List<Account> accounts = new ArrayList<>();
        User user = super.load(id);
        if (user != null) {
            // Refresh accounts, evitando i valori nulli
            for (Account account : user.getAccounts()) {
                if (account != null) {  // Controllo di sicurezza per evitare null pointer exception
                    Account loadedAccount = accountDAO.load(account.getEmail() + "_" + account.getRole());
                    if (loadedAccount != null) {
                        accounts.add(loadedAccount);
                    }
                }
            }
            user.setAccounts(accounts);
        }
        return user;
    }

    @Override
    public void store(User entity)  {
        for (Account account: entity.getAccounts()) {
            System.out.println("Sto salvando l'account con chiave: " + account.getEmail() + "_" + account.getRole());
            accountDAO.store(account);
        }
        super.store(entity);
    }
}
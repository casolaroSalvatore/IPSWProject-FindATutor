package logic.model.dao.inmemory;

import logic.model.dao.DAO;
import logic.model.dao.DaoFactory;
import logic.model.dao.UserDAO;
import logic.model.domain.Account;
import logic.model.domain.User;

import java.util.ArrayList;
import java.util.List;

// DAO in-memory per la gestione degli User
public class InMemoryUserDAO extends InMemoryDAO<String, User> implements UserDAO {

    private static InMemoryUserDAO instance;

    public static synchronized InMemoryUserDAO getInstance() {
        if (instance == null) {
            instance = new InMemoryUserDAO();
        }
        return instance;
    }

    // Riferimento all'accountDAO corretto
    private DAO<String, Account> accountDAO = DaoFactory.getInstance().getAccountDAO();

    // Restituisce la chiave primaria (email)
    @Override
    protected String getKey(User user) {
        return user.getEmail();
    }

    // Carica uno user e aggiorna gli account caricandoli dall'accountDAO
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

    // Salva lo user e tutti i suoi account
    @Override
    public void store(User entity)  {
        for (Account account: entity.getAccounts()) {
            accountDAO.store(account);
        }
        super.store(entity);
    }
}
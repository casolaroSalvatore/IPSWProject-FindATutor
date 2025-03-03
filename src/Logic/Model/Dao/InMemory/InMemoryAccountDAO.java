package Logic.Model.Dao.InMemory;

import Logic.Model.Dao.AccountDAO;
import Logic.Model.Domain.Account;

public class InMemoryAccountDAO extends InMemoryDAO<String, Account> implements AccountDAO {

    private static InMemoryAccountDAO instance;

    private InMemoryAccountDAO() {

    }

    public static InMemoryAccountDAO getInstance() {
        if (instance == null) {
            instance = new InMemoryAccountDAO();
        }
        return instance;
    }


    @Override
    protected String getKey(Account account) {
        return account.getEmail() + "_" + account.getRole();
    }

    @Override
    public void store(Account account) {
        System.out.println("Salvataggio account: " + account.getEmail() + " con ruolo " + account.getRole());
        super.store(account);
    }
}

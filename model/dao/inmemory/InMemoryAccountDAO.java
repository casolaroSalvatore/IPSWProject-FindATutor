package logic.model.dao.inmemory;

import logic.model.dao.AccountDAO;
import logic.model.domain.Account;

import java.util.ArrayList;
import java.util.List;

public class InMemoryAccountDAO extends InMemoryDAO<String, Account> implements AccountDAO {

    private static InMemoryAccountDAO instance;

    private InMemoryAccountDAO() { }

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

    @Override
    public List<Account> loadAllAccountsOfType(String role) {
        List<Account> result = new ArrayList<>();
        for (Account acc : allValues()) {
            if (acc.getRole().equalsIgnoreCase(role)) {
                result.add(acc);
            }
        }
        return result;
    }
}

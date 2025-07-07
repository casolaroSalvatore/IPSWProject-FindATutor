package logic.model.dao.inmemory;

import logic.model.dao.AccountDAO;
import logic.model.domain.Account;

import java.util.ArrayList;
import java.util.List;

// DAO in-memory per la gestione degli Account
public class InMemoryAccountDAO extends InMemoryDAO<String, Account> implements AccountDAO {

    private static InMemoryAccountDAO instance;

    // Restituisce l'istanza singleton del DAO
    public static synchronized InMemoryAccountDAO getInstance() {
        if (instance == null) {
            instance = new InMemoryAccountDAO();
        }
        return instance;
    }

    // Restituisce la chiave formata da email + "_" + ruolo
    @Override
    protected String getKey(Account account) {
        // Chiave formata da email + "_" + ruolo
        return account.getEmail() + "_" + account.getRole();
    }

    // Restituisce tutti gli account con il ruolo specificato
    @Override
    public List<Account> loadAllAccountsOfType(String role) {
        // Filtra e restituisce tutti gli account con il ruolo dato
        List<Account> result = new ArrayList<>();
        for (Account acc : allValues()) {
            if (acc.getRole().equalsIgnoreCase(role)) {
                result.add(acc);
            }
        }
        return result;
    }
}

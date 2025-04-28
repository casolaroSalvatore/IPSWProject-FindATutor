package logic.model.dao;

import logic.model.domain.Account;

import java.util.List;

public interface AccountDAO extends DAO<String,Account> {

    @Override
    default Account create(String key) {
        return new Account(key, null);
    }

    List<Account> loadAllAccountsOfType(String role);
}
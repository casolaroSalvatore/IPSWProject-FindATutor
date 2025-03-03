package Logic.Model.Dao;

import Logic.Model.Domain.Account;

public interface AccountDAO extends DAO<String,Account> {

    @Override
    default Account create(String key) {
        return new Account(key, null);
    }
}
package Logic.Model.Dao;

import Logic.Model.Domain.User;

public interface UserDAO extends DAO<String, User> {

    @Override
    default User create(String key) {
        return new User(key);
    }

}

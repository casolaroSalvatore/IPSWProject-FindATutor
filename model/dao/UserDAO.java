package logic.model.dao;

import logic.model.domain.User;

public interface UserDAO extends DAO<String, User> {

    @Override
    default User create(String key) {
        return new User(key);
    }

}

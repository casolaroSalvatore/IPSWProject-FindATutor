package logic.model.dao;

import logic.model.domain.User;

// Interfaccia DAO per la gestione degli User
public interface UserDAO extends DAO<String, User> {

    @Override
    default User create(String key) {
        // Crea un nuovo User con l'ID dato
        return new User(key);
    }
}

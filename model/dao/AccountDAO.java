package logic.model.dao;

import logic.model.domain.Account;

import java.util.List;

// Interfaccia DAO per la gestione degli Account
public interface AccountDAO extends DAO<String,Account> {

    // Crea un nuovo Account con la chiave e null come password
    @Override
    default Account create(String key) {
        return new Account(key, null);
    }

    // Carica tutti gli account con un certo ruolo
    List<Account> loadAllAccountsOfType(String role);
}
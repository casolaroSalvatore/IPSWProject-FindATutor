package logic.model.dao;

import logic.model.dao.db.DBDaoFactory;
import logic.model.dao.filesystem.FileSystemDAOFactory;
import logic.model.dao.inmemory.InMemoryDAOFactory;

// Enum che rappresenta i provider di persistenza disponibili nell'applicazione: InMemory, FileSystem e DB
public enum PersistenceProvider {

    IN_MEMORY ("InMemory",
            "logic.model.dao.inmemory.InMemoryDAOFactory"),
    FILE_SYSTEM("FileSystem",
            "logic.model.dao.filesystem.FileSystemDAOFactory"),
    DB         ("DB",
            "logic.model.dao.db.DBDaoFactory");

    private final String name;
    private final String factoryClassName;

    private PersistenceProvider(String name, String factoryClassName) {
        this.name = name;
        this.factoryClassName = factoryClassName;
    }

    public String getName() {
        return this.name;
    }

    // Restituisce la classe della DaoFactory associata al provider
    public Class<? extends DaoFactory> getDaoFactoryclass() {
        try {
            return (Class<? extends DaoFactory>)
                    Class.forName(factoryClassName); // reflection soft-link
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "Factory class not found: " + factoryClassName, e);
        }
    }
    @Override
    public String toString() {
        return getName();
    }
}
package logic.model.domain;

import logic.model.dao.DaoFactory;
import logic.model.dao.db.DBDaoFactory;
import logic.model.dao.filesystem.FileSystemDAOFactory;
import logic.model.dao.inmemory.InMemoryDAOFactory;

// Enum che rappresenta i provider di persistenza disponibili nell'applicazione: InMemory, FileSystem e DB
public enum PersistenceProvider {

    IN_MEMORY("InMemory", InMemoryDAOFactory.class),
    FILE_SYSTEM("FileSystem", FileSystemDAOFactory.class),
    DB("DB", DBDaoFactory.class);

    private final String name;
    private final Class<? extends DaoFactory> daoFactoryclass;

    private PersistenceProvider(String name, Class<? extends DaoFactory> daoFactoryclass) {
        this.name = name;
        this.daoFactoryclass = daoFactoryclass;
    }

    public String getName() {
        return this.name;
    }

    // Restituisce la classe della DaoFactory associata al provider
    public Class<? extends DaoFactory> getDaoFactoryclass() {
        return daoFactoryclass;
    }

    @Override
    public String toString() {
        return getName();
    }
}
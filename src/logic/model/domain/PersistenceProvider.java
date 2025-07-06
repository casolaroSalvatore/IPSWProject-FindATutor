package logic.model.domain;

import logic.model.dao.DaoFactory;
import logic.model.dao.db.DBDaoFactory;
import logic.model.dao.filesystem.FileSystemDAOFactory;
import logic.model.dao.inmemory.InMemoryDAOFactory;

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

    public Class<? extends DaoFactory> getDaoFactoryclass() {
        return daoFactoryclass;
    }

    @Override
    public String toString() {
        return getName();
    }
}
package Logic.Model.Domain;

import Logic.Model.Dao.DaoFactory;
import Logic.Model.Dao.InMemory.InMemoryDAOFactory;

public enum PersistenceProvider {

    IN_MEMORY("InMemory", InMemoryDAOFactory.class);
    // FILE("FileSystem",FileSystemDAOFactory .class);
    // DB("DB",DBDaoFactory .class);

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
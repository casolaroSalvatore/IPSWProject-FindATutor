package logic.model.dao;

import logic.exception.NoImplementationForPersistenceProviderException;
import logic.model.domain.PersistenceProvider;

import java.lang.reflect.InvocationTargetException;

public abstract class DaoFactory {

    private static DaoFactory instance = null;// Riferimento a se stessa
    private static PersistenceProvider persistenceProvider = null;

    public static void setPersistenceProvider(PersistenceProvider provider) {
        persistenceProvider = provider;
    }

    public static DaoFactory getInstance() {
        if (instance == null) {
            try {
                instance = persistenceProvider.getDaoFactoryclass().getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException |
                     InvocationTargetException | NoSuchMethodException | SecurityException e) {
                throw new NoImplementationForPersistenceProviderException(persistenceProvider, e);
            }
        }
        return instance;
    }

    public abstract UserDAO getUserDAO();
    public abstract AccountDAO getAccountDAO();
    public abstract TutoringSessionDAO getTutoringSessionDAO();
    public abstract SharedReviewDAO getSharedReviewDAO();
}

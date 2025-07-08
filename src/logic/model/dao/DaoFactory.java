package logic.model.dao;

import logic.exception.NoImplementationForPersistenceProviderException;

import java.lang.reflect.InvocationTargetException;

// Factory astratta per fornire le DAO in base al PersistenceProvider scelto
public abstract class DaoFactory {

    private static DaoFactory instance = null;
    private static PersistenceProvider persistenceProvider = null;

    // Imposta il PersistenceProvider da usare (es. in-memory, DB, filesystem)
    public static void setPersistenceProvider(PersistenceProvider provider) {
        persistenceProvider = provider;
    }

    // Restituisce l'istanza singleton della factory, creandola se necessario
    public static DaoFactory getInstance() {
        if (instance == null) {
            try {
                instance = persistenceProvider.getDaoFactoryclass().getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException |
                     InvocationTargetException | NoSuchMethodException | SecurityException e) {
                throw new NoImplementationForPersistenceProviderException(persistenceProvider.getName(), e);
            }
        }
        return instance;
    }

    // Restituisce il DAO per gli utenti
    public abstract UserDAO getUserDAO();

    // Restituisce il DAO per gli account
    public abstract AccountDAO getAccountDAO();

    // Restituisce il DAO per le sessioni di tutoraggio
    public abstract TutoringSessionDAO getTutoringSessionDAO();

    // Restituisce il DAO per le recensioni condivise
    public abstract SharedReviewDAO getSharedReviewDAO();
}

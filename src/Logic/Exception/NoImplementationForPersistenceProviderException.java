package Logic.Exception;

import Logic.Model.Domain.PersistenceProvider;

public class NoImplementationForPersistenceProviderException extends RuntimeException{

    public NoImplementationForPersistenceProviderException(PersistenceProvider persistenceProvider, Exception e) {
        super("Cannot istantiate Factory for provider "+ persistenceProvider.getName(), e);
    }
}

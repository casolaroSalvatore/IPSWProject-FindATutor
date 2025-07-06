package logic.exception;

public class NoImplementationForPersistenceProviderException extends RuntimeException {

    public NoImplementationForPersistenceProviderException(String providerName, Exception e) {
        super("Cannot instantiate Factory for provider " + providerName, e);
    }
}

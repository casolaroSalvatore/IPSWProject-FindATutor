package logic.exception;

// Eccezione lanciata se non esiste una factory per il provider di persistenza indicato
public class NoImplementationForPersistenceProviderException extends RuntimeException {

    public NoImplementationForPersistenceProviderException(String providerName, Exception e) {
        super("Cannot instantiate Factory for provider " + providerName, e);
    }
}

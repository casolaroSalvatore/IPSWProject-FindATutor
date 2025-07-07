package logic.exception.dao;

// Eccezione generica per errori DAO
public class DAOException extends RuntimeException {
    public DAOException(String message, Throwable cause) {
        super(message, cause);
    }
}

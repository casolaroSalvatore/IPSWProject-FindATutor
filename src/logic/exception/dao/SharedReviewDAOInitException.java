package logic.exception.dao;

import java.nio.file.Path;

// Eccezione lanciata se fallisce la creazione del FileSystemSharedReviewDAO
public class SharedReviewDAOInitException extends RuntimeException {
    public SharedReviewDAOInitException(Path base, Throwable cause) {
        super("Cannot create FileSystemSharedReviewDAO at " + base, cause);
    }
}

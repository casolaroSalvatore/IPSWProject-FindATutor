package logic.exception.dao;

import java.nio.file.Path;

public class SharedReviewDAOInitException extends RuntimeException {
    public SharedReviewDAOInitException(Path base, Throwable cause) {
        super("Cannot create FileSystemSharedReviewDAO at " + base, cause);
    }
}

package logic.exception.filesystem;

import java.nio.file.Path;

public class SharedReviewDaoInitException extends RuntimeException {
    public SharedReviewDaoInitException(Path base, Throwable cause) {
        super("Cannot create FileSystemSharedReviewDAO at " + base, cause);
    }
}

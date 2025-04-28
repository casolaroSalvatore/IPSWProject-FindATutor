package logic.exception.filesystem;

import java.nio.file.Path;

// Eccezione per errori di lettura
public class FileReadException extends RuntimeException {
    public FileReadException(Path path, Throwable cause) {
        super("Unable to read file: " + path, cause);
    }
}


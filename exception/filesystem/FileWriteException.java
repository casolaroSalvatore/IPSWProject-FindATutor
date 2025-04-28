package logic.exception.filesystem;

import java.nio.file.Path;

// Eccezione per errori di scrittura file

public class FileWriteException extends RuntimeException {
  public FileWriteException(Path path, Throwable cause) {
    super("Unable to write file: " + path, cause);
  }
}
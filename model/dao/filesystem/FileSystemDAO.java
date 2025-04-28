package logic.model.dao.filesystem;

import logic.exception.filesystem.DirectoryScanException;
import logic.exception.filesystem.FileReadException;
import logic.exception.filesystem.FileWriteException;
import logic.model.dao.DAO;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

abstract class FileSystemDAO<I,E> implements DAO<I,E> {

    private final Path dir;

    protected FileSystemDAO(Path root, String sub) throws IOException {
        this.dir = Files.createDirectories(root.resolve(sub));
    }

    @Override

    public boolean exists(I id) { return Files.exists(path(id)); }
    @Override

    public void delete(I id) {
        try {
            Files.deleteIfExists(path(id));
        } catch (IOException ignored) {
            // Ignored on purpose: it's okay if the file does not exist or deletion fails silently.
        }
    }

    @Override public void store(E entity) { writeFile(path(getId(entity)), encode(entity)); }
    @Override public E load(I id) { return Files.exists(path(id)) ? decode(readFile(path(id))) : null; }

    protected abstract I getId(E entity);
    protected abstract List<String> encode(E entity);     // righe da scrivere
    protected abstract E decode(List<String> lines);      // parse righe lette

    protected Path path(I id) {
        return dir.resolve(id.toString() + ".txt");
    }

    protected List<String> readFile(Path p) {
        try {
            return Files.readAllLines(p, StandardCharsets.UTF_8);
        } catch (IOException e) { throw new FileReadException(p, e);}
    }

    protected void writeFile(Path p, List<String> l) {
        try { Files.write(p, l, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new FileWriteException(p, e);}
    }

    protected <T> List<T> scan(java.util.function.Function<E,T> mapFn) {
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir, "*.txt")) {
            List<T> out = new ArrayList<>();
            for (Path p : ds) out.add(mapFn.apply(decode(readFile(p))));
            return out;
        } catch (IOException e) { throw new DirectoryScanException(dir, e); }
    }
}

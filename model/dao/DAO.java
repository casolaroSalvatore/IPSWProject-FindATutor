package logic.model.dao;

// Interfaccia generica DAO con operazioni base per gestire i dati
public interface DAO<I, E> {

    // Carica un'entità dato il suo ID
    E load(I id);

    // Salva o aggiorna un'entità
    void store(E entity);

    // Elimina un'entità dato il suo ID
    void delete(I id);

    // Verifica se un'entità con l'ID dato esiste
    boolean exists(I id);

    // Crea un'entità partendo dall'ID
    E create(I id);
}

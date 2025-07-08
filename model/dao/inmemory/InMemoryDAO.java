package logic.model.dao.inmemory;

import logic.model.dao.DAO;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

// DAO astratto in-memory: simula la persistenza usando una mappa in memoria
public abstract class InMemoryDAO<K, V> implements DAO<K,V> {

    // Simulo una persistenza virtuale tramite l'utilizzo della memoria centrale
    private Map<K,V> memory = new HashMap<>();

    protected void store(K key, V value) {
        // Salva o aggiorna un valore associato alla chiave
        memory.put(key, value);
    }

    // Rimuove un'entità dalla memoria
    @Override
    public void delete (K id) {
        memory.remove(id);
    }

    // Verifica se la chiave esiste
    @Override
    public boolean exists(K id) {
        return memory.containsKey(id);
    }

    // Restituisce l'entità associata alla chiave
    @Override
    public V load (K id) {
        return memory.get(id);
    }

    // Salva un'entità usando la chiave ricavata da getKey
    @Override
    public void store(V entity) {
        K key = getKey(entity);
        store(key, entity);
    }

    protected abstract K getKey(V value);

    protected Collection<V> allValues() {
        return memory.values();
    }
}

package Logic.Model.Dao.InMemory;

import Logic.Model.Dao.DAO;

import java.util.HashMap;
import java.util.Map;

public abstract class InMemoryDAO<K, V> implements DAO<K,V> {

    // Simulo una persistenza virtuale tramite l'utilizzo della memoria centrale
    private Map<K,V> memory = new HashMap<>();

    protected void store(K key, V value) {
        memory.put(key, value);
    }

    @Override
    public void delete (K id) {
        memory.remove(id);
    }

    @Override
    public boolean exists(K id) {
        return memory.containsKey(id);
    }

    @Override
    public V load (K id) {
        return memory.get(id);
    }

    @Override
    public void store(V entity) {
        K key = getKey(entity);
        store(key, entity);
    }

    protected abstract K getKey(V value);
}

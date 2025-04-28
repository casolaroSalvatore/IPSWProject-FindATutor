package logic.model.dao;

public interface DAO<ID, E> {

    E load(ID id);

    void store(E entity);

    void delete(ID id);

    boolean exists(ID id);

    E create(ID id);

}

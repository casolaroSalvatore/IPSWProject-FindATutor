package logic.model.dao;

import logic.model.domain.TutoringSession;

import java.util.List;

public interface TutoringSessionDAO extends DAO<String, TutoringSession> {

    @Override
    default TutoringSession create(String key) {
            return new TutoringSession(key);
    }

    List<TutoringSession> loadAllTutoringSession();
}

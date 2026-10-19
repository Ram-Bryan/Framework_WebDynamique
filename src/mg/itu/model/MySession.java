package mg.itu.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MySession {

    private final Map<String, Object> session = new HashMap<>();

    public MySession() {
    }

    public MySession(Map<String, Object> session) {
        if (session != null) {
            this.session.putAll(session);
        }
    }

    public Object getSession(String key) {
        return session.get(key);
    }

    public <T> T getSession(String key, Class<T> type) {
        Object value = session.get(key);
        if (value == null) {
            return null;
        }
        return type.cast(value);
    }

    public void addSession(String key, Object value) {
        session.put(key, value);
    }

    public void setSession(String key, Object value) {
        addSession(key, value);
    }

    public void supprimerSession(String key) {
        session.remove(key);
    }

    public void removeSession(String key) {
        supprimerSession(key);
    }

    public void clearSession() {
        session.clear();
    }

    public boolean containsSession(String key) {
        return session.containsKey(key);
    }

    public Map<String, Object> getAllSessions() {
        return Collections.unmodifiableMap(session);
    }

    public void setAllSessions(Map<String, Object> values) {
        session.clear();
        if (values != null) {
            session.putAll(values);
        }
    }
}
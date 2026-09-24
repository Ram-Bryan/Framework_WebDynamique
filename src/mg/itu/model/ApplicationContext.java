package mg.itu.model;

import java.util.HashMap;
import java.util.Map;

public class ApplicationContext {
    private Map<String, Object> beans = new HashMap<>();

    public void addBean(String name, Object bean) {
        beans.put(name, bean);
    }

    public Object getBean(String name) {
        return beans.get(name);
    }

    public <T> T getBean(String name, Class<T> type) {
        return type.cast(beans.get(name));
    }

    public Map<String, Object> getAllBeans() {
        return beans;
    }
}

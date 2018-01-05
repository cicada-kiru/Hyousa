package hyousa.common.conf;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yousa on 2017/11/30.
 */
public class Configuration implements Serializable {
    private Map<String,String> map = new HashMap<>();

    public void set(String key, String value) {
        map.put(key, value);
    }

    public String get(String key) {
        return map.get(key);
    }

    public String get(String key, String ifNull) {
        String value = map.get(key);
        if (value == null) return ifNull;
        return value;
    }

    public void setInt(String key, int value) {
        map.put(key, Integer.toString(value));
    }

    public int getInt(String key) {
        return Integer.parseInt(map.get(key));
    }

    public int getInt(String key, int ifNull) {
        String value = map.get(key);
        if (value == null) return ifNull;
        return Integer.parseInt(value);
    }

    public void setDouble(String key, double value) {
        map.put(key, Double.toString(value));
    }

    public double getDouble(String key) {
        return Double.parseDouble(map.get(key));
    }

    public double getDouble(String key, double ifNull) {
        String value = map.get(key);
        if (value == null) return ifNull;
        return Double.parseDouble(value);
    }

    public Map<String, String> getAll() {
        return map;
    }
}

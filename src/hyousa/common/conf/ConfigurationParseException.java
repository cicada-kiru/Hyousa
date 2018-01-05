package hyousa.common.conf;

/**
 * Created by yousa on 2017/12/1.
 */
public class ConfigurationParseException extends RuntimeException {
    public ConfigurationParseException() {
        super();
    }

    public ConfigurationParseException(String msg) {
        super(msg);
    }

    public ConfigurationParseException(Throwable e) {
        super(e);
    }
}

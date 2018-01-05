package hyousa.common.log;

/**
 * Created by yousa on 2017/12/1.
 */
public interface Logger {
    void log(int level, String log);

    void error(Throwable e);
}

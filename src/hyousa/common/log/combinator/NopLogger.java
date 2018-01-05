package hyousa.common.log.combinator;

import hyousa.common.log.Logger;

/**
 * Created by yousa on 2017/10/27.
 */
public class NopLogger implements Logger {
    private static NopLogger logger = new NopLogger();

    public static Logger get() {
        return logger;
    }

    private NopLogger() {}

    public void log(int level, String log) {}

    public void error(Throwable e) {}
}

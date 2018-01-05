package hyousa.common.log.combinator;

import hyousa.common.log.Logger;

/**
 * Created by yousa on 2017/10/27.
 */
public class PrefixLogger implements Logger {
    private String prefix;
    private Logger logger;

    public PrefixLogger(String prefix, Logger logger) {
        this.prefix = prefix;
        this.logger = logger;
    }
    public void log(int level, String log) {
        logger.log(level, prefix + log);
    }

    public void error(Throwable e) {
        logger.error(e);
    }
}

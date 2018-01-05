package hyousa.common.log.combinator;

import hyousa.common.log.Logger;

/**
 * Created by yousa on 2017/10/27.
 */
public class SequenceLogger implements Logger {
    private Logger[] loggers;

    public SequenceLogger(Logger...loggers) {
        this.loggers = loggers;
    }

    public void log(int level, String log) {
        for (Logger logger : loggers) logger.log(level, log);
    }

    public void error(Throwable e) {
        for (Logger logger : loggers) logger.error(e);
    }
}

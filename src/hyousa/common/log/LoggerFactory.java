package hyousa.common.log;

import hyousa.common.log.combinator.*;

/**
 * Created by yousa on 2017/12/1.
 */
public class LoggerFactory {
    private static Logger logger;

    static {
        Logger out = new WriterLogger(System.out);
        Logger err = new WriterLogger(System.err);
        Logger fix = new FilterLogger(out, err, lvl -> lvl < 1);
        fix = new FilterLogger(fix, NopLogger.get(), lvl -> lvl >= 0);
        logger = new PrefixLogger("[INFO]: ", fix);
    }

    public static Logger getLogger(Class<?> clazz) {
        return logger;
    }
}

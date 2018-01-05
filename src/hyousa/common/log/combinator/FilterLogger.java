package hyousa.common.log.combinator;

import hyousa.common.log.Logger;

import java.util.function.Predicate;

/**
 * Created by yousa on 2017/10/27.
 */
public class FilterLogger implements Logger {
    private Predicate<Integer> predicate;
    private Logger first, second;

    public FilterLogger(Logger first, Logger second, Predicate<Integer> predicate) {
        this.first = first;
        this.second = second;
        this.predicate = predicate;
    }

    public void log(int level, String log) {
        if (predicate.test(level)) first.log(level, log);
        else second.log(level, log);
    }

    public void error(Throwable e) {
        second.error(e);
    }
}

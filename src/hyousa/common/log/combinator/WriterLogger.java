package hyousa.common.log.combinator;

import hyousa.common.log.Logger;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * Created by yousa on 2017/10/27.
 */
public class WriterLogger implements Logger {
    private PrintWriter writer;

    public WriterLogger(PrintStream out) {
        writer = new PrintWriter(out, true);
    }

    public WriterLogger(PrintStream out, boolean autoFlush) {
        writer = new PrintWriter(out, autoFlush);
    }


    public WriterLogger(PrintWriter writer) {
        this.writer = writer;
    }

    public void log(int level, String log) {
        writer.println(log);
    }

    public void error(Throwable e) {
        e.printStackTrace(writer);
    }
}

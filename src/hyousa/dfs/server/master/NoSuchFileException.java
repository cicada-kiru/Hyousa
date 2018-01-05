package hyousa.dfs.server.master;

import java.io.IOException;

/**
 * Created by yousa on 2017/12/16.
 */
public class NoSuchFileException extends IOException {
    public NoSuchFileException() {
        super();
    }

    public NoSuchFileException(String msg) {
        super(msg);
    }
}

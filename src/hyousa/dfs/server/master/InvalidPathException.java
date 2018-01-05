package hyousa.dfs.server.master;

import java.io.IOException;

/**
 * Created by yousa on 2017/12/16.
 */
public class InvalidPathException extends IOException {
    public InvalidPathException() {
        super();
    }

    public InvalidPathException(String msg) {
        super(msg);
    }
}

package hyousa.common.rpc.server.netty;

import hyousa.common.rpc.Result;

import java.io.Serializable;

/**
 * Created by yousa on 2017/12/4.
 */
public class SerializableResult implements Result, Serializable {
    private boolean isVoid, hasError;
    private Serializable result;
    private Throwable cause;

    public SerializableResult(boolean isVoid, boolean hasError, Serializable result, Throwable cause) {
        this.isVoid = isVoid;
        this.hasError = hasError;
        this.result = result;
        this.cause = cause;
    }

    public boolean isVoid() {
        return isVoid;
    }

    public boolean hasError() {
        return hasError;
    }

    public Object getResult() {
        return result;
    }

    public Throwable getCause() {
        return cause;
    }
}

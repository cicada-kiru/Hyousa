package hyousa.common.rpc;

/**
 * Created by yousa on 2017/11/30.
 */
public interface Result {
    boolean isVoid();

    boolean hasError();

    Object getResult();

    Throwable getCause();
}

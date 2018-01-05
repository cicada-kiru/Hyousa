package hyousa.common.rpc;

/**
 * Created by yousa on 2017/11/30.
 */
public interface Invocation {
    String getMethod();

    Class<?>[] getTypes();

    Object[] getParams();
}

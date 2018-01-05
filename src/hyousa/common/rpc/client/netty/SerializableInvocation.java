package hyousa.common.rpc.client.netty;

import hyousa.common.rpc.Invocation;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * Created by yousa on 2017/12/2.
 */
public class SerializableInvocation implements Invocation, Serializable {
    private String method;
    private Class<?>[] types;
    private Serializable[] params;

    public SerializableInvocation(String method) {
        this.method = method;
        this.types = new Class<?>[]{};
        this.params = new Serializable[]{};
    }

    public SerializableInvocation(String method, Class<?>[] types, Serializable[] params) {
        this.method = method;
        this.types = types;
        this.params = params;
    }

    public String getMethod() {
        return method;
    }

    public Class<?>[] getTypes() {
        return types;
    }

    public Object[] getParams() {
        return params;
    }
}

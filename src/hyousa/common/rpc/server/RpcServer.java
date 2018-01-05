package hyousa.common.rpc.server;

import hyousa.common.conf.Configuration;

/**
 * Created by yousa on 2017/11/30.
 */
public abstract class RpcServer {
    protected Object delegate;

    /**
     * RpcServer subclass constructor must has the same parameter.
     */
    protected RpcServer(Object delegate) {
        this.delegate = delegate;
    }

    public abstract void start(int port, Configuration conf);
}

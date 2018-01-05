package hyousa.common.rpc.client;

import hyousa.common.conf.Configuration;
import hyousa.common.log.Logger;
import hyousa.common.log.LoggerFactory;
import hyousa.common.rpc.Invocation;
import hyousa.common.rpc.Result;
import hyousa.common.rpc.client.netty.NettyRpcClient;

/**
 * Created by yousa on 2017/11/30.
 */
public abstract class RpcClient {
    protected Configuration conf;
    protected Logger logger;
    protected String host;
    protected int port;

    public RpcClient(String host, int port, Configuration conf) {
        logger = LoggerFactory.getLogger(NettyRpcClient.class);
        this.conf = conf;
        this.host = host;
        this.port = port;
    }

    /**
     * connect server and send Invocation, then return the received Result
     * invoke always close relevant resource before return Result
     *
     * @param ivc Invocation: rpc request abstraction
     * @return Result: rpc response abstraction
     */
    public abstract Result invoke(String host, int port, Invocation ivc);

    public Result invoke(Invocation ivc) {
        return invoke(host, port, ivc);
    }

    /**
     * Change the RPC server host and port
     *
     * @param host The new host name
     * @param port The new port
     */
    public void setHostAndPort(String host, int port) {
        this.host = host;
        this.port = port;
    }
}
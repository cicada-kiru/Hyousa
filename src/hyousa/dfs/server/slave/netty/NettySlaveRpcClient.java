package hyousa.dfs.server.slave.netty;

import hyousa.common.conf.Configuration;
import hyousa.common.rpc.Result;
import hyousa.common.rpc.client.netty.SerializableInvocation;
import hyousa.common.rpc.client.netty.NettyRpcClient;
import hyousa.dfs.server.slave.protocol.SlaveHeartbeatResponse;
import hyousa.dfs.server.slave.protocol.SlaveProtocol;

import java.io.IOException;
import java.io.Serializable;

/**
 * Created by yousa on 2017/12/11.
 */
public class NettySlaveRpcClient extends NettyRpcClient implements SlaveProtocol {
    public NettySlaveRpcClient(String host, int port, Configuration conf) {
        super(host, port, conf);
    }

    public int saveBlock(String blockId) throws IOException {
        Result result = invoke(
            new SerializableInvocation(
                "saveBlock",
                new Class<?>[]{String.class},
                new Serializable[]{blockId}
            )
        );
        if (result.hasError()) {
            throw (IOException) result.getCause();
        }
        return (int) result.getResult();
    }

    public int getBlock(String blockId) throws IOException {
        Result result = invoke(
            new SerializableInvocation(
                "getBlock",
                new Class<?>[]{String.class},
                new Serializable[]{blockId}
            )
        );
        if (result.hasError()) {
            throw (IOException) result.getCause();
        }
        return (int) result.getResult();
    }

    public boolean deleteBlock(String blockId) throws IOException {
        Result result = invoke(
            new SerializableInvocation(
                "deleteBlock",
                new Class<?>[]{String.class},
                new Serializable[]{blockId}
            )
        );
        if (result.hasError()) {
            throw (IOException) result.getCause();
        }
        return (boolean) result.getResult();
    }

    public SlaveHeartbeatResponse sendHeartbeat() {
        return (SlaveHeartbeatResponse) invoke(new SerializableInvocation("sendHeartbeat")).getResult();
    }
}

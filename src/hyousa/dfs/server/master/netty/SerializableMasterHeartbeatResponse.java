package hyousa.dfs.server.master.netty;

import hyousa.dfs.server.master.protocol.MasterHeartbeatResponse;

import java.io.Serializable;

/**
 * Created by yousa on 2018/1/2.
 */
public class SerializableMasterHeartbeatResponse implements MasterHeartbeatResponse, Serializable {
    private String[] slaves;

    public String[] slaves() {
        return slaves;
    }

    public MasterHeartbeatResponse slaves(String[] slaves) {
        this.slaves = slaves;
        return this;
    }
}

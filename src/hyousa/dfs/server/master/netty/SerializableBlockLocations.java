package hyousa.dfs.server.master.netty;

import hyousa.dfs.server.master.protocol.BlockLocations;

import java.io.Serializable;

/**
 * Created by yousa on 2018/1/2.
 */
public class SerializableBlockLocations implements BlockLocations, Serializable {
    private String blockId;
    private String[] slaveIds;

    public String blockId() {
        return blockId;
    }

    public BlockLocations blockId(String blockId) {
        this.blockId = blockId;
        return this;
    }

    public String[] slaveIds() {
        return slaveIds;
    }

    public BlockLocations slaveIds(String[] slaveIds) {
        this.slaveIds = slaveIds;
        return this;
    }
}

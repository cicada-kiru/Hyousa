package hyousa.dfs.server.master.protocol;

/**
 * Created by yousa on 2017/12/28.
 */
public interface BlockLocations {
    String blockId();

    BlockLocations blockId(String blockId);

    String[] slaveIds();

    BlockLocations slaveIds(String[] slaveIds);
}

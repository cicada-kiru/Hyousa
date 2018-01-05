package hyousa.dfs.server.master.protocol;

/**
 * Created by yousa on 2017/12/30.
 */
public interface MasterHeartbeatResponse {
    String[] slaves();

    MasterHeartbeatResponse slaves(String[] slaves);
}

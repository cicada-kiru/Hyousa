package hyousa.dfs.server.slave.protocol;

import java.io.IOException;

/**
 * Created by yousa on 2017/12/6.
 */
public interface SlaveProtocol {
    /**
     * Open a data receiver thread to prepare receive the block
     *
     * @param blockId The saved block ID
     * @return The data receiver bind port
     * @throws IOException If an I/O error occurs
     */
    int saveBlock(String blockId) throws IOException;

    /**
     * Open a data streamer thread to prepare send the block
     *
     * @param blockId The saved block ID
     * @return The data streamer bind port
     * @throws IOException If an I/O error occurs
     */
    int getBlock(String blockId) throws IOException;

    /**
     * Delete a block if exists
     *
     * @param blockId block slaveId generated from master
     * @return true if the block was deleted by this method;
     *         false if the block could not be deleted because it did not exist
     * @throws IOException if an I/O error occurs
     */
    boolean deleteBlock(String blockId) throws IOException;

    /**
     * Send heartbeat to the slave
     *
     * @return The response of the slave
     */
    SlaveHeartbeatResponse sendHeartbeat();
}

package hyousa.dfs.server.slave;

import hyousa.common.conf.Configuration;
import hyousa.common.conf.ConfigurationParser;
import hyousa.common.log.Logger;
import hyousa.common.log.LoggerFactory;
import hyousa.common.rpc.server.RpcServer;
import hyousa.common.util.IOUtil;
import hyousa.common.util.IpUtil;
import hyousa.common.util.ReflectUtil;
import hyousa.dfs.server.master.netty.NettyMasterRpcClient;
import hyousa.dfs.server.master.protocol.MasterProtocol;
import hyousa.dfs.server.slave.protocol.SlaveHeartbeatResponse;
import hyousa.dfs.server.slave.protocol.SlaveProtocol;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by yousa on 2017/12/9.
 */
public class HyousaSlave implements SlaveProtocol {
    private static final String SLAVE_DATA_DIR = "slave.data.dir";
    private static final String RPC_SERVER_CLASS = "rpc.server.class";
    private static final String SLAVE_RPC_SERVER_PORT = "slave.rpc.server.port";
    private static final String SLAVE_HEARTBEAT_CLASS = "slave.heartbeat.class";
    private static final String MASTER_RPC_SERVER_HOST = "master.rpc.server.host";
    private static final String MASTER_RPC_SERVER_PORT = "master.rpc.server.port";

    private static final int DATA_RECEIVE_PORT_BOUND = 40000;

    private Configuration conf;
    private RpcServer server;
    private Logger logger;
    private String dataDir, slaveId;
    private MasterProtocol master;
    private List<String> blocks = new ArrayList<>();
    private ExecutorService streams = Executors.newCachedThreadPool();

    public HyousaSlave(Configuration conf) throws IOException {
        this.conf = conf;
        this.logger = LoggerFactory.getLogger(HyousaSlave.class);
        this.dataDir = conf.get(SLAVE_DATA_DIR);
        dataDir = dataDir.endsWith("/") ? dataDir : dataDir + "/";
        try {
            this.slaveId = IpUtil.getLocal();
            Files.list(Paths.get(dataDir)).forEach(path -> blocks.add(path.getFileName().toString()));
            server = (RpcServer) Class.forName(conf.get(RPC_SERVER_CLASS)).getConstructor(Object.class).newInstance(this);
        } catch (IOException | ReflectiveOperationException e) {
            logger.error(e);
            System.exit(1);
        }
        //TODO: reflect
        this.master = new NettyMasterRpcClient(conf.get(MASTER_RPC_SERVER_HOST), conf.getInt(MASTER_RPC_SERVER_PORT), conf);
        String[] badBlocks = master.registerSlave(slaveId, blocks.toArray(new String[blocks.size()]));
        for (String badBlock : badBlocks) deleteBlock(badBlock);
        server.start(conf.getInt(SLAVE_RPC_SERVER_PORT), conf);
    }

    /**
     * Open a data receiver thread to prepare receive the block
     *
     * @param blockId The saved block ID
     * @return The data receiver bind port
     * @throws IOException If an I/O error occurs
     */
    public int saveBlock(String blockId) throws IOException {
        ServerSocket server = findPortAndBind();
        streams.execute(new DataReceiver(blockId, server));
        logger.log(1, "Saving block: " + blockId);
        return server.getLocalPort();
    }

    /**
     * Open a data streamer thread to prepare send the block
     *
     * @param blockId The saved block ID
     * @return The data streamer bind port
     * @throws IOException If an I/O error occurs
     */
    public int getBlock(String blockId) throws IOException {
        ServerSocket server = findPortAndBind();
        streams.execute(new DataStreamer(blockId, server));
        logger.log(1, "Sending block: " + blockId);
        return server.getLocalPort();
    }

    /**
     * Delete a block if exists
     *
     * @param blockId block slaveId generated from master
     * @return true if the block was deleted by this method;
     *         false if the block could not be deleted because it did not exist
     * @throws IOException if an I/O error occurs
     */
    public boolean deleteBlock(String blockId) throws IOException {
        logger.log(1, "Deleting block: " + blockId);
        return Files.deleteIfExists(Paths.get(dataDir+blockId));
    }

    //TODO
    public SlaveHeartbeatResponse sendHeartbeat() {
        try {
            return (SlaveHeartbeatResponse) ReflectUtil.newInstance(SLAVE_HEARTBEAT_CLASS);
        } catch (ReflectiveOperationException e) {
            //impossible
            logger.error(e);
            System.exit(1);
            return null;
        }
    }

    private ServerSocket findPortAndBind() {
        Random rand = new Random();
        while (true) try {
            int port = rand.nextInt(10000) + DATA_RECEIVE_PORT_BOUND;
            ServerSocket server = new ServerSocket();
            server.bind(new InetSocketAddress(port));
            return server;
        } catch (IOException e) {
            logger.log(1, "Retry find data stream port");
        }
    }

    private class DataReceiver implements Runnable {
        private Path block;
        private ServerSocket server;

        public DataReceiver(String blockId, ServerSocket server) throws IOException {
            this.block = Paths.get(dataDir+blockId);
            this.server = server;
        }

        public void run() {
            InputStream in = null;
            try {
                in = server.accept().getInputStream();
                Files.copy(in, block, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                logger.error(e);
            } finally {
                IOUtil.close(in, server);
            }
        }
    }

    private class DataStreamer implements Runnable {
        private Path block;
        private ServerSocket server;

        public DataStreamer(String blockId, ServerSocket server) throws IOException {
            this.block = Paths.get(dataDir+blockId);
            this.server = server;
        }

        public void run() {
            OutputStream out = null;
            try {
                out = server.accept().getOutputStream();
                Files.copy(block, out);
            } catch (IOException e) {
                logger.error(e);
            } finally {
                IOUtil.close(out, server);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        new HyousaSlave(new ConfigurationParser().load(args[0]));
    }
}

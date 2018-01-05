package hyousa.dfs.server.master;

import hyousa.common.conf.Configuration;
import hyousa.common.conf.ConfigurationParser;
import hyousa.common.log.Logger;
import hyousa.common.log.LoggerFactory;
import hyousa.common.rpc.server.RpcServer;
import hyousa.common.util.ReflectUtil;
import hyousa.dfs.server.master.protocol.BlockLocations;
import hyousa.dfs.server.master.protocol.FileStatus;
import hyousa.dfs.server.master.protocol.MasterHeartbeatResponse;
import hyousa.dfs.server.master.protocol.MasterProtocol;

import java.io.IOException;
import java.util.*;

/**
 * Created by yousa on 2017/12/13.
 */
public class HyousaMaster implements MasterProtocol {
    private static final String MASTER_HEARTBEAT_CLASS = "master.heartbeat.class";
    private static final String MASTER_BLOCK_REPLICATION = "master.block.replication";
    private static final String RPC_SERVER_CLASS = "rpc.server.class";
    private static final String MASTER_RPC_PORT = "master.rpc.server.port";

    private FsService service;
    private Configuration conf;
    private Logger logger;
    private RpcServer server;
    private Set<String> slaveIds;
    private int replica;

    public HyousaMaster(Configuration conf) throws IOException {
        logger = LoggerFactory.getLogger(HyousaMaster.class);
        this.conf = conf;
        try {
            server = (RpcServer) Class.forName(conf.get(RPC_SERVER_CLASS)).getConstructor(Object.class).newInstance(this);
        } catch (ReflectiveOperationException e) {
            logger.error(e);
            System.exit(1);
        }
        replica = conf.getInt(MASTER_BLOCK_REPLICATION);
        service = new FsService(conf);
        try {
            service.loadFsImage();
        } catch (IOException e) {
            service.format();
            service.dumpFsImage();
        }
        slaveIds = new HashSet<>();
        server.start(conf.getInt(MASTER_RPC_PORT), conf);
    }

    public String[] registerSlave(String slaveId, String[] blockIds) {
        slaveIds.add(slaveId);
        logger.log(1, "Slave: " + slaveId + " registered");
        return Arrays.stream(blockIds)
                    .filter(blockId -> !service.addBlockLocations(blockId, slaveId))
                    .toArray(String[]::new);
    }

    //TODO
    public MasterHeartbeatResponse sendHeartbeat() {
        try {
            return ((MasterHeartbeatResponse) ReflectUtil.newInstance(conf.get(MASTER_HEARTBEAT_CLASS)))
                    .slaves(slaveIds.toArray(new String[slaveIds.size()]));
        } catch (ReflectiveOperationException e) {
            //impossible
            logger.error(e);
            System.exit(1);
            return null;
        }
    }

    public String[] create(String name, String path, long size) throws NoSuchFileException, FileAlreadyExistsException {
        logger.log(1, "Creating file: " + name + " under path:" + path);
        return service.create(name, path, size);
    }

    public BlockLocations[] getBlockLocations(String path) throws NoSuchFileException {
        return service.getBlockLocations(path);
    }

    public void addBlockLocations(BlockLocations[] blocks) {
        for (BlockLocations block : blocks) {
            service.addBlockLocations(block.blockId(), block.slaveIds());
        }
    }

    public FileStatus[] listDir(String path) throws NoSuchFileException, FileAlreadyExistsException {
        return service.listDir(path);
    }

    public void rename(String path, String name) throws NoSuchFileException, FileAlreadyExistsException, InvalidPathException {
        service.rename(path, name);
    }

    public void mkdir(String...paths) throws FileAlreadyExistsException {
        for (String path : paths) {
            service.mkdir(path);
            logger.log(1, "Directory: " + path + " made");
        }
    }

    public void move(String src, String target) throws NoSuchFileException, FileAlreadyExistsException, InvalidPathException {
        service.move(src, target);
        logger.log(1, "Src: " + src + " moved to " + target);
    }

    public void remove(String path) throws NoSuchFileException, InvalidPathException {
        service.remove(path);
        logger.log(1, "Path: " + path + " removed");
    }

    public void dump() throws IOException {
        service.dumpFsImage();
    }

    public static void main(String[] args) throws IOException {
        new HyousaMaster(new ConfigurationParser().load(args[0]));
    }
}

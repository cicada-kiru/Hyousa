package hyousa.common.fs;

import hyousa.common.conf.Configuration;
import hyousa.common.conf.ConfigurationParser;
import hyousa.common.log.Logger;
import hyousa.common.log.LoggerFactory;
import hyousa.common.util.IOUtil;
import hyousa.dfs.server.master.FileAlreadyExistsException;
import hyousa.dfs.server.master.InvalidPathException;
import hyousa.dfs.server.master.NoSuchFileException;
import hyousa.dfs.server.master.netty.NettyMasterRpcClient;
import hyousa.dfs.server.master.netty.SerializableBlockLocations;
import hyousa.dfs.server.master.protocol.BlockLocations;
import hyousa.dfs.server.master.protocol.FileStatus;
import hyousa.dfs.server.master.protocol.MasterProtocol;
import hyousa.dfs.server.slave.netty.NettySlaveRpcClient;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.*;

/**
 * Created by yousa on 2017/12/12.
 */
public class DistributedFileSystem {
    private static final String MASTER_BLOCK_REPLICATION = "master.block.replication";
    private static final String SLAVE_RPC_SERVER_PORT = "slave.rpc.server.port";
    private static final String MASTER_RPC_SERVER_HOST = "master.rpc.server.host";
    private static final String MASTER_RPC_SERVER_PORT = "master.rpc.server.port";
    private static final String MASTER_BLOCK_SIZE = "master.block.size";

    private static Configuration conf;
    private static int blockSize;
    private static int slavePort;
    private static MasterProtocol master;
    private static NettySlaveRpcClient slave;
    private static final Logger logger = LoggerFactory.getLogger(DistributedFileSystem.class);

    public static void create(String localPath, String name, String path) throws IOException {
        File local = new File(localPath);
        String[] blockIds = master.create(name, path, local.length());
        BlockLocations[] blocks = new BlockLocations[blockIds.length];
        Random rand = new Random();
        int replica = conf.getInt(MASTER_BLOCK_REPLICATION);
        List<String> slaveIds = new LinkedList<>();
        Collections.addAll(slaveIds, master.sendHeartbeat().slaves());
        FileInputStream in = null;
        try {
            in = new FileInputStream(local);
            for (int i = 0; i < blockIds.length; i++) {
                blocks[i] = new SerializableBlockLocations().blockId(blockIds[i]);
                int remaining = slaveIds.size();
                List<String> sendSlaves = new ArrayList<>();
                byte[] data = new byte[blockSize];
                int total = in.read(data);
                if (total < 0) throw new EOFException(path);
                if (total < blockSize) {
                    byte[] temp = data;
                    data = new byte[total];
                    System.arraycopy(temp, 0, data, 0, total);
                }
                logger.log(1, "Sending Block: " + blockIds[i] + " to slaves");
                for (int j = 0; j < replica; j++) {
                    String sendSlave = slaveIds.remove(rand.nextInt(remaining--));
                    sendSlaves.add(sendSlave);
                    slave.setHostAndPort(sendSlave, slavePort);
                    int port = slave.saveBlock(blockIds[i]);
                    Socket socket = new Socket();
                    socket.connect(new InetSocketAddress(sendSlave, port));
                    socket.getOutputStream().write(data);
                    socket.close();
                }
                slaveIds.addAll(sendSlaves);
                blocks[i].slaveIds(sendSlaves.toArray(new String[sendSlaves.size()]));
                sendSlaves.clear();
            }
            if (in.read() != -1) throw new IOException("File has remaining");
            master.addBlockLocations(blocks);
            logger.log(1, "Complete create file: " + name);
        } catch (IOException e) {
            master.remove(path);
            logger.log(3, "Create file: " + name + " failed");
            throw e;
        } finally {
            IOUtil.close(in);
        }
    }

    public static void get(String path, String localPath) throws IOException {
        BlockLocations[] blocks = master.getBlockLocations(path);
        OutputStream out = new FileOutputStream(localPath);
        try {
            for (int i = 0; i < blocks.length;i ++) {
                String blockId = blocks[i].blockId();
                String[] slaveIds = blocks[i].slaveIds();
                byte[] data = null;
                int total = -2;
                for (int j = 0; j < slaveIds.length; j++) {
                    data = new byte[blockSize];
                    total = -2;
                    slave.setHostAndPort(slaveIds[j], slavePort);
                    Socket socket = null;
                    try {
                        int port = slave.getBlock(blockId);
                        socket = new Socket();
                        socket.connect(new InetSocketAddress(slaveIds[j], port));
                        InputStream in = socket.getInputStream();
                        int pos = 0;
                        while (pos < blockSize && total != -1) {
                            int l = in.read(data, pos, data.length-pos);
                            if (l < 0) total = l;
                            else pos += l;
                        }
                        if (total < 0) total = pos;
                        socket.close();
                        break;
                    } catch (IOException e) {
                        logger.log(1, "Connect slave: " + slaveIds[j] + " failed, try another slave");
                    } finally {
                        IOUtil.close(socket);
                    }
                }
                if (total == -2)
                    throw new IOException("Get file: " + path + " failed, already try connect all slave to get the block: " + blockId);
                if (total < 0 && i != blocks.length - 1) throw new EOFException(path);
                if (total < blockSize) {
                    byte[] temp = data;
                    data = new byte[total];
                    System.arraycopy(temp, 0, data, 0, total);
                }
                out.write(data);
                out.flush();
            }
        } finally {
            IOUtil.close(out);
        }
    }

    public static FileStatus[] listDir(String path) throws NoSuchFileException, FileAlreadyExistsException {
        FileStatus[] ls = master.listDir(path);
        for (FileStatus file : ls) {
            String ctime = file.ctime();
            ctime = ctime.substring(0, ctime.length()-4);
            System.out.println((file.isDirectory() ? "d" : "-") + "r-xr-xr-- " + file.size() + " " + ctime + " " + file.name());
        }
        return ls;
    }

    public static void mkdir(String...paths) throws FileAlreadyExistsException {
        master.mkdir(paths);
    }

    public static void remove(String path) throws NoSuchFileException, InvalidPathException {
        master.remove(path);
    }

    public static void rename(String path, String name) throws NoSuchFileException, FileAlreadyExistsException, InvalidPathException {
        master.rename(path, name);
    }

    public static void move(String src, String target) throws NoSuchFileException, FileAlreadyExistsException, InvalidPathException {
        master.move(src, target);
    }

    public static void init(String confPath) {
        conf = new ConfigurationParser().load(confPath);
        blockSize = conf.getInt(MASTER_BLOCK_SIZE);
        slavePort = conf.getInt(SLAVE_RPC_SERVER_PORT);
        master = new NettyMasterRpcClient(conf.get(MASTER_RPC_SERVER_HOST), conf.getInt(MASTER_RPC_SERVER_PORT), conf);
        slave = new NettySlaveRpcClient(null, 0, conf);
    }

    public static void dump() throws IOException {
        master.dump();
    }

    public static void main(String[] arvs) throws IOException {
        if (arvs.length < 2) {
            printUsage();
            return;
        }
        init(arvs[0]);
        String[] args = new String[arvs.length-1];
        System.arraycopy(arvs, 1, args, 0, args.length);
        switch (args[0]) {
            case "put":     if (checkArgsLength(args, 4)) create(args[1], args[3], args[2]);    return;
            case "get":     if (checkArgsLength(args, 3)) get(args[1], args[2]);                return;
            case "ls":      if (checkArgsLength(args, 2)) listDir(args[1]);                     return;
            case "mkdir":   String[] params = new String[args.length-1];
                            System.arraycopy(args, 1, params, 0, params.length);
                            mkdir(params);
                            return;
            case "rename":  if (checkArgsLength(args, 3)) rename(args[1], args[2]);             return;
            case "move":    if (checkArgsLength(args, 3)) move(args[1], args[2]);               return;
            case "dump":    if (checkArgsLength(args, 1)) dump();                               return;
            default:        printUsage();
        }
    }

    private static boolean checkArgsLength(String[] args, int length) {
        if (args.length != length) {
            printUsage();
            return false;
        }
        return true;
    }

    private static void printUsage() {
        System.out.println(
            "Usage:\n\n" +
            "put local_file_path dfs_path file_name\n\n" +
            "get dfs_file_path local_path\n\n" +
            "ls path\n\n" +
            "mkdir path1 [path2 [path3 ...]]\n\n" +
            "rename path new_name\n\n" +
            "move src target"
        );
    }
}

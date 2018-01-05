package hyousa.dfs.server.master;

import hyousa.common.conf.Configuration;
import hyousa.common.log.Logger;
import hyousa.common.log.LoggerFactory;
import hyousa.common.util.ReflectUtil;
import hyousa.dfs.server.master.protocol.BlockLocations;
import hyousa.dfs.server.master.protocol.FileStatus;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by yousa on 2017/12/15.
 */
public class FsService {
    private static final String BLOCK_LOCATIONS_CLASS = "master.block.locations.class";
    private static final String FILE_STATUS_CLASS = "master.file.status.class";
    private static final String MASTER_NAMESPACE_DIR = "master.namespace.dir";
    private static final String NAMESPACE_FILE_NAME = "root.obj";
    private static final String MASTER_BLOCK_SIZE = "master.block.size";

    private INodeDirectory root;
    private BlockManager blockManager;
    private Configuration conf;
    private Logger logger;
    private Class<?> blockLocationsClass;
    private Class<?> fileStatusClass;
    private final int blockSize;

    public FsService(Configuration conf) {
        this.conf = conf;
        logger = LoggerFactory.getLogger(FsService.class);
        blockManager = new BlockManager();
        try {
            blockLocationsClass = Class.forName(conf.get(BLOCK_LOCATIONS_CLASS));
            fileStatusClass = Class.forName(conf.get(FILE_STATUS_CLASS));
        } catch (ClassNotFoundException e) {
            //impossible
            logger.error(e);
            System.exit(1);
        }
        blockSize = conf.getInt(MASTER_BLOCK_SIZE);
    }

    private class BlockManager {
        private static final String MASTER_BLOCK_REPLICATION = "master.block.replication"; //TODO

        Map<String,List<String>> blocks = new HashMap<>(); // non-private to simplify FsService access

        public void registerBlock(String blockId) {
            //lazy load has no meaning
            blocks.put(blockId, new ArrayList<>());
        }

        public List<String> getBlockLocations(String blockId) {
            return blocks.get(blockId);
        }

        public boolean addBlockLocations(String blockId, String...slaveIds) {
            if (!blocks.containsKey(blockId)) {
                return false;
            }
            blocks.get(blockId).addAll(Arrays.asList(slaveIds));
            return true;
        }

        public void removeBlock(String blockId) {
            blocks.remove(blockId);
        }

        public String nextBlockId() {
            return Long.toString(System.nanoTime());
        }

        public String now() {
            return LocalDate.now().toString() + ' ' + LocalTime.now();
        }
    }

    //TODO: review dump and load method
    public void dumpFsImage() throws IOException {
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(conf.get(MASTER_NAMESPACE_DIR)+NAMESPACE_FILE_NAME));
        out.writeObject(root);
    }

    public void loadFsImage() throws IOException {
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(conf.get(MASTER_NAMESPACE_DIR)+NAMESPACE_FILE_NAME));
        try {
            root = (INodeDirectory) in.readObject();
        } catch (ClassNotFoundException e) {
            //impossible
            logger.error(e);
            System.exit(1);
        }
        root.walk(new ArrayList<>())
            .filter(INode::isFile)
            .flatMap(inode -> Arrays.stream(((INodeFile)inode).getBlocks()))
            .forEach(block -> blockManager.registerBlock(block));
    }

    public void format() {
        root = new INodeDirectory("/", null, blockManager.now());
    }

    /**
     * Create a file with the given name and blocks information
     *
     * @param name The created file name
     * @param path The file being created under the path
     * @param size The file length
     * @return The blocks of the file
     * @throws NoSuchFileException If the path dir not exists
     * @throws FileAlreadyExistsException If the path is a file
     */
    public String[] create(String name, String path, long size) throws NoSuchFileException, FileAlreadyExistsException {
        INodeDirectory dir = getDir(path);
        int n;
        if (size % blockSize == 0) n = (int) size / blockSize;
        else n = (int) (size / blockSize) + 1;
        String[] blocks = new String[n];
        for (int i = 0;i < n;i ++) {
            blocks[i] = blockManager.nextBlockId();
        }
        dir.addChild(new INodeFile(name, dir, blockManager.now(), size, blocks));
        for (String block : blocks) blockManager.registerBlock(block);
        return blocks;
    }

    public boolean addBlockLocations(String blockId, String...slaveIds) {
        return blockManager.addBlockLocations(blockId, slaveIds);
    }

    /**
     * Get the blocks of a file and the locations of the blocks
     *
     * @param path The file path
     * @return Block locations array
     * @throws NoSuchFileException If the path not exists or the path is a directory
     */
    public BlockLocations[] getBlockLocations(String path) throws NoSuchFileException {
        INodeFile file = getFile(path);
        String[] blocks = file.blocks;
        BlockLocations[] locationsArray = new BlockLocations[blocks.length];
        for (int i = 0;i < locationsArray.length;i ++) try {
            BlockLocations blockLocations = (BlockLocations) ReflectUtil.newInstance(conf.get(BLOCK_LOCATIONS_CLASS));
            List<String> blockLocationsList = blockManager.getBlockLocations(blocks[i]);
            blockLocations.blockId(blocks[i]).slaveIds(blockLocationsList.toArray(new String[blockLocationsList.size()]));
            locationsArray[i] = blockLocations;
        } catch (ReflectiveOperationException e) {
            //impossible
            logger.error(e);
            System.exit(1);
        }
        return locationsArray;
    }

    /**
     * List the files under the path
     *
     * @param path The listed path
     * @return The files under the path
     * @throws NoSuchFileException If the path dir not exists
     * @throws FileAlreadyExistsException If the path is a file
     */
    public FileStatus[] listDir(String path) throws NoSuchFileException, FileAlreadyExistsException {
        INodeDirectory dir = getDir(path);
        return dir.getChildren().stream().map(inode -> inode.asFileInfo(fileStatusClass)).toArray(FileStatus[]::new);
    }

    /**
     * Rename a file or a directory
     *
     * @param path The path to be renamed
     * @param name The path new name
     * @throws InvalidPathException If path is root directory
     * @throws NoSuchFileException If path not exists
     * @throws FileAlreadyExistsException If new name already in the path parent dir
     */
    public void rename(String path, String name) throws InvalidPathException, NoSuchFileException, FileAlreadyExistsException {
        checkRootPath(path);
        INode renamed = getINode(path);
        INodeDirectory dir = renamed.getParent();
        if (dir.hasChild(name)) throw new FileAlreadyExistsException("File "+name+" already exists");
        dir.removeChild(renamed.getName());
        renamed.setName(name);
        dir.addChild(renamed);
    }

    /**
     * Create a directory (or hierarchy of directories) with the given name
     *
     * @param path The path of the directory to be created
     * @throws FileAlreadyExistsException If path already exits
     */
    public void mkdir(String path) throws FileAlreadyExistsException {
        String[] dirs = parsePath(path);
        INode inode = root;
        for (int i = 0; i < dirs.length; i ++) {
            if (inode.isFile()) throw new FileAlreadyExistsException("Path "+dirs[i]+"is a file");
            INodeDirectory dir = (INodeDirectory) inode;
            if (!dir.hasChild(dirs[i])) {
                inode = new INodeDirectory(dirs[i], dir, blockManager.now());
                dir.addChild(inode);
            }
            else inode = dir.getChild(dirs[i]);
        }
    }

    private INodeDirectory getDir(String path) throws NoSuchFileException, FileAlreadyExistsException {
        if (path.equals("/")) return root;
        INode inode = getINode(path);
        if (inode.isFile()) throw new FileAlreadyExistsException("Path "+path+"is a file");
        return (INodeDirectory) inode;
    }

    private INodeFile getFile(String path) throws NoSuchFileException {
        INode inode = getINode(path);
        if (inode.isDirectory()) throw new NoSuchFileException("Path "+path+"is a directory");
        return (INodeFile) inode;
    }

    private String[] parsePath(String path) {
        return (path.endsWith("/") ? path.substring(1, path.length()-1) : path.substring(1)).split("/");
    }

    /**
     * Move src(a file or a directory) to target directory
     *
     * @param src The path to be moved
     * @param target The target path
     * @throws InvalidPathException If path is root directory, or src and target are the same
     * @throws FileAlreadyExistsException If target path is a file
     * @throws NoSuchFileException If src not exists
     */
    public void move(String src, String target) throws InvalidPathException, FileAlreadyExistsException, NoSuchFileException {
        checkRootPath(src);
        if (src.equals(target)) throw new InvalidPathException("'"+src+"' and '"+target+"' are the same file");
        INode move = getINode(src);
        INodeDirectory srcDir = move.getParent();
        INodeDirectory targetDir = target.equals("/") ? root : getDir(target);
        srcDir.removeChild(move.getName());
        targetDir.addChild(move);
    }

    /**
     * Delete the given file or directory from the file system
     *
     * @param path existing name
     * @throws InvalidPathException If path is root directory
     * @throws NoSuchFileException If the file or directory not exists
     */
    public void remove(String path) throws InvalidPathException, NoSuchFileException {
        checkRootPath(path);
        INode remove = getINode(path);
        INodeDirectory from = remove.getParent();
        from.removeChild(remove.getName());
        if (remove.isFile()) for (String block : ((INodeFile)remove).blocks) blockManager.removeBlock(block);
        else ((INodeDirectory)remove).walk(new ArrayList<>())
                .filter(INode::isFile)
                .flatMap(file -> Arrays.stream(((INodeFile)file).blocks))
                .forEach(block -> blockManager.removeBlock(block));
    }

    private void checkRootPath(String...paths) throws InvalidPathException {
        for (String path : paths)
            if (path.equals("/")) throw new InvalidPathException("Invalid path '/'");
    }

    private INode getINode(String path) throws NoSuchFileException {
        String[] paths = parsePath(path);
        return getINode(paths, paths.length-1);
    }

    private INode getINode(String[] paths, int offset) throws NoSuchFileException {
        INodeDirectory dir = root;
        for (int i = 0;i < offset;i ++) {
            dir = dir.getDir(paths[i]);
            if (dir == null) throw new NoSuchFileException(paths[i]+": No such directory");
        }
        INode inode = dir.getChild(paths[offset]);
        if (inode == null) throw new NoSuchFileException(paths[offset]+": No such file or directory");
        return inode;
    }
}

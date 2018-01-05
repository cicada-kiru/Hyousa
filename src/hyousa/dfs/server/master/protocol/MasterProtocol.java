package hyousa.dfs.server.master.protocol;

import hyousa.dfs.server.master.FileAlreadyExistsException;
import hyousa.dfs.server.master.InvalidPathException;
import hyousa.dfs.server.master.NoSuchFileException;

import java.io.IOException;

/**
 * Created by yousa on 2017/12/10.
 */
public interface MasterProtocol {
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
    String[] create(String name, String path, long size) throws NoSuchFileException, FileAlreadyExistsException;

    /**
     * Get the blocks of a file and the locations of the blocks
     *
     * @param path The file path
     * @return Block locations array
     * @throws NoSuchFileException If the path not exists or the path is a directory
     */
    BlockLocations[] getBlockLocations(String path) throws NoSuchFileException;

    /**
     * Add the blocks of a file to block manager
     *
     * @param blocks Block locations array
     */
    void addBlockLocations(BlockLocations[] blocks);

    /**
     * List the files under the path
     *
     * @param path The listed path
     * @return The files under the path
     * @throws NoSuchFileException If the path dir not exists
     * @throws FileAlreadyExistsException If the path is a file
     */
    FileStatus[] listDir(String path) throws NoSuchFileException, FileAlreadyExistsException;

    /**
     * Rename a file or a directory
     *
     * @param path The path to be renamed
     * @param name The path new name
     * @throws InvalidPathException If path is root directory
     * @throws NoSuchFileException If path not exists
     * @throws FileAlreadyExistsException If new name already in the path parent dir
     */
    void rename(String path, String name) throws NoSuchFileException, FileAlreadyExistsException, InvalidPathException;

    /**
     * Create directories (allows hierarchy of directories)
     *
     * @param paths The paths of the directories to be created
     * @throws FileAlreadyExistsException If path already exits
     */
    void mkdir(String...paths) throws FileAlreadyExistsException;

    /**
     * Move src(a file or a directory) to target directory
     *
     * @param src The path to be moved
     * @param target The target path
     * @throws InvalidPathException If path is root directory, or src and target are the same
     * @throws FileAlreadyExistsException If target path is a file
     * @throws NoSuchFileException If src not exists
     */
    void move(String src, String target) throws NoSuchFileException, FileAlreadyExistsException, InvalidPathException;

    /**
     * Delete the given file or directory from the file system
     *
     * @param path existing name
     * @throws InvalidPathException If path is root directory
     * @throws NoSuchFileException If the file or directory not exists
     */
    void remove(String path) throws NoSuchFileException, InvalidPathException;

    /**
     * Register slave to master
     *  @param slaveId The registered slave
     * @param blockIds The blocks slave maintaining
     */
    String[] registerSlave(String slaveId, String[] blockIds);

    /**
     * Send heartbeat to master
     *
     * @return The response of master
     */
    MasterHeartbeatResponse sendHeartbeat();

    //TODO: instead of edits log
    void dump() throws IOException;
}

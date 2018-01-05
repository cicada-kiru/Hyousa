package hyousa.dfs.server.master.protocol;

/**
 * Created by yousa on 2017/12/28.
 */
public interface FileStatus {
    String name();

    FileStatus name(String name);

    String ctime();

    FileStatus ctime(String ctime);

    long size();

    FileStatus size(long size);

    boolean isFile();

    FileStatus isFile(boolean isFile);

    boolean isDirectory();

    FileStatus isDirectory(boolean isDir);
}

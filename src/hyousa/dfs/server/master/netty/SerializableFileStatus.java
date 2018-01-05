package hyousa.dfs.server.master.netty;

import hyousa.dfs.server.master.protocol.FileStatus;

import java.io.Serializable;

/**
 * Created by yousa on 2018/1/2.
 */
public class SerializableFileStatus implements FileStatus, Serializable {
    private String name, ctime;
    private long size;
    private boolean isFile, isDir;

    public String name() {
        return name;
    }

    public FileStatus name(String name) {
        this.name = name;
        return this;
    }

    public String ctime() {
        return ctime;
    }

    public FileStatus ctime(String ctime) {
        this.ctime = ctime;
        return this;
    }

    public long size() {
        return size;
    }

    public FileStatus size(long size) {
        this.size = size;
        return this;
    }

    public boolean isFile() {
        return isFile;
    }

    public FileStatus isFile(boolean isFile) {
        this.isFile = isFile;
        return this;
    }

    public boolean isDirectory() {
        return isDir;
    }

    public FileStatus isDirectory(boolean isDir) {
        this.isDir = isDir;
        return this;
    }
}

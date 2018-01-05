package hyousa.dfs.server.master;

import hyousa.common.util.ReflectUtil;
import hyousa.dfs.server.master.protocol.FileStatus;

import java.io.Serializable;

/**
 * Created by yousa on 2017/12/15.
 */
public abstract class INode implements Serializable {
    private String name, ctime;
    private INodeDirectory parent;

    public INode(String name, INodeDirectory parent, String ctime) {
        this.name = name;
        this.parent = parent;
        this.ctime = ctime;
    }

    public INodeDirectory getParent() {
        return parent;
    }

    public void setParent(INodeDirectory parent) {
        this.parent = parent;
    }

    public abstract boolean isDirectory();

    public abstract boolean isFile();

    public abstract long size();

    public String ctime() {
        return ctime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FileStatus asFileInfo(Class<?> fileInfoClass) {
        try {
            return ((FileStatus) ReflectUtil.newInstance(fileInfoClass))
                    .name(name).ctime(ctime).size(size())
                    .isFile(isFile()).isDirectory(isDirectory());
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return null;
        }
    }
}

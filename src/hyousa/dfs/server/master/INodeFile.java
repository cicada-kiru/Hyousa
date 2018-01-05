package hyousa.dfs.server.master;

/**
 * Created by yousa on 2017/12/15.
 */
public class INodeFile extends INode {
    private long size;
    String[] blocks;

    public INodeFile(String name, INodeDirectory parent, String ctime, long size, String[] blocks) {
        super(name, parent, ctime);
        this.size = size;
        this.blocks = blocks;
    }

    public String[] getBlocks() {
        return blocks;
    }

    public void setBlocks(String[] blocks) {
        this.blocks = blocks;
    }

    public boolean isDirectory() {
        return false;
    }

    public boolean isFile() {
        return true;
    }

    public long size() {
        return size;
    }

    public String toString() {
        return "File[" + getName() + "]";
    }
}

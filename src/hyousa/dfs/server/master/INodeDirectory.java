package hyousa.dfs.server.master;

import java.util.*;
import java.util.stream.Stream;

/**
 * Created by yousa on 2017/12/15.
 */
public class INodeDirectory extends INode {
    private Map<String,INodeDirectory> dirs = new HashMap<>();
    private Map<String,INodeFile> files = new HashMap<>();

    public INodeDirectory(String name, INodeDirectory parent, String ctime, INode...children) {
        super(name, parent, ctime);
        for (INode child : children) {
            if (child.isDirectory()) dirs.put(child.getName(), (INodeDirectory)child);
            else if (child.isFile()) files.put(child.getName(), (INodeFile)child);
        }
    }

    public void addChild(INode child) throws FileAlreadyExistsException {
        String name = child.getName();
        if (child.isFile()) {
            if (files.containsKey(name)) throw new FileAlreadyExistsException("File "+name+" already exists");
            else files.put(name, (INodeFile) child);
        } else if (child.isDirectory()) {
            if (dirs.containsKey(name)) throw new FileAlreadyExistsException("Directory "+name+" already exists");
            else dirs.put(name, (INodeDirectory)child);
        }
    }

    public INode getChild(String name) {
        if (hasFile(name)) return getFile(name);
        return getDir(name);
    }

    public boolean hasChild(String name) {
        return hasFile(name) || hasDir(name);
    }

    public INode removeChild(String name) throws NoSuchFileException {
        if (files.containsKey(name)) return files.remove(name);
        else if (dirs.containsKey(name)) return dirs.remove(name);
        else throw new NoSuchFileException(name);
    }

    public Collection<INode> getChildren() {
        Collection<INode> children = new ArrayList<>(files.size()+dirs.size());
        children.addAll(files.values());
        children.addAll(dirs.values());
        return children;
    }

    public INodeFile getFile(String name) {
        return files.get(name);
    }

    public Collection<INodeFile> getFiles() {
        return files.values();
    }

    public boolean hasFile(String name) {
        return files.containsKey(name);
    }

    public INodeDirectory getDir(String name) {
        return dirs.get(name);
    }

    public Collection<INodeDirectory> getDirs() {
        return dirs.values();
    }

    public boolean hasDir(String name) {
        return dirs.containsKey(name);
    }

    public boolean isDirectory() {
        return true;
    }

    public boolean isFile() {
        return false;
    }

    public long size() {
        return 0;
    }

    public String toString() {
        return "Dir["+getName()+"]";
    }

    public String tree() {
        return tree(new StringBuilder(), this, "").toString();
    }

    private StringBuilder tree(StringBuilder builder, INodeDirectory parent, String level) {
        for (INodeFile file : parent.getFiles()) builder.append(level).append(file).append("\n");
        for (INodeDirectory dir : parent.getDirs()) {
            builder.append(level).append(dir).append("\n");
            tree(builder, dir, level+"\t");
        }
        return builder;
    }

    public Stream<INode> walk(List<INode> list) {
        list.add(this);
        list.addAll(files.values());
        for (INodeDirectory dir : dirs.values()) dir.walk(list);
        return list.stream();
    }
}

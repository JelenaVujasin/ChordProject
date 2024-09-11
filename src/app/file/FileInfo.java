package app.file;

import app.ChordState;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
public class FileInfo implements Serializable {

    @Serial
    private static final long serialVersionUID = 463426265374700139L;

    private final String path;
    private final String content;
    private boolean isPrivate;
    private final int OriginalNode;

    public FileInfo(String path, String content, boolean isPrivate, int originalNode) {
        this.path = path;
        this.content = content;
        this.isPrivate = isPrivate;
        OriginalNode = originalNode;
    }

    public FileInfo(String path, String content, int originalNode) {
        this.path = path;
        this.content = content;
        OriginalNode = originalNode;
        this.isPrivate = false;

    }

    public FileInfo(String path, int originalNode) {
        this (path, "", false, originalNode);
    }

    public FileInfo(FileInfo fileInfo) {
        this(fileInfo.getPath(), fileInfo.getContent(), fileInfo.isPrivate(), fileInfo.getOriginalNode());
    }

    public String getPath() {
        return path;
    }

    public String getContent() {
        return content;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public int getOriginalNode() {
        return OriginalNode;
    }

    @Override
    public int hashCode() {
        return ChordState.chordHash(getPath());
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof FileInfo)return o.hashCode() == this.hashCode();
        return false;

    }

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }


}

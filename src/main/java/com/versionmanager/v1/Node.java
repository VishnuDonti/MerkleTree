package com.versionmanager.v1;

import java.io.Serializable;
import java.util.List;

public class Node implements Serializable {

    private Node parent;
    private Node left;
    private Node right;
    private boolean leaf;
    private short level;
    private String resourceId;
    private List<Deleted> deletedList;
    private List<Hash> hashList;

    public boolean isLeaf() {
        return leaf;
    }

    public void setLeaf(boolean leaf) {
        this.leaf = leaf;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public List<Deleted> getDeletedList() {
        return deletedList;
    }

    public void setDeletedList(List<Deleted> deletedList) {
        this.deletedList = deletedList;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public Node getLeft() {
        return left;
    }

    public void setLeft(Node left) {
        this.left = left;
    }

    public Node getRight() {
        return right;
    }

    public void setRight(Node right) {
        this.right = right;
    }


    public short getLevel() {
        return level;
    }

    public void setLevel(short level) {
        this.level = level;
    }

    public List<Hash> getHashList() {
        return hashList;
    }

    public void setHashList(List<Hash> hashList) {
        this.hashList = hashList;
    }
}

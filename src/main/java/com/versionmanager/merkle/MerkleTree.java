package com.versionmanager.merkle;

import java.io.Serializable;
import java.util.List;

public class MerkleTree implements Serializable {


    private List<Node> root;
    private Node start;
    private Node end;
    private boolean isOdd;
    private List<Node> leaves;

    public List<Node> getLeaves() {
        return leaves;
    }

    public void setLeaves(List<Node> leaves) {
        this.leaves = leaves;
    }

    public Node getStart() {
        return start;
    }

    public void setStart(Node start) {
        this.start = start;
    }

    public Node getEnd() {
        return end;
    }

    public void setEnd(Node end) {
        this.end = end;
    }

    public boolean isOdd() {
        return isOdd;
    }

    public void setOdd(boolean odd) {
        isOdd = odd;
    }

    public List<Node> getRoot() {
        return root;
    }

    public void setRoot(List<Node> root) {
        this.root = root;
    }
}

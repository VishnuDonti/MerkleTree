package com.versionmanager.v3.model;

import lombok.Data;

@Data
public class Node {
    private Short version;
    private Node left;
    private Node right;
    int index;
    public Node(Short version) {
        this.version = version;
    }
}

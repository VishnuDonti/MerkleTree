package com.versionmanager.v2.model;

import lombok.Data;

import java.util.List;

@Data
public class MerkleTree {
    private Node root;
    private int leavesCount;
    private List<Short> versionInfo;
}

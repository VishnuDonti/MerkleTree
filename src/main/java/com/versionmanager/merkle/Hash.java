package com.versionmanager.merkle;

import java.io.Serializable;

public class Hash implements Serializable {

    private String hashValue;
    private int rootIndex;

    public String getHashValue() {
        return hashValue;
    }

    public void setHashValue(String hashValue) {
        this.hashValue = hashValue;
    }

    public int getRootIndex() {
        return rootIndex;
    }

    public void setRootIndex(int rootIndex) {
        this.rootIndex = rootIndex;
    }
}

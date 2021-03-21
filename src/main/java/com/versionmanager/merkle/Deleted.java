package com.versionmanager.merkle;

import java.io.Serializable;

public class Deleted implements Serializable {
    private boolean isDeleted;
    private int rootIndex;

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public int getRootIndex() {
        return rootIndex;
    }

    public void setRootIndex(int rootIndex) {
        this.rootIndex = rootIndex;
    }
}

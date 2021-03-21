package com.versionmanager.merkle;

import java.util.List;

public class Difference {

    private List<String> updated;
    private List<String> deleted;

    public List<String> getUpdated() {
        return updated;
    }

    public void setUpdated(List<String> updated) {
        this.updated = updated;
    }

    public List<String> getDeleted() {
        return deleted;
    }

    public void setDeleted(List<String> deleted) {
        this.deleted = deleted;
    }
}

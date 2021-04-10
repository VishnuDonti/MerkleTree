package com.versionmanager.v3.model;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class Difference {
    private List<String> updated;
    private List<String> deleted;
    private String versionId;
}

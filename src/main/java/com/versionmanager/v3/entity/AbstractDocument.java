package com.versionmanager.v3.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class AbstractDocument  {
    @Id
    private String id;

}

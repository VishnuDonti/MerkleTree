package com.versionmanager.v2.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class AbstractDocument  {
    @Id
    private String id;

}

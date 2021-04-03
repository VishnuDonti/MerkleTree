package com.versionmanager.merkle2.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Event {

    private String id;
    private String hash;
    private boolean removed = false;
}

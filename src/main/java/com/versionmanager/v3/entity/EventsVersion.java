package com.versionmanager.v3.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document("EventsVersion")
@AllArgsConstructor
@NoArgsConstructor
public class EventsVersion extends AbstractDocument {

    // eventsStore Id
    private String eventStoreId;
    // VersionInfo List.of(1,1,1)
    private byte[] versionInfo;
    // total number of leaves in the version
    private int leavesCount;
    // Created Date
    private Date createdDate;

}

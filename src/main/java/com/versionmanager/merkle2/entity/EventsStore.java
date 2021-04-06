package com.versionmanager.merkle2.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document("EventsStore")
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class EventsStore extends  AbstractDocument {
    // List of the events for the customer
    private List<Event> events;

    private String latestVersion;

}

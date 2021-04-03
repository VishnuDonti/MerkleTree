package com.versionmanager.merkle2.service;

import com.versionmanager.merkle2.model.*;
import com.versionmanager.merkle2.entity.Event;

import java.util.List;

public interface IMerkleService {

    void updateOrAddEvent(Event event, String eventStoreId);

    void constructVersion(String eventsStoreId, List<Event> events);

    Difference getUpdates(String eventsStoreId, String versionId);

}

package com.versionmanager.v3.service;

import com.versionmanager.v3.entity.Event;
import com.versionmanager.v3.model.Difference;

import java.util.List;

public interface IMerkleService {

    void updateOrAddEvent(Event event, String eventStoreId);

    void constructVersion(String eventsStoreId, List<Event> events);

    Difference getUpdates(String eventsStoreId, String versionId);

}

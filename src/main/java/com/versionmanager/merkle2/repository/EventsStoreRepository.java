package com.versionmanager.merkle2.repository;


import com.versionmanager.merkle2.entity.AbstractDocument;
import com.versionmanager.merkle2.entity.EventsStore;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventsStoreRepository extends MongoRepository<EventsStore,String> {

    Optional<EventsStore> findEventIdById(String id);

}

package com.versionmanager.v3.repository;


import com.versionmanager.v3.entity.EventsStore;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventsStoreRepository extends MongoRepository<EventsStore,String> {

    Optional<EventsStore> findEventIdById(String id);

    @Query(fields = "{'latestVersion':1}",value = "{'id' : ?0}")
    Optional<EventsStore> findLatestVersionById(String id);

}

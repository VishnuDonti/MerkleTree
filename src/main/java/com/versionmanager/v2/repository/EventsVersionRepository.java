package com.versionmanager.v2.repository;

import com.versionmanager.v2.entity.EventsVersion;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventsVersionRepository extends MongoRepository<EventsVersion,String> {

    Optional<EventsVersion> findTop1ByEventStoreIdOrderByCreatedDateDesc(String eventStoreId);

    List<EventsVersion> findTop3ByEventStoreIdOrderByCreatedDateDesc(String eventStoreId);

    List<EventsVersion> findTop8ByEventStoreIdOrderByCreatedDateDesc(String eventStoreId);

    List<EventsVersion> findTop80ByEventStoreIdOrderByCreatedDateDesc(String eventStoreId);

    List<EventsVersion> findTop88ByEventStoreIdOrderByCreatedDateDesc(String eventStoreId);

    List<EventsVersion> findTop1000ByEventStoreIdOrderByCreatedDateDesc(String eventStoreId);

    Optional<EventsVersion> findByEventStoreIdAndId(String eventStoreId, String id);

}

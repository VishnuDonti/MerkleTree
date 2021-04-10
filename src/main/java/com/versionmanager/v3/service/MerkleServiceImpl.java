package com.versionmanager.v3.service;

import com.versionmanager.v3.entity.Event;
import com.versionmanager.v3.entity.EventsVersion;
import com.versionmanager.v3.entity.EventsStore;
import com.versionmanager.v3.model.Difference;
import com.versionmanager.v3.model.MerkleTree;
import com.versionmanager.v3.model.Node;
import com.versionmanager.v3.repository.EventsStoreRepository;
import com.versionmanager.v3.repository.EventsVersionRepository;
import com.versionmanager.v3.utility.EventStoreCreator;
import com.versionmanager.v3.utility.MemoizedValues;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.bson.Document;
import org.springframework.data.domain.Range;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import static com.versionmanager.v2.constants.MerkleConstants.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class MerkleServiceImpl implements IMerkleService {

    private final EventsStoreRepository eventsStoreRepository;

    private final EventsVersionRepository eventsVersionRepository;

    private final MongoTemplate mongoTemplate;

    @Override
    public void updateOrAddEvent(Event event, String eventStoreId) {
        // Check if Event is present
        // DB Call 1
        MatchOperation matchOperation = Aggregation.match(Criteria.where("_id").is(eventStoreId));
        ArrayOperators.IndexOfArray indexOfArray = ArrayOperators.IndexOfArray.arrayOf(EVENTS_ID).indexOf(event.getId())
                .within(Range.from(Range.Bound.inclusive(0l)).to(Range.Bound.unbounded()));
        ProjectionOperation indexOfElement = Aggregation.project("latestVersion").and(indexOfArray).as(INDEX_OF_EVENT).and(ArrayOperators.Size.lengthOfArray(EVENTS)).as(NUMBER_OF_EVENTS);
        AggregationResults<Document> integerAggregationResults = mongoTemplate.aggregate(Aggregation.newAggregation(matchOperation, indexOfElement), EVENTS_STORE, Document.class);
        Document uniqueMappedResult = integerAggregationResults.getUniqueMappedResult();

        if (Objects.isNull(uniqueMappedResult)) {
            log.info("No Events/Version/Customer found for Addition or Update");
            return;
        }
        //  DB Call 2
        Optional<EventsVersion> eventsStoreRepositoryById = eventsVersionRepository.findById((String) uniqueMappedResult.get("latestVersion"));
        EventsVersion eventsVersion = eventsStoreRepositoryById.get();
        int leavesCount = eventsVersion.getLeavesCount();
        List<Short> desrializedVersionInfo = (List<Short>) SerializationUtils.deserialize(eventsVersion.getVersionInfo());
        int indexOfGivenEvent = ((int) (Math.pow(2, levels(desrializedVersionInfo.size()) - 1) + (Integer) uniqueMappedResult.get(INDEX_OF_EVENT)) - 1);
        // Updating an existing event
        if ((Integer) uniqueMappedResult.get(INDEX_OF_EVENT) != -1) {
            desrializedVersionInfo.set(indexOfGivenEvent, (short) (desrializedVersionInfo.get(indexOfGivenEvent) + 1));
            int upperLevelIndex = (indexOfGivenEvent - 1) / 2;
            while (upperLevelIndex > 0) {
                desrializedVersionInfo.set(upperLevelIndex, (short) (desrializedVersionInfo.get(upperLevelIndex) + 1));
                upperLevelIndex = (upperLevelIndex - 1) / 2;
            }
            desrializedVersionInfo.set(upperLevelIndex, (short) (desrializedVersionInfo.get(upperLevelIndex) + 1));

            // Create a new version
            EventsVersion eventsVersion1 = new EventsVersion();
            eventsVersion1.setCreatedDate(new Date());
            eventsVersion1.setLeavesCount(leavesCount);
            eventsVersion1.setEventStoreId(eventStoreId);
            eventsVersion1.setVersionInfo(SerializationUtils.serialize(desrializedVersionInfo));
            EventsVersion storedEventsVersion = eventsVersionRepository.save(eventsVersion1);

            // DB Call 3
            Update update = new Update().set("events.$.hash", event.getHash()).set("events.$.removed", event.isRemoved()).set("latestVersion", storedEventsVersion.getId());
            mongoTemplate.updateFirst(Query.query(Criteria.where("_id").is(eventStoreId).and(EVENTS_ID).is(event.getId())), update, EVENTS_STORE);
        } else {
            leavesCount++;
            double levels = log2(uniqueMappedResult.getInteger(NUMBER_OF_EVENTS));
            if (levels - Math.floor(levels) == 0) {
                // Add a new level
                int upperLevelIndex = (desrializedVersionInfo.size() - 1) / 2;
                while (upperLevelIndex > 0) {
                    int numberOfNodes = (int) Math.pow(2, --levels);
                    for (int k = 0; k < numberOfNodes; k++) {
                        desrializedVersionInfo.add(upperLevelIndex + k, (short) 0);
                    }
                    upperLevelIndex = (upperLevelIndex - 1) / 2;
                }
                desrializedVersionInfo.add(upperLevelIndex, (short) 0);
            } else {
                int upperLevelIndex = (desrializedVersionInfo.size() - 1) / 2;
                while (upperLevelIndex > 0) {
                    desrializedVersionInfo.set(upperLevelIndex, (short) (desrializedVersionInfo.get(upperLevelIndex) + 1));
                    upperLevelIndex = (upperLevelIndex - 1) / 2;
                }
                desrializedVersionInfo.set(upperLevelIndex, (short) (desrializedVersionInfo.get(upperLevelIndex) + 1));
            }
            desrializedVersionInfo.add((short) 0);
            // Create a new version
            EventsVersion eventsVersion1 = new EventsVersion();
            eventsVersion1.setCreatedDate(new Date());
            eventsVersion1.setLeavesCount(leavesCount);
            eventsVersion1.setEventStoreId(eventStoreId);
            eventsVersion1.setVersionInfo(SerializationUtils.serialize(desrializedVersionInfo));
            EventsVersion storedEventsVersion = eventsVersionRepository.save(eventsVersion1);

            Update update = new Update();
            update.addToSet(EVENTS, event).set("latestVersion", storedEventsVersion.getId());
            mongoTemplate.updateFirst(Query.query(Criteria.where("_id").is(eventStoreId)), update, EVENTS_STORE);
        }
    }

    @Override
    public void constructVersion(String eventsStoreId, List<Event> events) {
        if (Objects.isNull(events) || events.isEmpty()) {
            return;
        }
        // Create Version
        EventsVersion eventsVersion = new EventsVersion();
        eventsVersion.setEventStoreId(EventStoreCreator.getEvenStoreId());
        int levels = levels(events.size());
        List<Short> versionInfo = new LinkedList<>();
        // Fill the levels from top to bottom except 0 level
        for (int i = 0; i <= levels - 1; i++) {
            int numberOfINodes = (int) Math.pow(2, i);
            for (int k = 0; k < numberOfINodes; k++)
                versionInfo.add((short) 0);
        }
        // Fill level 0
        events.stream().forEach(x -> versionInfo.add((short) 0));
        // Version Info
        eventsVersion.setVersionInfo(SerializationUtils.serialize(versionInfo));
        // leaves count
        eventsVersion.setLeavesCount(events.size());
        // set version Id
        eventsVersion.setCreatedDate(new Date());
        EventsVersion storedVersion = eventsVersionRepository.save(eventsVersion);

        // Events Store
        EventsStore eventsStore = new EventsStore();
        eventsStore.setEvents(events);
        eventsStore.setId(eventsStoreId);
        eventsStore.setLatestVersion(storedVersion.getId());
        eventsStoreRepository.save(eventsStore);
    }

    @Override
    public Difference getUpdates(String eventsStoreId, String versionId) {
        String latestVersion1 = eventsStoreRepository.findLatestVersionById(eventsStoreId).get().getLatestVersion();
        List<EventsVersion> collect = StreamSupport.stream(eventsVersionRepository.findAllById(List.of(latestVersion1, versionId)).spliterator(), false).collect(Collectors.toList());
        if (collect.size() == 1) {
            Difference difference = new Difference();
            difference.setUpdated(eventsStoreRepository.findEventIdById(eventsStoreId).get().getEvents().stream().filter(x -> !x.isRemoved()).map(y -> y.getId()).collect(Collectors.toList()));
            difference.setVersionId(collect.get(0).getId());
            return difference;
        }
        List<Short> latestVersionInfo = (List<Short>) SerializationUtils.deserialize(collect.get(1).getVersionInfo());
        List<Short> givenVersionInfo = (List<Short>) SerializationUtils.deserialize(collect.get(0).getVersionInfo());
        Difference difference = compare(givenVersionInfo, latestVersionInfo, eventsStoreId, collect.get(0).getLeavesCount(), collect.get(1).getLeavesCount());
        return difference;
    }

    private Difference compare(List<Short> givenVersion, List<Short> latestVersion, String eventsStoreId, int givenVersionLeafCount, int latestVersionLeafCount) {
        int levelsOfGivenVersion = levels(givenVersion.size());
        int levelsOfLatestVersion = levels(latestVersion.size());
        int diffOfLevels = levelsOfLatestVersion - levelsOfGivenVersion;
        List<Integer> indexes = new ArrayList<>();
        if (diffOfLevels == 0) {
            compareLevelTree(givenVersion, latestVersion, indexes, 0, givenVersionLeafCount, (int) Math.pow(2, levelsOfLatestVersion - 1));
        } else {
            indexes = IntStream.range(givenVersionLeafCount, latestVersionLeafCount).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
            compareDiffLevelTree(givenVersion, latestVersion, indexes, 0, givenVersionLeafCount, (int) Math.pow(2, levelsOfGivenVersion-1),diffOfLevels);
        }
        Difference difference = new Difference();
        MatchOperation selectCustomer = Aggregation.match(Criteria.where("_id").is(eventsStoreId));
        UnwindOperation unwindOperation = Aggregation.unwind("events", "indexOfElement");
        MatchOperation matchOperation = Aggregation.match(Criteria.where("indexOfElement").in(indexes));
        ProjectionOperation project = Aggregation.project().and("events._id").as("eventId").andExclude("_id").and("events.removed").as("eventRemoved");
        List<Document> documents = mongoTemplate.aggregate(Aggregation.newAggregation(selectCustomer, unwindOperation, matchOperation, project), "EventsStore", Document.class).getMappedResults();
        List<String> deletedIds = new ArrayList<>();
        List<String> updatedIds = new ArrayList<>();
        documents.stream().forEach(y -> {
            if ((Boolean) y.get("eventRemoved")) {
                deletedIds.add((String) y.get("eventId"));
            } else {
                updatedIds.add((String) y.get("eventId"));
            }
        });
        difference.setUpdated(updatedIds);
        difference.setDeleted(deletedIds);
        return difference;
    }

    private void compareDiffLevelTree(List<Short> givenVersion, List<Short> latestVersion, List<Integer> indexes, int index, int givenVersionLeafCount, int nonLeafCount, int diffOfLevels) {
        if (index > givenVersion.size() - 1) {
            return;
        }
        if (givenVersion.get(index).shortValue() == latestVersion.get(MemoizedValues.levelIndex((short) index,diffOfLevels)).shortValue()) {
            return;
        }
        if (index >= givenVersion.size() - givenVersionLeafCount) {
            indexes.add(index - nonLeafCount + 1);
            return;
        }
        compareDiffLevelTree(givenVersion, latestVersion, indexes, (2 * index) + 1, givenVersionLeafCount, nonLeafCount,diffOfLevels);
        compareDiffLevelTree(givenVersion, latestVersion, indexes, (2 * index) + 2, givenVersionLeafCount, nonLeafCount,diffOfLevels);
    }

    private void compareLevelTree(List<Short> givenVersion, List<Short> latestVersion, List<Integer> indexes, int index, int givenVersionLeafCount, int nonLeafCount) {
        if (index > latestVersion.size() - 1) {
            return;
        }
        if (index > givenVersion.size() - 1) {
            indexes.add(index - nonLeafCount + 1);
            return;
        }
        if (givenVersion.get(index).shortValue() == latestVersion.get(index).shortValue()) {
            return;
        }
        if (index >= givenVersion.size() - givenVersionLeafCount) {
            indexes.add(index - nonLeafCount + 1);
            return;
        }
        compareLevelTree(givenVersion, latestVersion, indexes, (2 * index) + 1, givenVersionLeafCount, nonLeafCount);
        compareLevelTree(givenVersion, latestVersion, indexes, (2 * index) + 2, givenVersionLeafCount, nonLeafCount);
    }

    private int levels(int eventsSize) {
        return (int) Math.ceil(log2(eventsSize));
    }

    private double log2(int eventsSize) {
        return Math.log10(eventsSize) / Math.log10(2);
    }

}

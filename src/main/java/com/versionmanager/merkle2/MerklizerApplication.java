package com.versionmanager.merkle2;

import com.versionmanager.merkle2.entity.Event;
import com.versionmanager.merkle2.entity.EventsVersion;
import com.versionmanager.merkle2.repository.EventsStoreRepository;
import com.versionmanager.merkle2.repository.EventsVersionRepository;
import com.versionmanager.merkle2.service.MerkleServiceImpl;
import com.versionmanager.merkle2.utility.EventStoreCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@SpringBootApplication
public class MerklizerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MerklizerApplication.class);
    }


    @Bean
    public ApplicationListener<ApplicationReadyEvent> init(){

        return new ApplicationListener<ApplicationReadyEvent>() {

            @Autowired
            private EventsVersionRepository eventsVersionRepository;

            @Autowired
            private EventsStoreRepository eventsStoreRepository;


            @Autowired
            private MongoTemplate mongoTemplate;

            @Autowired
            private MerkleServiceImpl merkleService;

            @Override
            @Async
            public void onApplicationEvent(ApplicationReadyEvent event) {
                eventsVersionRepository.deleteAll();
                eventsStoreRepository.deleteAll();
                List<Event> events = EventStoreCreator.createEvents(1, 88);
                merkleService.constructVersion(EventStoreCreator.getEvenStoreId(),events);
                int count = 2;
                while(count <= 88) {
                    Event event1 = EventStoreCreator.createEvents(count+1, count + 2).get(0);
                    merkleService.updateOrAddEvent(event1, EventStoreCreator.getEvenStoreId());
                    count++;
                }
                EventsVersion eventsVersion = eventsVersionRepository.findTop80ByEventStoreIdOrderByCreatedDateDesc(EventStoreCreator.getEvenStoreId()).get(56);
                System.out.println(merkleService.getUpdates(EventStoreCreator.getEvenStoreId(),eventsVersion.getId()+1).getUpdated().size());
            }
        };
    }

}

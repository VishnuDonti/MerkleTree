package com.versionmanager.v2;

import com.versionmanager.v2.entity.EventsVersion;
import com.versionmanager.v2.repository.EventsStoreRepository;
import com.versionmanager.v2.repository.EventsVersionRepository;
import com.versionmanager.v2.service.MerkleServiceImpl;
import com.versionmanager.v2.utility.EventStoreCreator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.Async;

@SpringBootApplication
@Slf4j
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
//                eventsVersionRepository.deleteAll();
//                eventsStoreRepository.deleteAll();
                StopWatch stopWatch = new StopWatch();
//                List<Event> events = EventStoreCreator.createEvents(1, 3000);
                stopWatch.start();
//                merkleService.constructVersion(EventStoreCreator.getEvenStoreId(),events);
                stopWatch.stop();
                log.info(" Time Taken to construct Version {} {}" , 0 ,stopWatch.getTime());
                stopWatch.reset();
//                int count = 670;
//                int i = 1;
//                while(count <= 12000) {
//                    Event event1 = EventStoreCreator.createEvents(count+1, count + 2).get(0);
//                    stopWatch.start();
//                    merkleService.updateOrAddEvent(event1, EventStoreCreator.getEvenStoreId());
//                    stopWatch.stop();
//                    log.info(" Time Taken to construct Version  {} {}" , i ,stopWatch.getTime());
//                    stopWatch.reset();
//                    count++;
//                    i++;
//                }
                EventsVersion eventsVersion = eventsVersionRepository.findTop88ByEventStoreIdOrderByCreatedDateDesc(EventStoreCreator.getEvenStoreId()).get(86);
                stopWatch.start();
                System.out.println(merkleService.getUpdates(EventStoreCreator.getEvenStoreId(),eventsVersion.getId()).getUpdated().size());
                stopWatch.stop();
                log.info(" Get updates {} {}" , 22 ,stopWatch.getTime());
                stopWatch.reset();
            }
        };
    }

}

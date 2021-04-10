package com.versionmanager.v3.utility;

import com.versionmanager.v3.entity.Event;

import java.util.List;
import java.util.UUID;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class EventStoreCreator {

    // Generate 16000 UniqueIds at the start
    public static  List<String> uniqueIds = IntStream.rangeClosed(0, 16001).mapToObj(x -> UUID.randomUUID().toString()).collect(Collectors.toList());

    public static String getEvenStoreId(){
        return "customer1";
    }

    public static List<Event> createEvents(int offset, int size) {
        IntFunction<Event> intFunction = x -> {
            return new Event(uniqueIds.get(x), UUID.randomUUID().toString() , false);
        };
        return IntStream.rangeClosed(offset,size).mapToObj(intFunction).collect(Collectors.toList());
    }

}

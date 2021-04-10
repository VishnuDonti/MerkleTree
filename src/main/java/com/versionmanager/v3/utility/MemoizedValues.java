package com.versionmanager.v3.utility;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

@Component
public class MemoizedValues implements InitializingBean {

    private static Map<Short,Short> nextLevelIndexMapping = new HashMap<>();

    public static Short nextLevelIndex(int index) {
        return nextLevelIndexMapping.get(index);
    }

    public static Short levelIndex(short index, int difference) {
        short tempIndex = index;
        for(int i = 0 ; i < difference ; i++) {
            if(nextLevelIndexMapping.get(tempIndex) == null) {
                short nextTempIndex = (short)(tempIndex + Math.pow(2,floorlog2(tempIndex+1)));
                nextLevelIndexMapping.put(tempIndex,nextTempIndex);
                tempIndex = nextTempIndex;
            } else {
                tempIndex = nextLevelIndexMapping.get(tempIndex);
            }
        }
        return tempIndex;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        IntStream.rangeClosed(0,12000).forEach(x -> nextLevelIndexMapping.put((short)x , (short)(x + Math.pow(2,floorlog2(x+1)))));
    }

    private static int floorlog2(int index) {
        return (int) Math.floor(Math.log10(index) / Math.log10(2));
    }

}



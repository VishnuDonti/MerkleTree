package com.versionmanager;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class StringsCollectionRunnable implements Runnable {
    /*
     * Implement the runnable for adding threadName with index
     */
    private final int stringsCount;
    private final String threadName;
    private final StringsCollection stringsCollection;

    public StringsCollectionRunnable(StringsCollection stringsCollection, int stringsCount, String threadName) {
        this.stringsCollection = stringsCollection;
        this.stringsCount = stringsCount;
        this.threadName = threadName;
    }

    @Override
    public void run() {
        for(int i = 0 ; i < stringsCount && i < 10 ;i++) {
            stringsCollection.addString(threadName + i);
        }
       int diff = stringsCount - 10;
        if(diff > 0) {
            while(diff > 0) {
                stringsCollection.addString(null);
                diff--;
            }
        }

    }
}

class StringsCollection {
    java.util.List<String> list = java.util.Collections.synchronizedList(new java.util.ArrayList<>());
    public void addString(String string){
        list.add(string);
    }

    public java.util.List<String> getStringsCollection(){
        return list;
    }

    public void clear(){
        list.clear();
        list = java.util.Collections.synchronizedList(new java.util.ArrayList<>());
    }

}


public class CandidateCode {
    private static final Scanner SCANNER = new Scanner(System.in);
    private static final StringsCollection STRINGS_COLLECTION = new StringsCollection();

    public static void main(String[] args) {
        int threadsCount = Integer.parseInt(SCANNER.nextLine());


        List<String> stringsCollection = getStrings(threadsCount);

        int nonNullStrings = 0;
        for (String string: stringsCollection) {
            if (string != null) {
                nonNullStrings++;
            }
        }
        System.out.println(nonNullStrings);
        STRINGS_COLLECTION.clear();
    }

    private static List<String> getStrings(int threadsCount){
        Thread[] threads = new Thread[threadsCount];

        for (int i = 0; i < threadsCount; i++) {
            int stringsCount = Integer.parseInt(SCANNER.nextLine());
            threads[i] = new Thread(new StringsCollectionRunnable(STRINGS_COLLECTION, stringsCount, String.valueOf(i + 1)));
            threads[i].start();
        }

        for (int i = 0; i < threadsCount; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException ex) {
                System.err.println(ex);
            }
        }

        List<String> stringsCollection = STRINGS_COLLECTION.getStringsCollection();
        System.out.println(stringsCollection.size());

        return stringsCollection;
    }

    public static List<String> getStrings(int threadsCount,List<String> strings){
        Thread[] threads = new Thread[threadsCount];
        int stringsCount = strings.size();
        if (stringsCount < threadsCount){
            int diff = threadsCount - stringsCount;
            for(int i=0; i<diff ; i++ ){
                strings.add("Thread"+i);
            }
        }
        for (int i = 0; i < threadsCount; i++) {
            threads[i] = new Thread(new StringsCollectionRunnable(STRINGS_COLLECTION, stringsCount, strings.get(i)));
            threads[i].start();
        }

        for (int i = 0; i < threadsCount; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException ex) {
                System.err.println(ex);
            }
        }

        List<String> stringsCollection = STRINGS_COLLECTION.getStringsCollection();
        System.out.println(stringsCollection.size());
        return stringsCollection;
    }
}

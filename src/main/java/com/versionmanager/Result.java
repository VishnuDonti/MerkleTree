package com.versionmanager;

import static java.util.stream.Collectors.joining;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.*;
import java.net.*;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.*;
import java.util.stream.Collectors;

import com.google.gson.*;



class Result {

    /*
     * Complete the 'getArticleTitles' function below.
     *
     * The function is expected to return a STRING_LIST.
     * The function accepts STRING author as parameter.
     *
     * URL for cut and paste:
     * https://restmock.techgig.com/news?author=<authorName>&page=<num>
     *
     */
    public static List<String> getArticleTitles(String author)  {
        List<String> titles = new ArrayList<>();
        try {

            String param2 = "1";
            Result1 result1 = getResult1(author, param2);
            List<Article> contents = result1.getContent();
            for(int k = 2 ; k < result1.getTotal_page(); k++ ) {
                contents.addAll(getResult1(author, param2).getContent());
            }

            titles.addAll(contents.stream().sorted().filter( y1 -> !(((y1.getTitle() == null || y1.getTitle().equals("") )&&
                    (y1.getLink() == null ||y1.getLink().equals(""))))).map(y -> {
                String title =y.getTitle();
                String link = y.getLink();
                if(title != null && !title.equals("")) {
                    return title;
                } else
                    return link;
            }).collect(Collectors.toList()));
        } catch (Exception e) {
            System.out.println(e);
        }
        return titles;
    }

    private static Result1 getResult1(String author, String param2) throws IOException {
        String query = String.format("author=%s&page=%s",
                                     author, param2);
        URL url = new URL("https://restmock.techgig.com/news?"+query);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        InputStreamReader reader = new InputStreamReader(urlConnection.getInputStream());
        return new Gson().fromJson(reader, Result1.class);
    }


    class Result1 {
        public int getPage() {
            return page;
        }

        public void setPage(int page) {
            this.page = page;
        }

        public int getTotal_page() {
            return total_page;
        }

        public void setTotal_page(int total_page) {
            this.total_page = total_page;
        }

        public int getPer_page() {
            return per_page;
        }

        public void setPer_page(int per_page) {
            this.per_page = per_page;
        }

        public int getTotal() {
            return total;
        }

        public void setTotal(int total) {
            this.total = total;
        }

        public List<Article> getContent() {
            return content;
        }

        public void setContent(List<Article> content) {
            this.content = content;
        }

        private int page;
        private int total_page;
        private int per_page;
        private int total;
        private List<Article> content;

    }

    class Article {
        private int id;
        private Integer points;
        private Integer num_comments;
        private String author;
        private String title;
        private String link;
        private String created_date;
        private String created_time;
        private String day_of_the_week;

        public String getDay_of_the_week() {
            return day_of_the_week;
        }

        public void setDay_of_the_week(String day_of_the_week) {
            this.day_of_the_week = day_of_the_week;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public Integer getPoints() {
            return points;
        }

        public void setPoints(Integer points) {
            this.points = points;
        }

        public Integer getNum_comments() {
            return num_comments;
        }

        public void setNum_comments(Integer num_comments) {
            this.num_comments = num_comments;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public String getCreated_date() {
            return created_date;
        }

        public void setCreated_date(String created_date) {
            this.created_date = created_date;
        }

        public String getCreated_time() {
            return created_time;
        }

        public void setCreated_time(String created_time) {
            this.created_time = created_time;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getLink() {
            return link;
        }

        public void setLink(String link) {
            this.link = link;
        }
    }

    public static void main(String[] args) throws IOException {

        List<String> result = Result.getArticleTitles("sama");

        System.out.println(result.stream()
                                   .collect(joining("\n"))
                                   + "\n");


    }

}






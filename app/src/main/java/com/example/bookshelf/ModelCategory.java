package com.example.bookshelf;

public class ModelCategory {
    //make sure to use same spelling for model variable as in firebse

     String id, category, uid;
     long timestamp;

     //constructor empty required for firebase

    public ModelCategory() {
    }

    //parametrized constructor


    public ModelCategory(String id) {
        this.id = id;
        this.category=category;
        this.uid=uid;
        this.timestamp=timestamp;
    }

    //Getter?Setter


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
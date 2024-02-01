package de.symeda.sormas.api.messaging;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FCMDto implements Serializable, Cloneable{
	
	@JsonProperty("to")
    private String to;

    @JsonProperty("notification")
    private Notification notification;

//    @JsonProperty("data")
//    private Data data;

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public Notification getNotification() {
        return notification;
    }

    public void setNotification(Notification notification) {
        this.notification = notification;
    }

//    public Data getData() {
//        return data;
//    }
//
//    public void setData(Data data) {
//        this.data = data;
//    }

    public class Notification implements Serializable{

        @JsonProperty("body")
        private String body;

        @JsonProperty("title")
        private String title;

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }

//    public class Data implements Serializable{
//
//        @JsonProperty("title")
//        private String title;
//
//        @JsonProperty("message")
//        private String message;
//
//        public String getTitle() {
//            return title;
//        }
//
//        public void setTitle(String title) {
//            this.title = title;
//        }
//
//        public String getMessage() {
//            return message;
//        }
//
//        public void setMessage(String message) {
//            this.message = message;
//        }
//    }
}

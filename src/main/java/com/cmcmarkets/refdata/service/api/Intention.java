package com.cmcmarkets.refdata.service.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Intention {

    @JsonProperty("USER")
    private String user;
    @JsonProperty("HOSTNAME")
    private String hostname;
    @JsonProperty("ENDPOINT")
    private String endPoint;
    @JsonProperty("REQUESS_BODY")
    private String requestBody;

    public Intention(String user, String hostname, String endPoint, String requestBody) {
        this.user = user;
        this.hostname = hostname;
        this.endPoint = endPoint;
        this.requestBody = requestBody;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    @Override
    public String toString() {
        return "Intention{" +
                "user='" + user + '\'' +
                ", hostname='" + hostname + '\'' +
                ", endPoint='" + endPoint + '\'' +
                ", requestBody='" + requestBody + '\'' +
                '}';
    }
}

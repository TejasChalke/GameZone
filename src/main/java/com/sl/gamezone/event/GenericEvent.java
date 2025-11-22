package com.sl.gamezone.event;

public class GenericEvent {
    public static final String EVENT_JOIN_LOBBY = "EVENT_JOIN_LOBBY";
    public static final String EVENT_LEAVE_LOBBY = "EVENT_LEAVE_LOBBY";

    String userId;
    String userName;
    String eventType;
    boolean guestEvent;

    public GenericEvent(String playerId, String name, String eventType, boolean guestEvent) {
        this.userId = playerId;
        this.userName = name;
        this.eventType = eventType;
        this.guestEvent = guestEvent;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public boolean isGuestEvent() {
        return guestEvent;
    }

    public void setGuestEvent(boolean guestEvent) {
        this.guestEvent = guestEvent;
    }
}

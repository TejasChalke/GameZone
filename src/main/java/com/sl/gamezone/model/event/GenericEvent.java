package com.sl.gamezone.model.event;

public class GenericEvent {
    public static final String EVENT_JOIN_LOBBY = "EVENT_JOIN_LOBBY";
    public static final String EVENT_LEAVE_LOBBY = "EVENT_LEAVE_LOBBY";
    public static final String EVENT_START_GAME = "EVENT_START_GAME";

    protected String id;
    protected String userName;
    protected String lobbyId;
    protected String eventType;
    protected boolean guestEvent;

    public GenericEvent(String id, String name, String lobbyId, String eventType, boolean guestEvent) {
        this.id = id;
        this.userName = name;
        this.lobbyId = lobbyId;
        this.eventType = eventType;
        this.guestEvent = guestEvent;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getLobbyId() {
        return lobbyId;
    }

    public void setLobbyId(String lobbyId) {
        this.lobbyId = lobbyId;
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

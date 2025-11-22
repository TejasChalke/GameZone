package com.sl.gamezone.user;

public class GenericUser {
    protected String id;
    protected String name;
    protected boolean guest;
    protected int userIndex; // used to remove the user when exiting the lobby
    protected boolean spectator;

    public GenericUser(String id, String name, boolean guest, boolean spectator) {
        this.id = id;
        this.name = name;
        this.guest = guest;
        this.spectator = spectator;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isGuest() {
        return guest;
    }

    public void setGuest(boolean guest) {
        this.guest = guest;
    }

    public int getUserIndex() {
        return userIndex;
    }

    public void setUserIndex(int userIndex) {
        this.userIndex = userIndex;
    }

    public boolean isSpectator() {
        return spectator;
    }

    public void setSpectator(boolean spectator) {
        this.spectator = spectator;
    }
}

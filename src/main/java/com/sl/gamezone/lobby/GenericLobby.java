package com.sl.gamezone.lobby;

import com.sl.gamezone.event.GenericEvent;
import com.sl.gamezone.user.GenericUser;

import java.util.List;
import java.util.Map;

// In he lobby manager the mapping would be modeId -> id -> GenericLobby, id should be auto generated
public abstract class GenericLobby {
    protected String id;
    protected int modeId;
    protected String name;
    protected int playerLimit;
    protected long turnTimeLimit;
    protected boolean gameStarted;

    protected Map<String, GenericUser> playerMap;
    protected Map<String, GenericUser> spectatorMap;
    protected List<GenericUser> players;
    protected List<GenericUser> spectators;

    public enum GameMode {
        SIMPLE_POKER("Simple Poker");

        public final String name;

        GameMode(String name) {
            this.name = name;
        }
    }

    public GenericLobby(String id, int modeId, String name, int playerLimit, long turnTimeLimit) {
        this.id = id;
        this.modeId = modeId;
        this.name = name;
        this.playerLimit = playerLimit;
        this.turnTimeLimit = turnTimeLimit * 1000; // should be passed in seconds to convert into milliseconds
        this.gameStarted = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getModeId() {
        return modeId;
    }

    public void setModeId(int modeId) {
        this.modeId = modeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPlayerLimit() {
        return playerLimit;
    }

    public void setPlayerLimit(int playerLimit) {
        this.playerLimit = playerLimit;
    }

    public long getTurnTimeLimit() {
        return turnTimeLimit;
    }

    public void setTurnTimeLimit(long turnTimeLimit) {
        this.turnTimeLimit = turnTimeLimit;
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    public void setGameStarted(boolean gameStarted) {
        this.gameStarted = gameStarted;
    }

    public List<GenericUser> getPlayers() {
        return players;
    }

    public List<GenericUser> getSpectators() {
        return spectators;
    }

    public abstract void handleEvent(GenericEvent event) throws Exception;
}

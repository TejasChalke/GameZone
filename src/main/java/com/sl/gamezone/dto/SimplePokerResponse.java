package com.sl.gamezone.dto;

import com.sl.gamezone.model.lobby.SimplePokerLobby;
import com.sl.gamezone.model.user.SimplePokerUser;

import java.util.ArrayList;
import java.util.List;

public class SimplePokerResponse extends GenericResponse {
    public String lobbyName;
    public int playerLimit;

    public boolean roundStarted;
    public boolean tableStarted;
    public boolean gameStarted;

    public List<SimplePokerUser> players;
    public List<SimplePokerUser> spectators;
    public List<SimplePokerUser> winners;

    public List<Integer> displayCards;
    public int currentPot;
    public int minBet;
    public int[] maxBet;

    public int tablesCompleted;
    public int roundsCompleted;

    public int userToPlayIndex;
    public List<SimplePokerLobby.Rule> rules;

    public SimplePokerResponse(SimplePokerLobby lobby) {
        this.lobbyName = lobby.getName();
        this.playerLimit = lobby.getPlayerLimit();
        this.roundStarted = lobby.isRoundStarted();
        this.tableStarted = lobby.isTableStarted();
        this.gameStarted = lobby.isGameStarted();

        this.players = new ArrayList<>();
        lobby.getPlayers().forEach(p -> players.add((SimplePokerUser) p));
        this.spectators = new ArrayList<>();
        lobby.getSpectators().forEach(p -> players.add((SimplePokerUser) p));
        this.winners = lobby.winners;

        this.displayCards = lobby.displayCards;
        this.currentPot = lobby.currentPot;
        this.minBet = lobby.minBet;
        this.maxBet = lobby.maxBet;
        this.tablesCompleted = lobby.tablesCompleted;
        this.roundsCompleted = lobby.roundsCompleted;
        this.userToPlayIndex = lobby.userToPlayIndex;
        this.rules = lobby.rules;
    }
}

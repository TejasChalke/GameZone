package com.sl.gamezone.model.user;

import java.util.ArrayList;
import java.util.List;

public class SimplePokerUser extends GenericUser {
    private final List<Integer> cards;
    private int coins;
    private int currentBet;
    private int coinsWon; // would be used for leaderboard tracking
    private int coinsLost; // would be used for leaderboard tracking
    private boolean folded;

    public SimplePokerUser(String id, String name, boolean guest, boolean spectator, int coins) {
        super(id, name, guest, spectator);
        this.coins = coins;
        this.folded = false;
        this.cards = new ArrayList<>();
    }

    public int getCoins() {
        return coins;
    }

    public void placeBet(int amount) throws Exception {
        if (amount > coins) {
            throw new Exception("SimplePokerUser.placeBet() :: Bet is greater than the amount of coins held. Coins : " + coins + ", Bet : " + amount);
        }
        currentBet += amount;
        coins -= amount;
    }

    public int getCoinsWon() {
        return coinsWon;
    }

    public int getCoinsLost() {
        return coinsLost;
    }

    public void updateCoinsLost() {
        coinsLost += currentBet;
        currentBet = 0;
    }

    public void updateCoinsWon(int amount) {
        coinsWon += amount;
        coins += amount;
        currentBet = 0;
    }

    public boolean isFolded() {
        return folded;
    }

    public void setFolded(boolean folded) {
        this.folded = folded;
    }

    public void addCard(int card) {
        cards.add(card);
    }

    public List<Integer> getCards() {
        return cards;
    }

    public void reset() {
        coinsWon = coinsLost = coins = currentBet = 0;
        folded = false;
        cards.clear();
    }
}

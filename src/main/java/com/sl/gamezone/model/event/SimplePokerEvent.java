package com.sl.gamezone.model.event;

public class SimplePokerEvent extends GenericEvent {
    public static String EVENT_SIMPLE_POKER_CALL_RAISE = "EVENT_SIMPLE_POKER_CALL_RAISE";
    public static String EVENT_SIMPLE_POKER_FOLD = "EVENT_SIMPLE_POKER_FOLD";

    private int betAmount;

    public SimplePokerEvent(String userId, String userName, String lobbyId, String eventType, boolean guestEvent, int betAmount) {
        super(userId, userName, lobbyId, eventType, guestEvent);
        this.betAmount = betAmount;
    }

    public int getBetAmount() {
        return betAmount;
    }

    public void setBetAmount(int betAmount) {
        this.betAmount = betAmount;
    }
}
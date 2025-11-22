package com.sl.gamezone.event;

public class SimplePokerEvent extends GenericEvent {
    public static String EVENT_SIMPLE_POKER_CALL_RAISE = "EVENT_SIMPLE_POKER_CALL_RAISE";
    public static String EVENT_SIMPLE_POKER_FOLD = "EVENT_SIMPLE_POKER_FOLD";

    private int betAmount;

    public SimplePokerEvent(String playerId, String name, String eventType, boolean guestEvent, int betAmount) {
        super(playerId, name, eventType, guestEvent);
        this.betAmount = betAmount;
    }

    public int getBetAmount() {
        return betAmount;
    }

    public void setBetAmount(int betAmount) {
        this.betAmount = betAmount;
    }
}
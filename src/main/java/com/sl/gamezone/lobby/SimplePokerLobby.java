package com.sl.gamezone.lobby;

import com.sl.gamezone.event.GenericEvent;
import com.sl.gamezone.event.SimplePokerEvent;
import com.sl.gamezone.user.GenericUser;
import com.sl.gamezone.user.SimplePokerUser;
import com.sl.gamezone.util.Logger;

import java.util.*;

public class SimplePokerLobby extends GenericLobby {
    /*
        TODO:
        Once all the rounds are over for a table, move the players from spectator to active (set spectator to false) after a delay of 15 s
        Would need to implement multi threading to keep checking time for both player turn and table start
     */

    private final int startingCoins;
    private final int tableLimit;
    private final int roundsPerTable;
    private final List<Rule> rules;
    public List<Integer> displayCards;
    private int currentPot;
    private int minBet;
    private int[] maxBet;
    private boolean gameEnded;
    private boolean tableStarted;
    private int tablesCompleted;
    private int roundsCompleted;
    private int roundPhase;
    private int userToPlayIndex;
    private int currentFoldCount;
    private long timerStart;
    private long timerEnd;
    private Set<Integer> usedCards;

    public SimplePokerLobby(String id, int modeId, String name, int playerLimit, long turnTimeLimit, int startingCoins, int tableLimit, int roundsPerTable, final List<String> rules) {
        super(id, modeId, name, playerLimit, turnTimeLimit);
        this.startingCoins = startingCoins;
        this.tableStarted = false;
        this.tableLimit = tableLimit;
        this.roundsPerTable = roundsPerTable;
        this.rules = new ArrayList<>();
        for (String ruleName : rules) {
            this.rules.add(Rule.ruleMap.get(ruleName));
            this.rules.sort((a, b) -> Integer.compare(b.getPriority(), a.getPriority()));
        }
    }

    @Override
    public void handleEvent(GenericEvent genericEvent) throws Exception {
        if (!(genericEvent instanceof SimplePokerEvent event)) {
            throw new Exception("SimplePokerLobby.handleEvent() :: Incorrect event type passed : " + genericEvent.getEventType());
        }

        if (GenericEvent.EVENT_JOIN_LOBBY.equals(event.getEventType())) handleJoin(event);
        else if (GenericEvent.EVENT_LEAVE_LOBBY.equals(event.getEventType())) handleLeave(event);
        else if (SimplePokerEvent.EVENT_SIMPLE_POKER_CALL_RAISE.equals(event.getEventType())) handleCallOrRaise(event);
        else if (SimplePokerEvent.EVENT_SIMPLE_POKER_FOLD.equals(event.getEventType())) handleFold(event);
    }

    private void handleJoin(GenericEvent event) throws Exception {
        SimplePokerUser user = new SimplePokerUser(event.getUserId(), event.getUserName(), event.isGuestEvent(),
                tableStarted /* if the table is started the new player would be a spectator until the next table starts */,
                startingCoins);
        if (playerMap.containsKey(event.getUserId())) {
            throw new Exception("SimplePokerLobby.handleJoin() :: A user attempted to join the lobby with an existing userId : " + user.getId());
        }

        if (tableStarted) {
            spectatorMap.put(user.getId(), user);
            user.setUserIndex(-1);
            spectators.add(user);
        } else {
            playerMap.put(user.getId(), user);
            user.setUserIndex(players.size());
            players.add(user);
        }
    }

    private void handleLeave(GenericEvent event) throws Exception {
        if (!playerMap.containsKey(event.getUserId())) {
            throw new Exception("SimplePokerLobby.handleLeave() :: A user attempted to leave the lobby without an existing userId : " + event.getUserId());
        }

        SimplePokerUser user = (SimplePokerUser) playerMap.get(event.getUserId());
        if (user.getUserIndex() != players.size() - 1) {
            // move the user at the end of the list, to the position of the user to remove (override the index)
            SimplePokerUser lastIndexUser = (SimplePokerUser) players.get(players.size() - 1);
            players.set(user.getUserIndex(), lastIndexUser);
            lastIndexUser.setUserIndex(user.getUserIndex());
        }

        // remove the last user, this would remove the double entry and, the user to remove would no longer exists in the list
        players.remove(players.size() - 1);
        // remove the user from the map
        playerMap.remove(user.getId());
    }

    private void handleCallOrRaise(SimplePokerEvent event) throws Exception {
        if (!playerMap.containsKey(event.getUserId())) {
            throw new Exception("SimplePokerLobby.handleCallOrRaise() :: User with userId : " + event.getUserId() + ", not found!");
        }

        SimplePokerUser user = (SimplePokerUser) playerMap.get(event.getUserId());
        if (user.getUserIndex() != userToPlayIndex) {
            Logger.warn("SimplePokerLobby.handleCallOrRaise() :: User with userId : ", event.getUserId(),
                    ", is playing when it is not the user's turn. Ignoring action...");
            return;
        }
        if (event.getBetAmount() > maxBet[user.getUserIndex()]) {
            Logger.warn("SimplePokerLobby.handleCallOrRaise() :: User with userId : ", event.getUserId(),
                    ", is betting more than max bet allowed : ", event.getBetAmount() + " > ", maxBet[user.getUserIndex()] + ". Defaulting to max bet...");
            event.setBetAmount(maxBet[user.getUserIndex()]);
        }

        user.placeBet(event.getBetAmount());
        currentPot += event.getBetAmount();
        minBet = event.getBetAmount();
        updateTurn(userToPlayIndex + 1);
    }

    private void handleFold(SimplePokerEvent event) throws Exception {
        if (!playerMap.containsKey(event.getUserId())) {
            throw new Exception("SimplePokerLobby.handleFold() :: User with userId : " + event.getUserId() + ", not found!");
        }

        SimplePokerUser user = (SimplePokerUser) playerMap.get(event.getUserId());
        if (user.getUserIndex() != userToPlayIndex) {
            Logger.warn("SimplePokerLobby.handleFold() :: User with userId : ", event.getUserId(),
                    ", is playing when it is not the user's turn. Ignoring action...");
            return;
        }

        user.setFolded(true);
        currentFoldCount++;
        updateTurn(userToPlayIndex + 1);
    }

    public void startGame() {
        gameEnded = false;
        tableStarted = true;
        currentPot = 0;
        roundPhase = 0;
        currentFoldCount = 0;
        minBet = 1;

        for (GenericUser genericUser : players) {
            SimplePokerUser user = (SimplePokerUser) genericUser;
            for (int count = 0; count < 2; count++) {
                int card = getRandomCard();
                usedCards.add(card);
                user.addCard(card);
            }
        }

        setupPhase(0);
        updateTurn(0);
    }

    public void startNextTable() {

    }

    private void updateTurn(int turnIndex) {
        if (currentFoldCount >= players.size() - 1) {
            return; // A player will win by default or all have folded because of having too few coins
        }

        while (turnIndex < players.size() && ((SimplePokerUser) players.get(turnIndex)).isFolded()) {
            turnIndex++;
        }

        if (turnIndex == players.size()) {
            if (roundPhase < 3) {
                setupPhase(roundPhase++);
                updateTurn(0);
            }
            return; // either updateTurn was called OR, all phases are over, determine round winner
        }

        userToPlayIndex = turnIndex;
        timerStart = System.currentTimeMillis();
        timerEnd = timerStart + turnTimeLimit;
    }

    private List<SimplePokerUser> getWinners() {
        Set<String> winningIds = new HashSet<>();
        if (currentFoldCount == players.size() - 1) {
            for (GenericUser genericUser : players) {
                SimplePokerUser user = (SimplePokerUser) genericUser;
                if (!user.isFolded()) {
                    // All except 1 user have folded
                    winningIds.add(user.getId());
                    break;
                }
            }
        } else if (roundPhase < 3) {
            return null; // no winner by default and all phases are not over yet
        } else {
            WinCondition prevWinCondition = null;
            for (GenericUser genericUser : players) {
                SimplePokerUser user = (SimplePokerUser) genericUser;
                WinCondition condition = getWinCondition(user);
                condition.cardRanks.sort(Integer::compare);
                prevWinCondition = updateWinCondition(winningIds, prevWinCondition, condition);
            }

            if (winningIds.size() > 1) {
                currentPot /= winningIds.size();
            }
        }

        List<SimplePokerUser> winners = new ArrayList<>();
        for (GenericUser genericUser : players) {
            SimplePokerUser user = (SimplePokerUser) genericUser;
            if (winningIds.contains(user.getId())) {
                user.updateCoinsWon(currentPot);
                winners.add(user);
            } else {
                user.updateCoinsLost();
            }
        }

        if (++roundsCompleted == roundsPerTable) {
            roundsCompleted = 0;
            tableStarted = false;
            if (++tablesCompleted == tableLimit) {
                gameEnded = true;
            }
        }
        timerStart = System.currentTimeMillis();
        timerEnd = timerStart + 15000; // 15s for displaying the winner
        return winners; // it can happen that all would be forced to fold because next min bet is too large, handle this
    }

    private WinCondition getWinCondition(SimplePokerUser user) {
        WinCondition condition = new WinCondition(user.getId());
        condition.priority = getHighestPriorityRule(user, condition);
        return condition;
    }

    private int getHighestPriorityRule(SimplePokerUser user, WinCondition condition) {
        List<Card> cards = new ArrayList<>();
        displayCards.forEach(cardNumber -> cards.add(new Card(cardNumber)));
        user.getCards().forEach(cardNumber -> cards.add(new Card(cardNumber)));
        cards.sort((a, b) -> a.suit != b.suit ? Integer.compare(a.suit, b.suit) : Integer.compare(a.rank, b.rank));
        cards.forEach(card -> condition.cardRanks.add(card.rank));

        // we would be iterating the rules from highest to lowest priority
        // so return the first rule priority which is satisfied
        for (Rule rule : rules) {
            int[] count = new int[13];
            int prevRank = -1;
            int reqSuit = -1;
            boolean valid = true;
            int trips = 0;
            int pairs = 0;

            switch (rule) {
                case ROYAL_FLUSH: // 10, J, Q, K, A. suit should be the same
                    prevRank = cards.get(0).rank;
                    if (prevRank == 8) { // rank 8 indicates a Card with number 10
                        reqSuit = cards.get(0).suit;
                        for (Card card : cards) {
                            if (card.suit != reqSuit) {
                                break;
                            }
                        }
                        return Rule.ROYAL_FLUSH.priority;
                    }
                    break;
                case STRAIGHT_FLUSH: // 2, 3, 4, 5, 6 OR any sequence other than royal flush
                    for (Card card : cards) {
                        if (prevRank == -1) {
                            prevRank = card.rank;
                            reqSuit = card.suit;
                        } else if (prevRank + 1 != card.rank || reqSuit != card.suit) {
                            valid = false;
                            break;
                        } else {
                            prevRank++;
                        }
                    }
                    if (valid) return Rule.STRAIGHT_FLUSH.priority;
                    break;
                case QUADS: // 3, 3, 3, 3, *
                    for (Card card : cards) {
                        if (++count[card.rank] == 4) {
                            return Rule.QUADS.priority;
                        }
                    }
                    break;
                case FULL_HOUSE: // 4, 4, 4, 7, 7 or any other "trips and pair cards"
                    for (Card card : cards) {
                        if (++count[card.rank] == 2) {
                            pairs++;
                        } else if (count[card.rank] == 3) {
                            trips++;
                        }
                    }
                    if (trips == 1 && pairs == 2) return Rule.FULL_HOUSE.priority;
                    break;
                case FLUSH: // S, S, S, S, S (all have the same suit)
                    reqSuit = cards.get(0).suit;
                    for (Card card : cards) {
                        if (card.suit != reqSuit) {
                            valid = false;
                            break;
                        }
                    }
                    if (valid) return Rule.FLUSH.priority;
                    break;
                case STRAIGHT: // 3, 4, 5, 6, 7 (suit doesn't matter)
                    for (Card card : cards) {
                        if (prevRank == -1) {
                            prevRank = card.rank;
                        } else if (prevRank + 1 != card.rank) {
                            valid = false;
                            break;
                        } else {
                            prevRank++;
                        }
                    }
                    if (valid) return Rule.STRAIGHT.priority;
                    break;
                case TRIPS:
                    for (Card card : cards) {
                        if (++count[card.rank] == 3) {
                            return Rule.TRIPS.priority;
                        }
                    }
                    break;
                case TWO_PAIR:
                    for (Card card : cards) {
                        if (++count[card.rank] == 2) {
                            pairs++;
                        }
                    }
                    if (pairs == 2) return Rule.TWO_PAIR.priority;
                    break;
                case PAIR:
                    for (Card card : cards) {
                        if (++count[card.rank] == 2) {
                            return Rule.PAIR.priority;
                        }
                    }
                    break;
                default:
                    return Rule.HIGH_CARD.priority;
            }
        }
        return 0;
    }

    private WinCondition updateWinCondition(Set<String> winningIds, WinCondition prevWinCondition, WinCondition currentWinCondition) {
        if (prevWinCondition == null || prevWinCondition.priority < currentWinCondition.priority) {
            winningIds.clear();
            winningIds.add(currentWinCondition.userId);
            return currentWinCondition;
        } else if (prevWinCondition.priority == currentWinCondition.priority) {
            for (int card = 4; card >= 0; card--) {
                if (currentWinCondition.cardRanks.get(card) > prevWinCondition.cardRanks.get(card))
                    return currentWinCondition;
                else if (prevWinCondition.cardRanks.get(card) > currentWinCondition.cardRanks.get(card))
                    return prevWinCondition;
            }
            winningIds.add(currentWinCondition.userId); // Same priority and cards. Suit doesn't matter
        }
        return prevWinCondition;
    }

    private void setupPhase(int phase) {
        int nextMin = startingCoins * players.size();
        maxBet = new int[players.size()];

        for (int i = players.size() - 1; i >= 0; i--) {
            int userCoins = ((SimplePokerUser) players.get(i)).getCoins();
            if (userCoins < minBet) {
                ((SimplePokerUser) players.get(i)).setFolded(true);
                currentFoldCount++;
            } else {
                maxBet[i] = nextMin;
                nextMin = Math.min(nextMin, userCoins);
            }
        }

        if (phase == 0) return; // no cards would be displayed

        int card = getRandomCard();
        usedCards.add(card);
        displayCards.add(card);
    }

    private int getRandomCard() {
        int card = -1;
        do {
            card = ((int) (Math.random() * 52)) % 52;
        } while (usedCards.contains(card));
        return card;
    }

    public enum Rule {
        ROYAL_FLUSH("Royal Flush", "Ace, King, Queen, Jack, and Ten, all of the same suit", 10),
        STRAIGHT_FLUSH("Straight Flush", "Five consecutive cards, all of the same suit", 9),
        QUADS("Quads", "Four cards of same kind", 8),
        FULL_HOUSE("Full House", "Three cards of one rank and two cards of another rank", 7),
        FLUSH("Flush", "Five cards of the same suit, but not in consecutive order", 6),
        STRAIGHT("Straight", "Five cards in sequential order, but not all of the same suit", 5),
        TRIPS("Trips", "Three cards of the same rank, plus one unrelated card", 4),
        TWO_PAIR("Two Pair", "Two cards of one rank and two cards of another rank, plus one unrelated card", 3),
        PAIR("Pair", "Two cards of the same rank, plus three unrelated cards", 2),
        HIGH_CARD("High Card", "Single highest-ranking card", 1);

        public static final Map<String, Rule> ruleMap;

        static {
            ruleMap = new HashMap<>();
            for (Rule rule : Rule.values()) {
                ruleMap.put(rule.getName(), rule);
            }
        }

        private final String name;
        private final String description;
        private final int priority;

        Rule(String name, String description, int priority) {
            this.name = name;
            this.description = description;
            this.priority = priority;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public int getPriority() {
            return priority;
        }
    }

    public static class Card {
        int suit; // hearts, clubs, diamonds, spades
        int rank; // 2, 3, 4, ..., Q, K, A

        Card(int suit, int rank) {
            this.suit = suit;
            this.rank = rank;
        }

        Card(int cardNumber) {
            this.suit = cardNumber / 13;
            this.rank = cardNumber % 13;
        }
    }

    public static class WinCondition {
        int priority;
        List<Integer> cardRanks;
        String userId;

        WinCondition(String userId) {
            priority = 0;
            cardRanks = new ArrayList<>();
            this.userId = userId;
        }
    }
}

package com.sl.gamezone.service;

import com.sl.gamezone.dto.GenericRequest;
import com.sl.gamezone.dto.GenericResponse;
import com.sl.gamezone.dto.SimplePokerRequest;
import com.sl.gamezone.dto.SimplePokerResponse;
import com.sl.gamezone.model.event.SimplePokerEvent;
import com.sl.gamezone.model.lobby.GenericLobby;
import com.sl.gamezone.model.lobby.SimplePokerLobby;
import com.sl.gamezone.util.Logger;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.*;

@Service
public class LobbyService {
    private final SimpMessagingTemplate messagingTemplate;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);
    private final Map<String, GenericLobby> lobbies = new ConcurrentHashMap<>();

    public LobbyService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public GenericLobby getLobby(String lobbyId) {
        return lobbies.get(lobbyId);
    }

    public void addLobby(GenericLobby lobby) {
        lobbies.put(lobby.getId(), lobby);
    }

    public void closeLobby(String lobbyId) {
        GenericLobby lobby = lobbies.get(lobbyId);

        // Cancel old timer
        if (lobby.getTimerFuture() != null && !lobby.getTimerFuture().isDone()) {
            lobby.getTimerFuture().cancel(true);
        }
        lobbies.remove(lobbyId);

        messagingTemplate.convertAndSend("/topic/lobby/" + lobbyId, new GenericResponse(true, null));
    }

    public void startOrResetTimer(GenericRequest request) {
        GenericLobby lobby = lobbies.get(request.lobbyId);

        // Cancel old timer
        if (lobby.getTimerFuture() != null && !lobby.getTimerFuture().isDone()) {
            lobby.getTimerFuture().cancel(true);
        }

        // Start new timer
        ScheduledFuture<?> future = scheduler.schedule(() -> {
            sendLobbyState(request);
        }, lobby.getTurnTimeLimit(), TimeUnit.SECONDS);

        lobby.setTimerFuture(future);
    }

    // This would only execute if the timer runs out which can happen if,
    // 1. User does not provide an input during the users turn
    // 2. The table or game has ended
    private void sendLobbyState(GenericRequest genericRequest) {
        try {
            if (genericRequest instanceof SimplePokerRequest request) {
                SimplePokerLobby lobby = (SimplePokerLobby) lobbies.get(request.lobbyId);

                if (lobby.isGameStarted()) {
                    if (lobby.isTableStarted()) {
                        if (lobby.isRoundStarted()) {
                            // User did not provide an input, fold by default
                            SimplePokerEvent event = new SimplePokerEvent(request.userId, null, request.lobbyId, SimplePokerEvent.EVENT_SIMPLE_POKER_FOLD, request.guest, request.betAmount);
                            lobby.handleEvent(event);
                        } else {
                            lobby.startNextRound();
                        }
                    } else {
                        lobby.startNextTable();
                    }
                    messagingTemplate.convertAndSend("/topic/lobby/" + request.lobbyId, new SimplePokerResponse(lobby));
                } else {
                    closeLobby(genericRequest.lobbyId);
                }
            }
        } catch (Exception e) {
            Logger.error("LobbyService.sendLobbyState() :: Error while sending a response on timeout : ", e.getMessage());
        }
    }
}

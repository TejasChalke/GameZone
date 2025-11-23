package com.sl.gamezone.controller;

import com.sl.gamezone.dto.GenericRequest;
import com.sl.gamezone.dto.GenericResponse;
import com.sl.gamezone.dto.SimplePokerRequest;
import com.sl.gamezone.model.event.GenericEvent;
import com.sl.gamezone.model.event.SimplePokerEvent;
import com.sl.gamezone.model.lobby.GenericLobby;
import com.sl.gamezone.model.lobby.SimplePokerLobby;
import com.sl.gamezone.service.LobbyService;
import com.sl.gamezone.util.Logger;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class LobbyWebSocketController {
    private final LobbyService lobbySevice;
    private final SimpMessagingTemplate messagingTemplate;

    public LobbyWebSocketController(LobbyService lobbySevice, SimpMessagingTemplate messagingTemplate) {
        this.lobbySevice = lobbySevice;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/joinLobby")
    @SendToUser("/queue/errors")
    public void joinLobby(GenericRequest request, Principal principal) {
        try {
            GenericLobby lobby = lobbySevice.getLobby(request.lobbyId);
            GenericEvent event = new GenericEvent(request.userId, principal.getName(), request.lobbyId, request.eventType, request.guest);
            lobby.handleEvent(event); // automatically handles joins, as per the lobby type
            messagingTemplate.convertAndSend("/topic/lobby/" + event.getLobbyId(), new GenericResponse(false, lobby.getPlayers()));
        } catch (Exception e) {
            Logger.error("LobbyWebSocketController.joinLobby() :: Error when connecting user to lobby : ", e.getMessage());
            throw new MessagingException("Unable to connect to lobby");
        }
    }

    @MessageMapping("/simplePoker")
    @SendToUser("/queue/errors")
    public void makeUserAction(SimplePokerRequest request, Principal principal) {
        try {
            SimplePokerLobby lobby = (SimplePokerLobby) lobbySevice.getLobby(request.lobbyId);
            SimplePokerEvent event = new SimplePokerEvent(request.userId, principal.getName(), request.lobbyId, request.eventType, request.guest, request.betAmount);
            lobby.handleEvent(event);
            // TODO: check for win after user action
            // TODO: start a timer to restart table after the current table is over
            lobbySevice.startOrResetTimer(request);
        } catch (Exception e) {
            Logger.error("LobbyWebSocketController.makeUserAction() [SimplePoker] :: Error when making user action : ", e.getMessage());
            throw new MessagingException("Unable to perform user action");
        }
    }
}

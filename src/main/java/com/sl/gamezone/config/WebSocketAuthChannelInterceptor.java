package com.sl.gamezone.config;

import com.sl.gamezone.service.JwtService;
import com.sl.gamezone.util.Logger;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;

    public WebSocketAuthChannelInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        try {
            if (StompCommand.CONNECT.equals(accessor.getCommand()) || StompCommand.SUBSCRIBE.equals(accessor.getCommand()) || StompCommand.SEND.equals(accessor.getCommand())) {
                String token = accessor.getFirstNativeHeader("Authorization");
                String username = jwtService.validateAndExtract(token);
                accessor.setUser(new UsernamePasswordAuthenticationToken(username, null));
            }
        } catch (Exception e) {
            Logger.error("WebSocketAuthChannelInterceptor.preSend() :: Error in authentication : ", e.getMessage());
        }

        return message;
    }
}
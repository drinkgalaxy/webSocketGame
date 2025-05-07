package com.example.webSocketGame.controller;

import com.example.webSocketGame.dto.ChatMessage;
import com.example.webSocketGame.session.SessionRegistry;
import jakarta.websocket.Session;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {

  private final SessionRegistry sessionRegistry;
  private final SimpMessageSendingOperations template;


  @MessageMapping("/chat")
  @SendTo("/topic/join")
  public void handleChar(@Payload ChatMessage msg, StompHeaderAccessor accessor) {
    String sessionId = accessor.getSessionId();
    String nickname = sessionRegistry.getNickName(sessionId);

    msg.setSender(nickname);
    template.convertAndSend("/topic/chat/" + msg.getRoomId(), msg);
  }
}

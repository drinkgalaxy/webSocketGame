package com.example.webSocketGame.game.controller;

import com.example.webSocketGame.game.dto.GameStartMessage;
import com.example.webSocketGame.session.SessionRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class GameController {

  private final SessionRegistry sessionRegistry;
  private final SimpMessageSendingOperations template;


  @MessageMapping("/start")
  public void handleStartGame(@Payload GameStartMessage msg, StompHeaderAccessor accessor) {
    String sessionId = accessor.getSessionId();
    String roomId = msg.getRoomId();

    // 1. 세션이 해당 방의 리더인지 확인
    if (!sessionRegistry.isLeader(sessionId, roomId)) {
      System.out.println("게임 시작 권한 없음");
      return;
    }

    // 2. 게임 시작 처리
    sessionRegistry.startGame(roomId, msg.getRounds());

    // 3. 모든 유저에게 브로드캐스트
    template.convertAndSend("/sub/start/" + roomId, "게임이 시작됩니다!");
  }

}

package com.example.webSocketGame.waitingRoom.controller;

import com.example.webSocketGame.waitingRoom.dto.JoinMessage;
import com.example.webSocketGame.session.SessionRegistry;
import com.example.webSocketGame.waitingRoom.entity.WaitingRoom;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class WaitingRoomController {

  private final SessionRegistry sessionRegistry;
  private final SimpMessageSendingOperations template;

  @MessageMapping("/enter")
  public void handleJoin(@Payload JoinMessage msg, StompHeaderAccessor accessor) {
    String sessionId = accessor.getSessionId();
    sessionRegistry.register(sessionId, msg.getNickname(), msg.getRoomId());

    // 1. 입장 메시지 생성
    String roomId = msg.getRoomId();
    String notice = msg.getNickname() + "님이 입장하셨습니다.";
    template.convertAndSend("/sub/notice/" + roomId, notice);

    // 2. 현재 인원 수 전송
    int count = sessionRegistry.countByRoom(roomId);
    template.convertAndSend("/sub/count/" + roomId, count);

    // 3. 참가자 목록, 리더 갱신
    sessionRegistry.broadcastMembers(msg.getRoomId());
  }



}

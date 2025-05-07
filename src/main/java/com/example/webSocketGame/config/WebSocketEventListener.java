package com.example.webSocketGame.config;

import com.example.webSocketGame.session.SessionRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

  private final SessionRegistry sessionRegistry;
  private final SimpMessageSendingOperations template;

  @EventListener
  public void handleDisconnect(SessionDisconnectEvent event) {
    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
    String sessionId = accessor.getSessionId();

    String nickname = sessionRegistry.getNickName(sessionId);
    String roomId = sessionRegistry.getRoomId(sessionId);

    // 세션 제거
    sessionRegistry.unregister(sessionId);

    // 퇴장 메시지
    if (nickname != null && roomId != null) {
      String leaveMsg = nickname + "님이 퇴장하셨습니다.";
      template.convertAndSend("/topic/notice/" + roomId, leaveMsg);

      // 인원 수 갱신
      int count = sessionRegistry.countByRoom(roomId);
      template.convertAndSend("/topic/count/" + roomId, count);
    }
  }
}

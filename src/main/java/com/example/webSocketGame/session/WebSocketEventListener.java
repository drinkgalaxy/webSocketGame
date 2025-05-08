package com.example.webSocketGame.session;

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

    // 1. 세션 제거
    sessionRegistry.unregister(sessionId);

    // 2. 현재 인원 수 전송
    int count = sessionRegistry.countByRoom(roomId);
    template.convertAndSend("/sub/count/" + roomId, count);

    // 3. 퇴장 메시지
    if (nickname != null && roomId != null) {
      String leaveMsg = nickname + "님이 퇴장하셨습니다.";
      template.convertAndSend("/sub/notice/" + roomId, leaveMsg);
      // 4. 참가자 목록, 리더 갱신
      sessionRegistry.broadcastMembers(roomId);
    }
  }
}

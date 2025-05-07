package com.example.webSocketGame.session;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class SessionRegistry {

  private final Map<String, String> sessionIdToNickName = new ConcurrentHashMap<>();
  private final Map<String, String> sessionIdToRoom = new ConcurrentHashMap<>();

  // 등록
  public void register(String sessionId, String nickname, String roomId) {
    sessionIdToNickName.put(sessionId, nickname);
    sessionIdToRoom.put(sessionId, roomId);
  }

  // 닉네임 조회
  public String getNickName(String sessionId) {
    return sessionIdToNickName.get(sessionId);
  }

  // 방 Id 조회
  public String getRoomId(String sessionId) {
    return sessionIdToRoom.get(sessionId);
  }

  // 현재 방 인원 수 카운트
  public int countByRoom(String roomId) {
    int count = 0;
    for (String id : sessionIdToRoom.values()) {
      if (id.equals(roomId)) {
        count++;
      }
    }
    return count;
  }

  // 퇴장 처리
  public void unregister(String sessionId) {
    sessionIdToNickName.remove(sessionId);
    sessionIdToRoom.remove(sessionId);
  }
}

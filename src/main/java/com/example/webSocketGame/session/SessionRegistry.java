package com.example.webSocketGame.session;

import com.example.webSocketGame.player.entity.Player;
import com.example.webSocketGame.session.dto.PlayerInfoDto;
import com.example.webSocketGame.waitingRoom.entity.WaitingRoom;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SessionRegistry {
  private final Map<String, WaitingRoom> rooms = new ConcurrentHashMap<>(); // roomId → WaitingRoom
  private final Map<String, String> sessionIdToRoomId = new ConcurrentHashMap<>(); // sessionId → roomId
  private final SimpMessageSendingOperations template;

  // 대기실에 플레이어 등록
  public void register(String sessionId, String nickname, String roomId) {
    WaitingRoom room = rooms.computeIfAbsent(roomId, WaitingRoom::new); // room 이 없으면 자동 생성

    room.addSession(sessionId, nickname); // 대기실에 플레이어 추가

    sessionIdToRoomId.put(sessionId, roomId); // sessionId -> roomId 매핑도 저장
  }

  // 해당 대기실에 있는 플레이어 닉네임 조회
  public String getNickName(String sessionId) {
    String roomId = sessionIdToRoomId.get(sessionId);
    if (roomId == null) return null;

    WaitingRoom room = rooms.get(roomId);
    if (room == null) return null;

    for (Player player : room.getPlayerSet()) {
      if (player.getSessionId().equals(sessionId)) {
        return player.getNickname();
      }
    }
    return null;
  }


  // 방 Id 조회
  public String getRoomId(String sessionId) {
    return sessionIdToRoomId.get(sessionId);
  }

  // 대기실에 있는 플레이어 수 리턴
  public int countByRoom(String roomId) {
    WaitingRoom room = rooms.get(roomId);
    return room == null ? 0 : room.getPlayerCount();

  }

  // 대기실에서 플레이어 퇴장 처리
  public void unregister(String sessionId) {
    String roomId = sessionIdToRoomId.remove(sessionId); // sessionId → roomId 매핑 제거
    WaitingRoom room = rooms.get(roomId); // roomId로 대기실 찾음
    room.removeSession(sessionId); // 대기실 내부에서 해당 유저 제거
    if (room.getPlayerCount() == 0) {
      rooms.remove(roomId); // 아무도 안 남으면 방도 제거
    }
  }

  // 플레이어 목록과 방장 브로드캐스트
  public void broadcastMembers(String roomId) {
    WaitingRoom room = rooms.get(roomId);
    if (room == null) return;

    List<PlayerInfoDto> players = room.getPlayerSet().stream()
        .map(p -> new PlayerInfoDto(p.getNickname(), p.isLeader()))
        .toList();

    template.convertAndSend("/sub/room/members/" + roomId, players);
  }
}

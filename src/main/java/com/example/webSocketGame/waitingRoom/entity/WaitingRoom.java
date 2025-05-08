package com.example.webSocketGame.waitingRoom.entity;

import com.example.webSocketGame.player.entity.Player;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;

@Getter
public class WaitingRoom {

  private String roomId;
  private Set<Player> playerSet = ConcurrentHashMap.newKeySet(); // 참가자 Id 세션 목록
  private int selectRounds;
  private boolean gameStarted;

  public WaitingRoom(String roomId) {
    this.roomId = roomId;
    this.selectRounds = 0;
    this.gameStarted = false;
  }

  // 참가자 입장
  public void addSession(String sessionId, String nickname) {
    Player newPlayer = new Player(sessionId, nickname);

    // 첫 입장자라면 leader 로 지정
    if (playerSet.isEmpty()) {
      newPlayer.setLeader();
    }

    playerSet.add(newPlayer);
  }

  // 참가자 퇴장
  public void removeSession(String sessionId) {
    Player toRemove = null;
    for (Player player : playerSet) {
      if (player.getSessionId().equals(sessionId)) {
        toRemove = player;
        break;
      }
    }

    if (toRemove != null) {
      boolean wasLeader = toRemove.isLeader(); // 나간 사람이 리더였는지 체크
      playerSet.remove(toRemove);

      // 리더였고 다른 사람이 남아 있다면 → 새로운 리더 지정
      if (wasLeader && !playerSet.isEmpty()) {
        Player newLeader = playerSet.iterator().next(); // 가장 먼저 남은 사람
        newLeader.setLeader();
      }
    }
  }

  // 참가자 인원 계산
  public int getPlayerCount() {
    return playerSet.size();
  }

  // 게임 라운드 설정
  public void setSelectRounds(int selectRounds) {
    this.selectRounds = selectRounds;
  }

  // 게임 시작
  public void startGame() {
    this.gameStarted = true;
  }

}

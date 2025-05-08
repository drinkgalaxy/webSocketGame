package com.example.webSocketGame.player.entity;

import lombok.Getter;

@Getter
public class Player {

  private final String sessionId;
  private final String nickname;
  private int score = 0;
  private boolean leader;

  public Player(String sessionId, String nickname) {
    this.sessionId = sessionId;
    this.nickname = nickname;
    this.score = 0;
    this.leader = false;
  }

  public void setLeader() {
    this.leader = true;
  }

}

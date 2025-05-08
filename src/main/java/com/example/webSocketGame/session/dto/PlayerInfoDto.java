package com.example.webSocketGame.session.dto;

import lombok.Getter;

@Getter
public class PlayerInfoDto {
  private String nickname;
  private boolean leader;

  public PlayerInfoDto(String nickname, boolean leader) {
    this.nickname = nickname;
    this.leader = leader;
  }

}

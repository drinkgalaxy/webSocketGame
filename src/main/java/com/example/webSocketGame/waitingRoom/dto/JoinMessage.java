package com.example.webSocketGame.waitingRoom.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JoinMessage {
  private String roomId;
  private String nickname;

}

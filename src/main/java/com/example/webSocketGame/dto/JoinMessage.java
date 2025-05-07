package com.example.webSocketGame.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JoinMessage {
  private String roomId;
  private String nickname;

}

package com.example.webSocketGame.game.dto;

import lombok.Getter;

@Getter
public class GameStartMessage {

  private String roomId;
  private int rounds;

}

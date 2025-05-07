package com.example.webSocketGame.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessage {
  private String roomId;
  private String content;
  private String sender; // 닉네임

}

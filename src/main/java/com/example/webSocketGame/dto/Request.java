package com.example.webSocketGame.dto;

public class Request {

	public record Init(String roomId, String nickname) {} // 방 입장 시 사용

	public record Move(String roomId, int position) {} // 수를 둘 때 서버에 전달되는 메시지

	public record Restart(String roomId) {}



}

package com.example.webSocketGame.dto;

public class Response {
	public record Board(String[] board, String turn, String result, String yourMark) {}
	// 서버가 클라이언트에게 전달하는 게임 상태
}

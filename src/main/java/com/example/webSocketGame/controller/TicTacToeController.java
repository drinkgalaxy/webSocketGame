package com.example.webSocketGame.controller;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.example.webSocketGame.dto.Request;
import com.example.webSocketGame.dto.Response;
import com.example.webSocketGame.entity.GameRoom;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class TicTacToeController {

	public static final Map<String, GameRoom> roomMap = new ConcurrentHashMap<>();
	private final SimpMessagingTemplate messagingTemplate;

	// 방 초기화 요청
	@MessageMapping("/init")
	public void initBoard(Request.Init init, SimpMessageHeaderAccessor accessor) {
		String roomId = init.roomId();
		String sessionId = accessor.getSessionId();

		roomMap.putIfAbsent(roomId, new GameRoom());
		GameRoom room = roomMap.get(roomId);

		// 사용자 지정: sessionId를 Principal 처럼 설정
		accessor.setUser(() -> sessionId);

		String yourMark = room.assignPlayer(sessionId, init.nickname());
		if (yourMark == null) {
			messagingTemplate.convertAndSendToUser(sessionId, "/topic/errors", "방이 가득 찼습니다.");
			return;
		}

		Response.Board board = new Response.Board(room.getBoard(), room.getNextTurnMark(), null, yourMark);
		messagingTemplate.convertAndSend("/topic/board/" + roomId, board);
	}

	// 플레이어가 말을 두었을 때
	@MessageMapping("/move")
	public void handleMove(Request.Move move, SimpMessageHeaderAccessor accessor) {

		String roomId = move.roomId();
		String sessionId = accessor.getSessionId();
		GameRoom room = roomMap.get(roomId);

		if (room == null || room.isFinished()) return;

		// 퇴장 후 1명만 남았으면 막기
		if (room.bothPlayersAssigned()) {
			messagingTemplate.convertAndSend("/topic/errors/" + sessionId, "게임은 두 명이 있어야 시작됩니다.");
			return;
		}


		int pos = move.position();
		String[] board = room.getBoard();
		String playerMark = room.getPlayerMark(sessionId);

		if (playerMark == null || !room.isPlayerTurn(sessionId)) return;
		if (pos < 0 || pos > 8 || board[pos] != null && !board[pos].isBlank()) return;

		board[pos] = playerMark;

		if (room.checkWin(playerMark)) {
			room.setFinished(true);
			String winnerName = room.getNicknameByMark(playerMark);
			messagingTemplate.convertAndSend("/topic/board/" + roomId,
				new Response.Board(board, null, winnerName + "무승부!", playerMark));
			return;
		}

		room.incrementTurn();
		if (room.getTurn() == 9) {
			room.setFinished(true);
			messagingTemplate.convertAndSend("/topic/board/" + roomId,
				new Response.Board(board, null, "DRAW", playerMark));
			return;
		}

		messagingTemplate.convertAndSend("/topic/board/" + roomId,
			new Response.Board(board, room.getNextTurnMark(), null, playerMark));
	}

	@MessageMapping("/restart")
	public void restartGame(Request.Restart restart, SimpMessageHeaderAccessor accessor) {
		String roomId = restart.roomId();
		String sessionId = accessor.getSessionId();
		GameRoom room = roomMap.get(roomId);
		if (room == null) return;

		// 요청한 사용자가 이 방의 O 또는 X일 때만 재시작 허용
		if (room.getPlayerMark(sessionId) == null) return;

		room.reset(); // 상태 초기화
		String yourMark = room.getPlayerMark(sessionId);

		messagingTemplate.convertAndSend("/topic/board/" + roomId,
			new Response.Board(room.getBoard(), room.getNextTurnMark(), null, yourMark));
	}
}

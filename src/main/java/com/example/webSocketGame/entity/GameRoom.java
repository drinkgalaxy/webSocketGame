package com.example.webSocketGame.entity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GameRoom {
	private final String[] board = new String[9];
	private int turn = 0;
	private boolean finished = false;

	private String playerO;
	private String playerX;

	private final Map<String, String> nicknames = new HashMap<>();

	public String assignPlayer(String sessionId, String nickname) {
		if (playerO == null) {
			playerO = sessionId;
			nicknames.put(sessionId, nickname);
			return "O";
		} else if (playerX == null && !sessionId.equals(playerO)) {
			playerX = sessionId;
			nicknames.put(sessionId, nickname);
			return "X";
		} else if (sessionId.equals(playerO) || sessionId.equals(playerX)) {
			return getPlayerMark(sessionId);
		}
		return null;
	}

	public String getPlayerMark(String sessionId) {
		if (sessionId.equals(playerO)) return "O";
		if (sessionId.equals(playerX)) return "X";
		return null;
	}

	public boolean isPlayerTurn(String sessionId) {
		String expectedMark = getCurrentTurnMark();
		return expectedMark.equals(getPlayerMark(sessionId));
	}

	public void incrementTurn() {
		this.turn++;
	}

	public String getCurrentTurnMark() {
		return (turn % 2 == 0) ? "O" : "X";
	}

	public String getNextTurnMark() {
		return getCurrentTurnMark();
	}

	public boolean checkWin(String mark) {
		int[][] lines = {
			{0, 1, 2}, {3, 4, 5}, {6, 7, 8},
			{0, 3, 6}, {1, 4, 7}, {2, 5, 8},
			{0, 4, 8}, {2, 4, 6}
		};
		for (int[] line : lines) {
			if (mark.equals(board[line[0]]) &&
				mark.equals(board[line[1]]) &&
				mark.equals(board[line[2]])) {
				return true;
			}
		}
		return false;
	}

	public void reset() {
		Arrays.fill(board, null); // 배열 초기화
		turn = 0;
		finished = false;
	}

	public boolean bothPlayersAssigned() {
		return playerO == null || playerX == null;
	}

	public void removePlayer(String sessionId) {
		if (sessionId.equals(playerO)) playerO = null;
		if (sessionId.equals(playerX)) playerX = null;
		nicknames.remove(sessionId);
	}

	public String getNicknameByMark(String mark) {
		if (mark.equals("O") && playerO != null) return nicknames.get(playerO);
		if (mark.equals("X") && playerX != null) return nicknames.get(playerX);
		return null;
	}

}


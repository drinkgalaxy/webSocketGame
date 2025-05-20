package com.example.webSocketGame.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import com.example.webSocketGame.dto.Request;
import com.example.webSocketGame.entity.Board;

@Controller
public class TicTacToeController {

	private final String[] board = new String[9];
	private int turn = 0;
	private boolean finished = false;

	@MessageMapping("/init")
	@SendTo("/topic/board")
	public Board initBoard() {
		return new Board(board, getNextTurnMark(), null);
	}

	@MessageMapping("/move")
	@SendTo("/topic/board")
	public Board handleMove(Request.Move move) {
		if (finished) return new Board(board, null, "GAME_OVER");

		int pos = move.position();
		if (pos < 0 || pos > 8 || board[pos] != null && !board[pos].isBlank()) {
			return new Board(board, getNextTurnMark(), null);
		}

		String mark = getCurrentTurnMark();
		if (!move.player().equals(mark)) {
			return new Board(board, getNextTurnMark(), null); // 잘못된 턴 무시
		}

		board[pos] = mark;

		if (checkWin(mark)) {
			finished = true;
			return new Board(board, null, "WIN_" + mark);
		}

		turn++;
		if (turn == 9) {
			finished = true;
			return new Board(board, null, "DRAW");
		}

		return new Board(board, getNextTurnMark(), null);
	}

	private String getCurrentTurnMark() {
		return (turn % 2 == 0) ? "O" : "X";
	}

	private String getNextTurnMark() {
		return (turn % 2 == 0) ? "O" : "X";
	}

	private boolean checkWin(String mark) {
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
}

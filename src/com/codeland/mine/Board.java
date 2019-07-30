/*******************************************************************************
 *
 * Copyright (c) 2019 Codeland
 *
 * -----------------------------------------------------------------------------
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files(the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 *******************************************************************************/

package com.codeland.mine;

public class Board {

	private interface Action {
		void execute(int x, int y, Element[][] board);
	}

	private class Element {
		public int state;
		public int mines;

		public Element(int state, int mines) {
			this.state = state;
			this.mines = mines;
		}
	}

	public class AiController {

		public int getState(int x, int y) {
			return board[x][y].state;
		}

		public int getMines(int x, int y) {
			if (board[x][y].state == STATE_PRESSED)
				return board[x][y].mines;
			else
				return -1;
		}
	}

	public static final int
		STATE_UNPRESSED  = 0x00,
		STATE_FLAGGED    = 0x01,
		STATE_DEPRESSED  = 0x02,
		STATE_PRESSED    = 0x03,
		STATE_DEAD       = 0x04;

	private static final int
		MINE = 0x09;

	private static final boolean
		DEPRESS_SELF      = false,
		DEPRESS_NEIGHBORS = true;

	private boolean depressType;
	private int pressX;
	private int pressY;
	private int flagged;

	private Element[][] board;

	public Board() {
		pressX = -1;
		pressY = -1;
		flagged = 0;
		load();
	}

	public int width() {
		return board.length;
	}

	public int height() {
		return board[0].length;
	}

	public int getState(int x, int y) {
		return board[x][y].state;
	}

	public int getMines(int x, int y) {
		return board[x][y].mines;
	}

	private void press(int x, int y) {
		board[x][y].state = STATE_PRESSED;
		if (board[x][y].mines == 0)
			neighbors(x, y, this::bucketClear);
	}

	public void flag(int x, int y) {
		switch (board[x][y].state) {
			case STATE_UNPRESSED: board[x][y].state = STATE_FLAGGED;   break;
			case STATE_FLAGGED:   board[x][y].state = STATE_UNPRESSED; break;
		}
	}

	public void depress(int x, int y) {
		if (pressX != -1 || pressY != -1)
			clearDepression();
		pressX = x;
		pressY = y;
		switch (board[x][y].state) {
			case STATE_UNPRESSED:
				board[x][y].state = STATE_DEPRESSED;
				depressType = DEPRESS_SELF;
				break;
			case STATE_PRESSED:
				flagged = 0;
				neighbors(x, y, (nx, ny, board) -> {
					switch (board[nx][ny].state) {
						case STATE_UNPRESSED: board[nx][ny].state = STATE_DEPRESSED; break;
						case STATE_FLAGGED:   ++flagged;                             break;
					}
				});
				depressType = DEPRESS_NEIGHBORS;
				break;
			default:
				pressX = -1;
				pressY = -1;
				break;
		}
	}

	public void release(int x, int y) {
		if (pressX != x || pressY != y)
			clearDepression();
		else {
			if (depressType == DEPRESS_SELF)
				press(x, y);
			else if (flagged >= getMines(x, y)){ // DEPRESS_NEIGHBORS -> PRESSED
				neighbors(x, y, (nx, ny, board) -> {
					if (board[nx][ny].state == STATE_DEPRESSED)
						press(nx, ny);
				});
			}
			else // DEPRESS_NEIGHBORS -> UNPRESSED
				clearDepression();
			pressX = -1;
			pressY = -1;
		}
	}

	private void clearDepression() {
		if (depressType == DEPRESS_SELF)
			board[pressX][pressY].state = STATE_UNPRESSED;
		else { // DEPRESS_NEIGHBORS
			neighbors(pressX, pressY, (x, y, board) -> {
				if (board[x][y].state == STATE_DEPRESSED)
					board[x][y].state = STATE_UNPRESSED;
			});
		}
		pressX = -1;
		pressY = -1;
	}

	public void iterate(Action action) {
		for (int i = 0; i < width(); ++i)
			for (int j = 0; j < height(); ++j)
				action.execute(i, j, board);
	}

	private void neighbors(int x, int y, Action action) {
		for (int k = -1; k < 2; ++k) {
			for (int k2 = -1; k2 < 2; ++k2) {
				if (k != 0 || k2 != 0) {
					int nx = x + k;
					int ny = y + k2;
					if (nx > -1 && nx < width()
					 && ny > -1 && ny < height())
						action.execute(nx, ny, board);
				}
			}
		};
	}

	private void bucketClear(int x, int y, Element[][] board) {
		if (board[x][y].state == STATE_UNPRESSED) {
			press(x, y);
			if (board[x][y].mines == 0)
				neighbors(x, y, this::bucketClear);
		}
	}

	public void load() {
		boolean[][] mines = loadMines();
		board = new Element[mines.length][mines[0].length];
		iterate((i, j, board) -> {
			board[i][j] = new Element(
				STATE_UNPRESSED,
				mines[i][j] ? MINE : 0
			);
		});
		iterate((i, j, board) -> {
			if (board[i][j].mines == MINE) {
				neighbors(i, j, (x, y, leBoard) -> {
					if (leBoard[x][y].mines != MINE)
						++leBoard[x][y].mines;
				});
			}
		});
	}

	private static boolean[][] loadMines() {
		boolean[][] ret = new boolean[16][16];
		for (int i = 0; i < ret.length; ++i)
			for (int j = 0; j < ret[0].length; ++j)
				if (Math.random() > 0.93)
					ret[i][j] = true;
		return ret;
	}
}

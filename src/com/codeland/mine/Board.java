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

	public interface Action {
		void execute(int x, int y, Element[][] board);
	}

	public class Element {
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
		STATE_UNPRESSED  = 0x00, // Mine count hidden
		STATE_FLAGGED    = 0x01, // Mine count hidden, but flagged as a mine
		STATE_DEPRESSED  = 0x02, // Tile appears pressed, but mine count is hidden. Occurs while mouse is held down
		STATE_PRESSED    = 0x03, // Mine count revealed
		STATE_DEAD       = 0x04, // If a mine is pressed that tile is set as the dead tile
		STATE_WIN        = 0x05; // If the game is won all mines are revealed in green

	public static final int
		STATUS_PLAYING = 0x00,
		STATUS_WIN     = 0x01,
		STATUS_LOSE    = 0x02;

	private static final int MINE = 0x09; // The ID of a mine

	private static final int BUCKET_FLAG = 0x01 << 16;

	private static final boolean
		DEPRESS_SELF      = false, // If the tile itself was depressed. E.g. a tile that was unpressed was sent the depress trigger
		DEPRESS_NEIGHBORS = true;  // If the tile's neighbors are being depressed. E.g. a tile that is pressed was sent the depress trigger

	private boolean depressType; // The type of depression that occured { DEPRESS_SELF | DEPRESS_NEIGHBORS }
	private int pressX;          // The x index of the depressed tile
	private int pressY;          // The y index of the depressed tile
	private int flagged;         // If type is DEPRESS_NEIGHBORS, the number of flags adjacent to the depressed tile

	private Element[][] board;   // The actual board state

	private int status;

	public Board() {
		pressX = -1;
		pressY = -1;
		flagged = 0;
		load();
	}

	/**
	 * Gets the width of the board
	 *
	 * @return Returns the width of the board
	 */
	public int width() {
		return board.length;
	}

	/**
	 * Gets the height of the board
	 *
	 * @return Returns the height of the board
	 */
	public int height() {
		return board[0].length;
	}

	/**
	 * Presses a tile
	 *
	 * @param x - The x coordinate of the tile to press
	 * @param y - The y coordinate of the tile to press
	 */
	private void press(int x, int y) {
		// Set the state of the pressed tile to pressed
		board[x][y].state = STATE_PRESSED;
		// If the tile had no adjacent mines and wasn't
		// a mine itself press all of it's neighbors as well
		if (board[x][y].mines == 0)
			neighbors(x, y, this::bucketClear);
		if (board[x][y].mines == MINE) {
			status = STATUS_LOSE;
			iterate((nx, ny, board) -> board[nx][ny].state = STATE_PRESSED);
			board[x][y].state = STATE_DEAD;
		}
		else {
			status = STATUS_WIN;
			iterate((nx, ny, board) -> {
				if (board[nx][ny].state == STATE_UNPRESSED && board[nx][ny].mines != MINE)
					status = STATUS_PLAYING;
			});
			System.out.println(status);
			if (status == STATUS_WIN) {
				iterate((nx, ny, board) -> {
					if (board[nx][ny].mines == MINE)
						board[nx][ny].state = STATE_WIN;
				});
			}
		}
	}

	/**
	 * Toggles the flag state on a tile
	 *
	 * @param x - The x coordinate of the tile to flag
	 * @param y - The y coordinate of the tile to flag
	 */
	public void flag(int x, int y) {
		switch (board[x][y].state) {
			// If the tile is unpressed, flag it
			case STATE_UNPRESSED: board[x][y].state = STATE_FLAGGED;   break;
			// If the tile is flagged, unflag it
			case STATE_FLAGGED:   board[x][y].state = STATE_UNPRESSED; break;
			// If the tile is neither flagged nor unpressed, do nothing
		}
	}

	/**
	 * Depresses the tile at position x, y
	 * Clears any previously depressed tiles if they haven't been released already
	 *
	 * @param x - The x coordinate of the tile to depress
	 * @param y - The y coordinate of the tile to depress
	 */
	public void depress(int x, int y) {
		// If a tile is already depressed clear it
		if (pressX != -1 || pressY != -1)
			clearDepression();
		// Set the currently depressed tile index to x, y
		pressX = x;
		pressY = y;
		switch (board[x][y].state) {
			// If the tile is unpressed, depress it and set the depress type to DEPRESS_SELF
			case STATE_UNPRESSED:
				board[x][y].state = STATE_DEPRESSED;
				depressType = DEPRESS_SELF;
				break;
			// If the tile is pressed, depress it's neighbors and set the depress type to DEPRESS_NEIGHBORS
			case STATE_PRESSED:
				// Reset the number of adjacent flags
				flagged = 0;
				// For each neighbor...
				neighbors(x, y, (nx, ny, board) -> {
					switch (board[nx][ny].state) {
						// If the neighbor is unpressed, depress it
						case STATE_UNPRESSED: board[nx][ny].state = STATE_DEPRESSED; break;
						// If the neighbor is flagged, increment the number of adjacent flags
						case STATE_FLAGGED:   ++flagged;                             break;
					}
				});
				depressType = DEPRESS_NEIGHBORS;
				break;
			// If the tile is neither pressed nor unpressed, reset the depressed tile index and ignore it
			default:
				pressX = -1;
				pressY = -1;
				break;
		}
	}

	/**
	 * Releases the tile at position x, y if it's depressed
	 * If it's not depressed clear the active depression and reset the depressed tile index
	 *
	 * If the depression type is DEPRESS_SELF, set it's state to pressed
	 * If the depression type is DEPRESS_NEIGHBOR and
	 *     the number of adjacent flags is >= to the number of mine neighbors, set all depressed neigbors to pressed
	 *     the number of adjacent flags is <  to the number of mine neighbors, set all depressed neigbors to unpressed
	 *
	 * @param x - The x coordinate of the tile to release
	 * @param y - The y coordinate of the tile to release
	 */
	public void release(int x, int y) {
		if (pressX != -1 && pressY != -1) {
			// If the tile that was depressed and the tile being released are different, reset the depressed tile index and ignore it
			if (pressX != x || pressY != y)
				clearDepression();
			else {
				// If the depression type is DEPRESS_SELF, press it
				if (depressType == DEPRESS_SELF)
					press(x, y);
					// If the depression type is DEPRESS_NEIGHBORS and the adjacent flag count is enough, then press all depressed neighbors
				else if (flagged >= board[x][y].mines) {
					neighbors(x, y, (nx, ny, board) -> {
						if (board[nx][ny].state == STATE_DEPRESSED)
							press(nx, ny);
					});
				}
				// If the depression type is DEPRESS_NEIGHBORS and the adjacent flag count is not enough, then unpress all the depressed neighbors
				else
					clearDepression();
				// Reset the depressed tile index
				pressX = -1;
				pressY = -1;
			}
		}
	}

	/**
	 * Resets the currently depressed tile(s) to unpressed
	 */
	private void clearDepression() {
		// If the depress type is DEPRESS_SELF, unpress it
		if (depressType == DEPRESS_SELF)
			board[pressX][pressY].state = STATE_UNPRESSED;
		// If the depress type is DEPRESS_NEIGHBORS, unpress it's depressed neighbors
		else {
			neighbors(pressX, pressY, (x, y, board) -> {
				if (board[x][y].state == STATE_DEPRESSED)
					board[x][y].state = STATE_UNPRESSED;
			});
		}
		// Reset the depressed tile index
		pressX = -1;
		pressY = -1;
	}

	/**
	 * Iterate over the entire board and perform the action and performs the action passed
	 *
	 * @param action - The action to perform on each tile
	 */
	public void iterate(Action action) {
		for (int i = 0; i < width(); ++i)
			for (int j = 0; j < height(); ++j)
				action.execute(i, j, board);
	}

	/**
	 * Iterates over each neighbor of a tile and performs the action passed
	 *
	 * @param x - The x coordinate of the tile whose neighbors to iterate over
	 * @param y - The y coordinate of the tile whose neighbors to iterate over
	 * @param action - The action to perform on each neighbor
	 */
	private void neighbors(int x, int y, Action action) {
		// Starting -1, -1 (top left) relative to the given tile
		// go through each neighbor until 1, 1 is reached
		for (int k = -1; k < 2; ++k) {
			for (int k2 = -1; k2 < 2; ++k2) {
				// If this is not the tile itself
				if (k != 0 || k2 != 0) {
					// Calculate the neighbor's coordinate
					int nx = x + k;
					int ny = y + k2;
					// If the tile is not outside the bounds of the board
					if (nx > -1 && nx < width()
					 && ny > -1 && ny < height())
						// Perform the action
						action.execute(nx, ny, board);
				}
			}
		}
	}

	/**
	 * Bucket fill through the board and reveal all contiguous empty tile
	 *
	 * @param x - The x coordinate of the empty tile
	 * @param y - The y coordinate of the empty tile
	 * @param board - The board to bucket through
	 */
	private void bucketClear(int x, int y, Element[][] board) {
		PositionStack stack = new PositionStack(board.length * board[0].length);
		board[x][y].state ^= BUCKET_FLAG;
		stack.push(x * height() + y);
		while (!stack.isEmpty()) {
			x = stack.pop();
			y = x % board[0].length;
			x /= board[0].length;
			board[x][y].state ^= BUCKET_FLAG;
			neighbors(x, y, (nx, ny, leBoard) -> {
				if (leBoard[nx][ny].state == STATE_UNPRESSED) {
					leBoard[nx][ny].state = STATE_PRESSED;
					if (leBoard[nx][ny].mines == 0
							&& (leBoard[nx][ny].state & BUCKET_FLAG) == 0) {
						leBoard[nx][ny].state ^= BUCKET_FLAG;
						stack.push(nx * height() + ny);
					}
				}
			});
		}
	}

	public int checkStatus() {
		return status;
	}

	public boolean depressed() {
		return pressX != -1 && pressY != -1;
	}

	/**
	 * Load the board
	 */
	public void load() {
		status = STATUS_PLAYING;
		// Get the mine positions
		boolean[][] mines = loadMines();
		// Create the board
		board = new Element[mines.length][mines[0].length];
		// Set the initial state of the tile and copy the mine positions in
		iterate((i, j, board) -> {
			board[i][j] = new Element(
				// All tiles start unpressed
				STATE_UNPRESSED,
				// If there is a mine here set the tile to a mine
				// otherwise set it to 0 neighbors (determine later)
				mines[i][j] ? MINE : 0
			);
		});
		// Determine the number of mine neighbors each tile has
		iterate((i, j, board) -> {
			// If the tile is a mine
			if (board[i][j].mines == MINE) {
				// Increment all it's non bomb neighbors by 1
				neighbors(i, j, (x, y, leBoard) -> {
					if (leBoard[x][y].mines != MINE)
						++leBoard[x][y].mines;
				});
			}
		});
	}

	/**
	 * Load the mine positions
	 * false - no mine
	 * true  - mine
	 *
	 * @return Returns an array of booleans which represents the mine layout
	 */
	private static boolean[][] loadMines() {
		boolean[][] ret = new boolean[30][30];
		for (int i = 0; i < ret.length; ++i)
			for (int j = 0; j < ret[0].length; ++j)
				if (Math.random() > 0.98)
					ret[i][j] = true;
		return ret;
	}
}

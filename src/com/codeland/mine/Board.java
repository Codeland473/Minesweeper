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

import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

	public static final int MINE = 0x09; // The ID of a mine

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

	private int mineCount;
	private String seed;

	public Board() {
		pressX = -1;
		pressY = -1;
		flagged = 0;
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
		if (seed == null)
			load(x * height() + y);
		// If the tile had no adjacent mines and wasn't
		// a mine itself press all of it's neighbors as well
		if (board[x][y].mines == 0)
			bucketClear(x, y, board);
		if (board[x][y].mines == MINE) {
			status = STATUS_LOSE;
			iterate((nx, ny, board) -> board[nx][ny].state = STATE_PRESSED);
			board[x][y].state = STATE_DEAD;
		} else {
			status = STATUS_WIN;
			iterate((nx, ny, board) -> {
				if (board[nx][ny].state == STATE_UNPRESSED && board[nx][ny].mines != MINE)
					status = STATUS_PLAYING;
			});
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

	private static class Int {
		public int num;
	}

	public int flagCount(int x, int y) {
		Int count = new Int();
		neighbors(x, y, (nx, ny, board) -> {
			if (board[nx][ny].state == STATE_FLAGGED)
				++count.num;
		});
		if (count.num > board[x][y].mines)
			return 2;
		else if (count.num == board[x][y].mines)
			return 1;
		else
			return 0;
	}

	public void reset(int width, int height, int mineCount) {
		status = STATUS_PLAYING;
		// Create the board
		board = new Element[width][height];
		// Set the initial state of the tile and copy the mine positions in
		iterate((i, j, board) -> board[i][j] = new Element(STATE_UNPRESSED, 0));

		seed = null;
		this.mineCount = mineCount;
	}

	private static class Biterator {

		private char[] hex;
		private int index;
		private int subindex;
		private int bits;

		public Biterator(String hex) {
			this.hex = hex.toCharArray();
			index = -1;
		}

		public boolean next() {
			if (subindex == 0) {
				if (index == hex.length - 1)
					return false;
				bits = fromHex(hex[++index]);
			}
			boolean ret = ((bits >> (3 - subindex)) & 1) > 0;
			subindex = (subindex + 1) % 4;
			return ret;
		}

		private static int fromHex(char c) {
			int bits;
			if (c >= 'A' && c <= 'F')
				bits = c - 'A' + 10;
			else if (c >= 'a' && c <= 'f')
				bits = c - 'a' + 10;
			else
				bits = c - '0';
			return bits;
		}
	}

	public void reset(String seed) {
		final String SEED_REGEX = "(\\d+)x(\\d+):(\\d+)#([\\da-fA-F]+)";
		if (seed != null && seed.matches(SEED_REGEX)) {
			this.seed = seed;
			Matcher match = Pattern.compile(SEED_REGEX).matcher(seed);
			match.find();
			final int width = Integer.parseInt(match.group(1));
			final int height = Integer.parseInt(match.group(2));
			final int firstPress = Integer.parseInt(match.group(3));
			status = STATUS_PLAYING;
			board = new Element[width][height];
			iterate((i, j, board) -> board[i][j] = new Element(STATE_UNPRESSED, 0));
			Biterator bits = new Biterator(match.group(4));
			mineCount = 0;
			iterate((i, j, board) -> {
				board[i][j].mines = bits.next() ? MINE : 0;
				++mineCount;
			});
			determineNeighbors();
			press(firstPress / height(), firstPress % height);

			int[][] mineBoard = new int[width()][height()];
			iterate((i, j, board) -> mineBoard[i][j] = board[i][j].mines);
			BruteSolver solver = new BruteSolver(mineBoard, firstPress / height(), firstPress % height());
			System.out.println("canBeSolved " + solver.isSolvable());
		}
		else
			reset("16x16:0#000E0A88C8808AA8A8FE3D6057E17B95F002F8A3C4210391870C03B8C20C1063");
	}

	/**
	 * Load the board
	 */
	public void load(int firstPress) {
		boolean[][] mines = BruteSolver.getSolvableBoard(width(), height(), mineCount, firstPress / height(), firstPress % height());
		iterate((i, j, board) -> board[i][j].mines = mines[i][j] ? MINE : 0);
		determineNeighbors();

		int[][] mineBoard = new int[width()][height()];
		iterate((i, j, board) -> mineBoard[i][j] = board[i][j].mines);
		calculateSeed(width(), height(), firstPress);

		BruteSolver solver = new BruteSolver(mineBoard, firstPress / height(), firstPress % height());
		System.out.println("canBeSolved " + solver.isSolvable());
	}

	private void determineNeighbors() {
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

	private void calculateSeed(int width, int height, int firstPress) {
		int length = width() * height();
		int digit;
		StringBuilder seed = new StringBuilder();
		seed.append(width)
		    .append('x')
		    .append(height)
		    .append(':')
		    .append(firstPress)
		    .append('#');
		for (int i = 0; i < length; i += 4) {
			digit = 0;
			for (int j = i; j < i + 4; ++j) {
				digit <<= 1;
				if (j < length)
					digit |= (board[j / height()][j % height()].mines == MINE) ? 1 : 0;
			}
			seed.append(hexDigit(digit));
		}
		System.out.println(seed.toString());
		this.seed = seed.toString();
	}

	private static String calculateSeed(int width, int height, int firstPress, boolean[][] field) {
		int length = width * height;
		int digit;
		StringBuilder seed = new StringBuilder();
		seed.append(width)
				.append('x')
				.append(height)
				.append(':')
				.append(firstPress)
				.append('#');
		for (int i = 0; i < length; i += 4) {
			digit = 0;
			for (int j = i; j < i + 4; ++j) {
				digit <<= 1;
				if (j < length)
					digit |= (field[j / height][j % height]) ? 1 : 0;
			}
			seed.append(hexDigit(digit));
		}
		System.out.println(seed.toString());
		return seed.toString();
	}

	private static class MineLocation {

		private int index;
		private int offset;

		public MineLocation(int index) {
			this.index = index;
			offset = 1;
		}

		public int getIndex() {
			return index;
		}

		public void increment(int amount) {
			offset += amount;
		}

		public int getOffset() {
			return offset;
		}
	}

	/**
	 * Load the mine positions
	 * false - no mine
	 * true  - mine
	 *
	 * @return Returns an array of booleans which represents the mine layout
	 */
	private static boolean[][] loadMines(int width, int height, int firstPress, int mineCount) {
		Random random = new Random();
		ArrayList<MineLocation> occupied = new ArrayList<>();
		occupied.add(new MineLocation(firstPress));
		boolean[][] field = new boolean[width][height];
		for (int i = 0; i < mineCount; ++i) {
			int index = random.nextInt(width * height - i - 1);
			int j = 0;
			for (; j < occupied.size() && occupied.get(j).getIndex() <= index; ++j)
				index += occupied.get(j).getOffset();
			field[index / height][index % height] = true;
			occupied.add(j, new MineLocation(index));
			if (j < occupied.size() - 1)
				merge(occupied, j);
			if (j > 0)
				merge(occupied, j - 1);
		}
		return field;
	}

	private static boolean[][] loadSolvableMines(int width, int height, int firstPress, int mineCount) {
		return BruteSolver.getSolvableBoard(width, height, mineCount, firstPress / height, firstPress % height);
	}

	private static void merge(ArrayList<MineLocation> occupied, int index) {
		MineLocation lower = occupied.get(index);
		MineLocation upper = occupied.get(index + 1);
		if (lower.getIndex() + lower.getOffset() == upper.getIndex()) {
			lower.increment(upper.getOffset());
			occupied.remove(index + 1);
		}
	}

	public String getSeed() {
		return seed;
	}

	private static char hexDigit(int num) {
		if (num > 9) return (char) ('A' + num - 10);
		else         return (char) ('0' + num     );
	}
}

package com.codeland.mine;

import java.util.Random;

public class BruteSolver {
	private int[][] board;
	private int[][] realBoard;
	private int mineCount;
	/**
	 * 0 = no info
	 * 1 = some info
	 * 2 = known
	 */

	public BruteSolver() {
		board = null;
		realBoard = null;
		mineCount = 0;
	}

	public static boolean isSolvable(boolean[][] field, int startX, int startY) {
		BruteSolver b = new BruteSolver(getBoardFromMines(field), startX, startY);
		return b.isSolvable();
	}

	public static boolean[][] getSolvable(int width, int height, int mines, int startX, int startY) {
		boolean[][] field = getMines(width, height, mines, startX, startY);
		BruteSolver b = new BruteSolver(getBoardFromMines(field), startX, startY);
		int tempMines = 0;
		int prevTempMines = 0;
		int numRepetitions = 0;
		while (!b.isSolvable()) {
			tempMines = 0;
			if (b.isBoxedOut()) {
				for (int i = 0; i < width; ++i) {
					for (int j = 0; j < height; ++j) {
						if (b.board[i][j] < 0 || b.board[i][j] > 9 || (field[i][j] && b.boardersUnknown(i, j))) {
							if (field[i][j]) {
								b.board[i][j] = 10;
								field[i][j] = false;
								++tempMines;
							}
						}
					}
				}
			} else {
				for (int i = 0; i < width; ++i) {
					for (int j = 0; j < height; ++j) {
						if (b.board[i][j] < 0 || b.board[i][j] > 9) {
							if (field[i][j]) {
								field[i][j] = false;
								++tempMines;
							}
						}
					}
				}
			}
			if (tempMines == prevTempMines) {
				++numRepetitions;
				if (numRepetitions >= 20 + tempMines) {
					return getSolvable(width, height, mines, startX, startY);
				}
			} else {
				prevTempMines = tempMines;
				numRepetitions = 0;
			}
			System.out.println(tempMines);
			if (tempMines == 0) {
				break;
			}
			for (int i = 0; i < tempMines; ++i) {
				int x = (int) (Math.random() * width);
				int y = (int) (Math.random() * height);
				if (b.board[x][y] < 0 || b.board[x][y] > 9) {
					if (!field[x][y]) {
						field[x][y] = true;
					} else {
						--i;
					}
				} else {
					--i;
				}
			}

			b = new BruteSolver(getBoardFromMines(field), startX, startY);
		}
		tempMines = 0;
		for (int i = 0; i < width; ++i) {
			for (int j = 0; j < height; ++j) {
				if (field[i][j]) {
					++tempMines;
				}
			}
		}
		System.out.println(tempMines);
		return field;
	}

	private boolean isBoxedOut() {
		for (int i = 0; i < board.length; ++i) {
			for (int j = 0; j < board[0].length; ++j) {
				if (board[i][j] >= 0 && board[i][j] < 9) {
					if (boardersUnknown(i, j)) {
						return false;
					}
				}
			}
		}
		return true;
	}

	public boolean boardersUnknown(int x, int y) {
		for (int di = -1; di <= 1; ++di) {
			for (int dj = -1; dj <= 1; ++dj) {
				if (x + di > 0 && y + dj > 0 && x + di < board.length && y + dj < board[0].length) {
					if (board[x + di][y + dj] == -1) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public BruteSolver(int[][] fullBoard, int startX, int startY) {
		board = new int[fullBoard.length][fullBoard[0].length];
		realBoard = fullBoard;
		for (int i = 0; i < fullBoard.length; ++i) {
			for (int j = 0; j < fullBoard[0].length; ++j) {
				if (fullBoard[i][j] >= 9) {
					++mineCount;
				}
				board[i][j] = -1;
			}
		}
		board[startX][startY] = fullBoard[startX][startY];
	}

	public void reUse(int[][] fullBoard, int startX, int startY) {
		board = new int[fullBoard.length][fullBoard[0].length];
		realBoard = fullBoard;
		for (int i = 0; i < fullBoard.length; ++i) {
			for (int j = 0; j < fullBoard[0].length; ++j) {
				if (fullBoard[i][j] >= 9) {
					++mineCount;
				}
				board[i][j] = -1;
			}
		}
		board[startX][startY] = fullBoard[startX][startY];
	}

	public boolean isSolvable() {
		while (true) {
			attemptLinnearProgress();
			if (canFinish()) {
				System.out.println("true");
				return true;
			}
			if (!attemptDeepProgress()) {
				System.out.println("false");
				return false;
			}
		}
	}

	public static boolean[][] getMines(int width, int height, int mineCount, int startX, int startY) {
		Random random = new Random();
		boolean[][] field = new boolean[width][height];
		for (int i = 0; i < mineCount; ++i) {
			int index = random.nextInt(width * height);
			if (!field[index / height][index % height] && (Math.abs(index / height - startX) > 1 || Math.abs(index % height - startY) > 1)) {
				field[index / height][index % height] = true;
			} else {
				--i;
			}
		}
		return field;
	}

	public static int[][] getBoardFromMines(boolean[][] mines) {
		int[][] board = new int[mines.length][mines[0].length];
		for (int x = 0; x < mines.length; ++x) {
			for (int y = 0; y < mines[0].length; ++y) {
				if (mines[x][y]) {
					board[x][y] = 9;
				} else {
					for (int i = -1; i <= 1; ++i) {
						for (int j = -1; j <= 1; ++j) {
							if (i != j || i != 0) {
								if (x + i >= 0 && y + j >= 0 && x + i < mines.length && y + j < mines[0].length) {
									if (mines[x + i][y + j]) {
										++board[x][y];
									}
								}
							}
						}
					}
				}
			}
		}
		return board;
	}

	public static int[][] setBoardFromMines(boolean[][] mines, int[][] board) {
		for (int x = 0; x < mines.length; ++x) {
			for (int y = 0; y < mines[0].length; ++y) {
				if (mines[x][y]) {
					board[x][y] = 9;
				} else {
					board[x][y] = 0;
					for (int i = -1; i <= 1; ++i) {
						for (int j = -1; j <= 1; ++j) {
							if (i != j || i != 0) {
								if (x + i >= 0 && y + j >= 0 && x + i < mines.length && y + j < mines[0].length) {
									if (mines[x + i][y + j]) {
										++board[x][y];
									}
								}
							}
						}
					}
				}
			}
		}
		return board;
	}

	private boolean attemptDeepProgress() {
		int[][] temp = copyBoard();
		for (int i = 0; i < board.length; ++i) {
			for (int j = 0; j < board[0].length; ++j) {
				if (board[i][j] == -1) {
					if (getBoardState(i, j) == 1) {
						if (!deepCanBeMine(i, j, temp)) {
							board[i][j] = realBoard[i][j];
							return true;
						}
						if (!deepCanBeNotMine(i, j, temp)) {
							board[i][j] = 9;
							return true;
						}
					}
					/*boolean canBeMine = deepCanBeMine(i, j, temp);
					boolean canBeNotMine = deepCanBeNotMine(i, j, temp);
					if (canBeMine != canBeNotMine) {
						if (canBeMine) {
							board[i][j] = 9;
						} else {
							board[i][j] = realBoard[i][j];
						}
						return true;
					}*/
				}
			}
		}
		return false;
	}

	private boolean layoutIsPossible() {
		attemptSafeLinnearProgress();
		if (boardHasInconsistencies()) {
			return false;
		}
		if (getRemainingMines() == 0) {
			return true;
		}
		/*if (canFinish()) {
			return true;
		}*/
		if (allEdgesGuessed()) {
			if (getNoInfoCount() >= getRemainingMines()) {
				if (allNumbersAreSatisfied()) {
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		}
		int[][] temp = copyBoard();
		for (int i = 0; i < board.length; ++i) {
			for (int j = 0; j < board[0].length; ++j) {
				if (board[i][j] == -1) {
					if (getBoardState(i, j) == 1) {
						if (deepCanBeMine(i, j, temp)) {
							return true;
						}
						if (deepCanBeNotMine(i, j, temp)) {
							return true;
						}
						return false;
					}
				}
			}
		}
		System.out.println("something is wrong");
		return false;
	}

	private boolean attemptLinnearProgress() {
		boolean ret = false;
		while (true) {
			if (!flagMustBeMines() && !openCantBeMines()) {
				break;
			} else {
				ret = true;
			}
		}
		return false;
	}

	private boolean attemptSafeLinnearProgress() {
		boolean ret = false;
		while (true) {
			if (!markMustBeMines() && !markCantBeMines()) {
				break;
			} else {
				ret = true;
			}
		}
		return false;
	}

	private boolean flagMustBeMines() {
		boolean ret = false;
		for (int i = 0; i < board.length; ++i) {
			for (int j = 0; j < board[0].length; ++j) {
				if (board[i][j] == -1) {
					if (canBeMine(i, j)) {
						if (mustBeMine(i, j)) {
							board[i][j] = 9;
							ret = true;
						}
					}
				}
			}
		}
		return ret;
	}

	private boolean markMustBeMines() {
		boolean ret = false;
		for (int i = 0; i < board.length; ++i) {
			for (int j = 0; j < board[0].length; ++j) {
				if (board[i][j] == -1) {
					if (canBeMine(i, j)) {
						if (mustBeMine(i, j)) {
							board[i][j] = 10;
							ret = true;
						}
					}
				}
			}
		}
		return ret;
	}

	private boolean openCantBeMines() {
		boolean ret = false;
		for (int i = 0; i < board.length; ++i) {
			for (int j = 0; j < board[0].length; ++j) {
				if (board[i][j] == -1) {
					if (!canBeMine(i, j)) {
						board[i][j] = realBoard[i][j];
						ret = true;
					}
				}
			}
		}
		return ret;
	}

	private boolean markCantBeMines() {
		boolean ret = false;
		for (int i = 0; i < board.length; ++i) {
			for (int j = 0; j < board[0].length; ++j) {
				if (board[i][j] == -1) {
					if (!canBeMine(i, j)) {
						board[i][j] = -2;
						ret = true;
					}
				}
			}
		}
		return ret;
	}

	private boolean boardHasInconsistencies() {
		int remainingMines = getRemainingMines();
		for (int i = 0; i < board.length; ++i) {
			for (int j = 0; j < board[0].length; ++j) {
				int val = board[i][j];
				if (val >= 0 && val <= 8) {
					int remainingMineNeighbors = remainingMineNeighbors(i, j);
					if (remainingMineSpaces(i, j) < 0 || remainingMineNeighbors > remainingMineSpaces(i, j) || remainingMines < remainingMineNeighbors) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private boolean canBeMine(int x, int y) {
		for (int i = -1; i <= 1; ++i) {
			for (int j = -1; j <= 1; ++j) {
				if (x + i >= 0 && x + i < board.length) {
					if (y + j >= 0 && y + j < board[0].length) {
						if (!(i == j && i == 0)) {
							if (board[x + i][y + j] != -1) {
								if (remainingMineNeighbors(x + i, y + j) == 0) {
									return false;
								}
							}
						}
					}
				}
			}
		}
		return true;
	}

	private boolean deepCanBeMine(int x, int y, int[][] temp) {
		boolean ret = canBeMine(x, y);
		if (ret) {
			board[x][y] = 10;
			ret = layoutIsPossible();
			board = copyBoard(temp);
		}
		return ret;
	}

	private boolean deepCanBeMine(int x, int y) {
		boolean ret = canBeMine(x, y);
		if (ret) {
			int[][] temp = copyBoard();
			board[x][y] = 10;
			ret = layoutIsPossible();
			board = copyBoard(temp);
		}
		return ret;
	}

	private boolean deepCanBeNotMine(int x, int y, int[][] temp) {
		boolean ret = !mustBeMine(x, y);
		if (ret) {
			board[x][y] = -2;
			ret = layoutIsPossible();
			board = copyBoard(temp);
		}
		return ret;
	}

	private boolean deepCanBeNotMine(int x, int y) {
		boolean ret = !mustBeMine(x, y);
		if (ret) {
			int[][] temp = copyBoard();
			board[x][y] = -2;
			ret = layoutIsPossible();
			board = copyBoard(temp);
		}
		return ret;
	}

	private boolean mustBeMine(int x, int y) {
		for (int i = -1; i <= 1; ++i) {
			for (int j = -1; j <= 1; ++j) {
				if (x + i >= 0 && x + i < board.length) {
					if (y + j >= 0 && y + j < board[0].length) {
						if (!(i == j && i == 0)) {
							if (board[x + i][y + j] >= 0 && board[x + i][y + j] < 9) {
								int neighbors = remainingMineNeighbors(x + i, y + j);
								int remainingMineSpaces = remainingMineSpaces(x + i, y + j);
								if (neighbors == remainingMineSpaces) {
									return true;
								}
							}
						}
					}
				}
			}
		}
		return false;
	}

	private int remainingMineNeighbors(int x, int y) {
		int ret = board[x][y];
		for (int i = -1; i <= 1; ++i) {
			for (int j = -1; j <= 1; ++j) {
				if (x + i >= 0 && x + i < board.length) {
					if (y + j >= 0 && y + j < board[0].length) {
						if (!(i == j && i == 0)) {
							if (board[x + i][y + j] >= 9) {
								--ret;
							}
						}
					}
				}
			}
		}
		return ret;
	}

	private int remainingMineSpaces(int x, int y) {
		int ret = 8;
		for (int i = -1; i <= 1; ++i) {
			for (int j = -1; j <= 1; ++j) {
				if (x + i >= 0 && x + i < board.length) {
					if (y + j >= 0 && y + j < board[0].length) {
						if (!(i == j && i == 0)) {
							if (board[x + i][y + j] != -1) {
								--ret;
							}
						}
					} else {
						--ret;
					}
				} else {
					--ret;
				}
			}
		}
		return ret;
	}

	private boolean canFinish() {
		if (getRemainingMines() == 0) {
			return true;
		}
		int remainingOpen = 0;
		for (int i = 0; i < board.length; ++i) {
			for (int j = 0; j < board[0].length; ++j) {
				if (board[i][j] == -1) {
					++remainingOpen;
				}
			}
		}
		return remainingOpen == 0 || remainingOpen == getRemainingMines();
	}

	private int getRemainingMines() {
		int ret = mineCount;
		for (int i = 0; i < board.length; ++i) {
			for (int j = 0; j < board[0].length; ++j) {
				if (board[i][j] >= 9) {
					--ret;
				}
			}
		}
		return ret;
	}

	private int[][] copyBoard() {
		int[][] ret =  new int[board.length][board[0].length];
		for (int i = 0; i < board.length; ++i) {
			for (int j = 0; j < board[0].length; ++j) {
				ret[i][j] = board[i][j];
			}
		}
		return ret;
	}

	public static int[][] copyBoard(int[][] board) {
		int[][] ret =  new int[board.length][board[0].length];
		for (int i = 0; i < board.length; ++i) {
			for (int j = 0; j < board[0].length; ++j) {
				ret[i][j] = board[i][j];
			}
		}
		return ret;
	}

	private int getBoardState(int x, int y) {
		if (board[x][y] >= 0 && board[x][y] <= 9) {
			return 2;
		}
		for (int i = -1; i <= 1; ++i) {
			for (int j = -1; j <= 1; ++j) {
				if (x + i >= 0 && x + i < board.length) {
					if (y + j >= 0 && y + j < board[0].length) {
						if (!(i == j && i == 0)) {
							if (board[x + i][y + j] >= 0 && board[x + i][y + j] < 9) {
								return 1;
							}
						}
					}
				}
			}
		}
		return 0;
	}

	private int getNoInfoCount() {
		int ret = 0;
		for (int i = 0; i < board.length; ++i) {
			for (int j = 0; j < board[0].length; ++j) {
				if (getBoardState(i, j) == 0) {
					++ret;
				}
			}
		}
		return ret;
	}

	private boolean allEdgesGuessed() {
		for (int i = 0; i < board.length; ++i) {
			for (int j = 0; j < board[0].length; ++j) {
				int boardState = getBoardState(i, j);
				if (boardState == 1 && board[i][j] > -2 && board[i][j] < 10) {
					return false;
				}
			}
		}
		return true;
	}

	private boolean allNumbersAreSatisfied() {
		for (int i = 0; i < board.length; ++i) {
			for (int j = 0; j < board[0].length; ++j) {
				if (board[i][j] >= 0 && board[i][j] < 9) {
					if (remainingMineNeighbors(i, j) != 0) {
						return false;
					}
				}
			}
		}
		return true;
	}
}

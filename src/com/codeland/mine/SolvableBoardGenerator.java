package com.codeland.mine;

import java.util.Random;

public class SolvableBoardGenerator implements Runnable {
	private float progress = 0;
	private boolean completed = true;
	private boolean[][] field;

	private int startX;
	private int startY;

	private int[][] board;
	private int[][] realBoard;
	private int mineCount;

	private boolean prepared = false;

	public void prepare(int width, int height, int mines, int startingX, int startingY) {
		field = new boolean[width][height];
		realBoard = new int[width][height];
		board = new int[width][height];
		startX = startingX;
		startY = startingY;
		mineCount = mines;
		completed = false;
		prepared = true;
	}

	@Override
	public void run() {
		prepared = false;
		completed = false;
		getNewMines();
		updateRealBoard();
		resetGuessBoard();
		while (true) {
			progress = (float) getKnownCount() / (float) (width() * height());
			attemptLinearProgress();
			if (canFinish()) {
				break;
			}
			if (!attemptDeepProgress()) {
				//current layout is impossible, re-organize
				redistribute();
				updateRealBoard();
				board[startX][startY] = realBoard[startX][startY];
				/*getNewMines();
				updateRealBoard();
				resetGuessBoard();*/
			}
		}
		completed = true;
	}

	private int redistributableMineCount() {
		int ret = 0;
		for (int i = 0; i < width(); ++i) {
			for (int j = 0; j < height(); ++j) {
				if (field[i][j] && boardersUnknown(i, j)) {
					board[i][j] = 12;
					field[i][j] = false;
					++ret;
				}
			}
		}
		for (int i = 0; i < width(); ++i) {
			for (int j = 0; j < height(); ++j) {
				if (board[i][j] == 12) {
					board[i][j] = -1;
				}
			}
		}
		for (int i = 0; i < width(); ++i) {
			for (int j = 0; j < height(); ++j) {
				if (boardersUnknown(i, j) && board[i][j] != -1) {
					board[i][j] = 12;
				}
			}
		}
		for (int i = 0; i < width(); ++i) {
			for (int j = 0; j < height(); ++j) {
				if (boardersNumber(i, j, 12) && board[i][j] != -1) {
					board[i][j] = 12;
				}
			}
		}
		return ret;
	}

	private void doBasicRedistribute(int minesToRedistribute) {
		for (int i = 0; i < minesToRedistribute; ++i) {
			int x = (int) (Math.random() * width());
			int y = (int) (Math.random() * height());
			if ((board[x][y] < 0 || board[x][y] > 9) &&
					!field[x][y] &&
					board[x][y] != 12) {
				field[x][y] = true;
			} else {
				--i;
			}
		}
		for (int i = 0; i < width(); ++i) {
			for (int j = 0; j < height(); ++j) {
				if (board[i][j] == 12) {
					board[i][j] = -1;
				}
			}
		}
	}

	private void redistribute() {
		doBasicRedistribute(redistributableMineCount());
	}

	private boolean attemptLinearProgress() {
		boolean ret = false;
		while (true) {
			if (!doLinearPass()) {
				break;
			} else {
				ret = true;
			}
		}
		return false;
	}

	private boolean attemptSafeLinearProgress() {
		boolean ret = false;
		while (true) {
			if (!doSafeLinearPass()) {
				break;
			} else {
				ret = true;
			}
		}
		return false;
	}

	private boolean doLinearPass() {
		boolean ret = false;
		for (int i = 0; i < width(); ++i) {
			for (int j = 0; j < height(); ++j) {
				if (board[i][j] == -1) {
					if (!canBeMine(i, j)) {
						board[i][j] = realBoard[i][j];
						ret = true;
					} else {
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

	private boolean doSafeLinearPass() {
		boolean ret = false;
		for (int i = 0; i < width(); ++i) {
			for (int j = 0; j < height(); ++j) {
				if (board[i][j] == -1) {
					if (!canBeMine(i, j)) {
						board[i][j] = -2;
						ret = true;
					} else {
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

	private boolean canFinish() {
		if (getRemainingMines() == 0) {
			return true;
		}
		int remainingOpen = 0;
		for (int i = 0; i < width(); ++i) {
			for (int j = 0; j < height(); ++j) {
				if (board[i][j] == -1) {
					++remainingOpen;
				}
			}
		}
		return remainingOpen == 0 || remainingOpen == getRemainingMines();
	}

	private boolean boardHasInconsistencies() {
		int remainingMines = getRemainingMines();
		for (int i = 0; i < width(); ++i) {
			for (int j = 0; j < height(); ++j) {
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

	private boolean attemptDeepProgress() {
		int[][] temp = copyBoard();
		for (int i = 0; i < width(); ++i) {
			for (int j = 0; j < height(); ++j) {
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
				}
			}
		}
		return false;
	}

	private boolean layoutIsPossible() {
		attemptSafeLinearProgress();
		if (boardHasInconsistencies()) {
			return false;
		}
		if (getRemainingMines() == 0) {
			return true;
		}
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
		for (int i = 0; i < width(); ++i) {
			for (int j = 0; j < height(); ++j) {
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



	private int getNoInfoCount() {
		int ret = 0;
		for (int i = 0; i < width(); ++i) {
			for (int j = 0; j < height(); ++j) {
				if (getBoardState(i, j) == 0) {
					++ret;
				}
			}
		}
		return ret;
	}

	private boolean allNumbersAreSatisfied() {
		for (int i = 0; i < width(); ++i) {
			for (int j = 0; j < height(); ++j) {
				if (board[i][j] >= 0 && board[i][j] < 9) {
					if (remainingMineNeighbors(i, j) != 0) {
						return false;
					}
				}
			}
		}
		return true;
	}

	private boolean allEdgesGuessed() {
		for (int i = 0; i < width(); ++i) {
			for (int j = 0; j < height(); ++j) {
				int boardState = getBoardState(i, j);
				if (boardState == 1 && board[i][j] > -2 && board[i][j] < 10) {
					return false;
				}
			}
		}
		return true;
	}

	private int getBoardState(int x, int y) {
		if (board[x][y] >= 0 && board[x][y] <= 9) {
			return 2;
		}
		for (int i = -1; i <= 1; ++i) {
			for (int j = -1; j <= 1; ++j) {
				if (x + i >= 0 && x + i < width()) {
					if (y + j >= 0 && y + j < height()) {
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

	private boolean canBeMine(int x, int y) {
		for (int i = -1; i <= 1; ++i) {
			for (int j = -1; j <= 1; ++j) {
				if (x + i >= 0 && x + i < width()) {
					if (y + j >= 0 && y + j < height()) {
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

	private boolean mustBeMine(int x, int y) {
		for (int i = -1; i <= 1; ++i) {
			for (int j = -1; j <= 1; ++j) {
				if (x + i >= 0 && x + i < width()) {
					if (y + j >= 0 && y + j < height()) {
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

	private boolean deepCanBeNotMine(int x, int y, int[][] temp) {
		boolean ret = !mustBeMine(x, y);
		if (ret) {
			board[x][y] = -2;
			ret = layoutIsPossible();
			board = copyBoard(temp);
		}
		return ret;
	}

	private int remainingMineSpaces(int x, int y) {
		int ret = 8;
		for (int i = -1; i <= 1; ++i) {
			for (int j = -1; j <= 1; ++j) {
				if (x + i >= 0 && x + i < width()) {
					if (y + j >= 0 && y + j < height()) {
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

	private int remainingMineNeighbors(int x, int y) {
		int ret = board[x][y];
		for (int i = -1; i <= 1; ++i) {
			for (int j = -1; j <= 1; ++j) {
				if (x + i >= 0 && x + i < width()) {
					if (y + j >= 0 && y + j < height()) {
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

	private int getRemainingMines() {
		int ret = mineCount;
		for (int i = 0; i < width(); ++i) {
			for (int j = 0; j < height(); ++j) {
				if (board[i][j] >= 9) {
					--ret;
				}
			}
		}
		return ret;
	}

	private boolean boardersUnknown(int x, int y) {
		for (int di = -1; di <= 1; ++di) {
			for (int dj = -1; dj <= 1; ++dj) {
				if (x + di >= 0 && y + dj >= 0 && x + di < width() && y + dj < height()) {
					if (board[x + di][y + dj] == -1) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private boolean boardersNumber(int x, int y, int number) {
		for (int di = -1; di <= 1; ++di) {
			for (int dj = -1; dj <= 1; ++dj) {
				if (x + di >= 0 && y + dj >= 0 && x + di < width() && y + dj < height()) {
					if (board[x + di][y + dj] == number) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private int getKnownCount() {
		int ret = 0;
		for (int i = 0; i < width(); ++i) {
			for (int j = 0; j < height(); ++j) {
				if (board[i][j] != -1) {
					++ret;
				}
			}
		}
		return ret;
	}



	private int[][] copyBoard() {
		int[][] ret =  new int[width()][height()];
		for (int i = 0; i < width(); ++i) {
			for (int j = 0; j < height(); ++j) {
				ret[i][j] = board[i][j];
			}
		}
		return ret;
	}

	private void getNewMines() {
		for (int i = 0; i < width(); ++i) {
			for (int j = 0; j < height(); ++j) {
				field[i][j] = false;
			}
		}
		Random random = new Random();
		for (int i = 0; i < mineCount; ++i) {
			int index = random.nextInt(width() * height());
			if (!field[index / height()][index % height()] && (Math.abs(index / height() - startX) > 1 || Math.abs(index % height() - startY) > 1)) {
				field[index / height()][index % height()] = true;
			} else {
				--i;
			}
		}
	}

	private void updateRealBoard() {
		for (int i = 0; i < width(); ++i) {
			for (int j = 0; j < height(); ++j) {
				if (field[i][j]) {
					realBoard[i][j] = 9;
				} else {
					realBoard[i][j] = realNeighboringMines(i, j);
				}
			}
		}
	}

	private void resetGuessBoard() {
		for (int i = 0; i < width(); ++i) {
			for (int j = 0; j < height(); ++j) {
				board[i][j] = -1;
			}
		}
		board[startX][startY] = realBoard[startX][startY];
	}

	private int realNeighboringMines(int x, int y) {
		int ret = 0;
		for (int i = -1; i <= 1; ++i) {
			for (int j = -1; j <= 1; ++j) {
				if ((j != i || j != 0) &&
						x + i >= 0 &&
						y + j >= 0 &&
						x + i < width() &&
						y + j < height()) {
					if (field[x + i][y + j]) {
						++ret;
					}
				}
			}
		}
		return ret;
	}

	private int width() {
		return field.length;
	}

	private int height() {
		return field[0].length;
	}



	public float getProgress() {
		return progress;
	}

	public boolean fieldIsCompleted() {
		return completed;
	}

	public boolean[][] getCompletedField() {
		if (completed) {
			return field;
		} else {
			return null;
		}
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

	public int getFirstPress() {
		return startX * height() + startY;
	}

	public boolean isPrepared() {
		return prepared;
	}

	public void reset() {
		field = null;
	}
}

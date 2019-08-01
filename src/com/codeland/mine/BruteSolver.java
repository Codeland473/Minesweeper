package com.codeland.mine;

public class BruteSolver {
	private int[][] board;
	private int[][] realBoard;
	private int mineCount;

	public BruteSolver(int[][] fullBoard, int startX, int startY) {
		board = new int[fullBoard.length][fullBoard[0].length];
		realBoard = fullBoard;
		for (int i = 0; i < fullBoard.length; ++i) {
			for (int j = 0; j < fullBoard[0].length; ++j) {
				if (fullBoard[i][j] == 9) {
					++mineCount;
				}
				board[i][j] = -1;
			}
		}
		board[startX][startY] = fullBoard[startX][startY];
	}

	public boolean isSolvable() {
		while (true) {
			while (true) {
				if (!flagMustBeMines() && !openCantBeMines()) {
					break;
				}
				/*if (boardHasInconsistencies()) {
					System.out.println("this is not supposed to happen but its happening anyway, will look into later. - C1FR1");
				}*/
			}
			if (canFinish()) {
				return true;
			}
			if (!deepIsSolvable()) {
				return false;
			}
		}
	}

	private boolean deepIsSolvable() {
		int[][] temp = copyBoard();
		for (int i = 0; i < board.length; ++i) {
			for (int j = 0; j < board[0].length; ++j) {
				if (board[i][j] == -1) {
					board[i][j] = 9;
					boolean canBeMine = softSolvable();
					board = temp;
					temp = copyBoard();
					board[i][j] = -2;
					boolean canBeNotMine = softSolvable();
					board = temp;
					if (canBeMine != canBeNotMine) {
						if (canBeMine) {
							board[i][j] = 9;
						} else {
							board[i][j] = 0;
						}
						return true;
					}
				}
			}
		}
		return false;
	}

	private boolean softSolvable() {
		boolean ret = false;
		while (true) {
			while (true) {
				if (!flagMustBeMines() && !markCantBeMines()) {
					break;
				} else {
					ret = true;
				}
			}
			if (boardHasInconsistencies()) {
				return false;
			}
			if (!deepIsSolvable()) {
				break;
			} else {
				ret = true;
			}
			if (canFinish()) {
				ret = true;
			}
		}
		if (boardHasInconsistencies()) {
			return false;
		}
		return ret;
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
			for (int j = 0; j < board.length; ++j) {
				int val = board[i][j];
				if (val >= 0 && val <= 8) {
					int remainingMineNeighbors = remainingMineNeighbors(i, j);
					if (remainingMineSpaces(i, j) < 0 || remainingMineNeighbors < remainingMineSpaces(i, j) || remainingMines < remainingMineNeighbors) {
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
							if (board[x + i][y + j] == 9) {
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
					}
				}
			}
		}
		return ret;
	}

	private boolean canFinish() {
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
				if (board[i][j] == 9) {
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
}

package com.codeland.mine;

public class SolvableBoardGenerator implements Runnable {
	public float progress = 0;
	private boolean completed = false;
	private boolean[][] field;

	private int startX;
	private int startY;
	private int mineCount;

	public void prepare(int width, int height, int mines, int startingX, int startingY) {
		field = new boolean[width][height];
		startX = startingX;
		startY = startingY;
		mineCount = mines;
	}

	@Override
	public void run() {

		completed = true;
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
}

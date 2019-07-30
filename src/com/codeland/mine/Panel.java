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

import com.gnarly.engine.display.Camera;
import com.gnarly.engine.display.Window;
import com.gnarly.engine.model.TexRect;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_1;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_2;

public class Panel {

	private static final int
		STATE_UNPRESSED = 0x00,
		STATE_FLAGGED   = 0x01,
		STATE_DEPRESSED = 0x02,
		STATE_PRESSED   = 0x03;

	private static TexRect unpressed = null;
	private static TexRect flagged   = null;
	private static TexRect depressed = null;
	private static TexRect[] nums    = null;

	private Window window;
	private Camera camera;

	private int[][] states;
	private int[][] field;

	private float minDim;

	private float pressX;
	private float pressY;

	public Panel(Window window, Camera camera) {
		this.window = window;
		this.camera = camera;

		pressX = -1;
		pressY = -1;

		field = FieldLoader.loadField();
		states = new int[field.length][field[0].length];
		for (int i = 0; i < states.length; ++i)
			for (int j = 0; j < states[0].length; ++j)
				states[i][j] = STATE_UNPRESSED;

		if (unpressed == null) {
			minDim = Math.min(
				camera.getWidth()  / field.length,
				camera.getHeight() / field[0].length
			);

			unpressed = new TexRect(camera, "res/img/tiles/unpressed.png", 0, 0, 0, minDim, minDim, 0, false);
			flagged   = new TexRect(camera, "res/img/tiles/flagged.png", 0, 0, 0, minDim, minDim, 0, false);
			depressed = new TexRect(camera, "res/img/tiles/depressed.png", 0, 0, 0, minDim, minDim, 0, false);

			nums = new TexRect[10];
			for (int i = 0; i < nums.length - 1; ++i)
				nums[i] = new TexRect(camera, "res/img/tiles/num-" + i + ".png", 0, 0, 0, minDim, minDim, 0, false);
			nums[9] = new TexRect(camera, "res/img/tiles/mine.png", 0, 0, 0, minDim, minDim, 0, false);
		}
	}

	public void update() {
		if (window.wasResized()) {
			camera.setDims(window.getWidth(), window.getHeight());
			minDim = Math.min(
				camera.getWidth()  / field.length,
				camera.getHeight() / field[0].length
			);
			unpressed.setDims(minDim, minDim);
			flagged.setDims(minDim, minDim);
			depressed.setDims(minDim, minDim);
			for (int i = 0; i < nums.length; ++i)
				nums[i].setDims(minDim, minDim);
			nums[9].setDims(minDim, minDim);
		}
		if (window.mousePressed(GLFW_MOUSE_BUTTON_1) == Window.BUTTON_PRESSED) {
			Vector3f coords = window.getMouseCoords(camera);
			int x = (int) (coords.x / minDim);
			int y = (int) (coords.y / minDim);
			if (x > -1 && x < states.length
			 && y > -1 && y < states[0].length) {
				switch (states[x][y]) {
					case STATE_UNPRESSED:
						pressX = x;
						pressY = y;
						states[x][y] = STATE_DEPRESSED;
						break;
					case STATE_PRESSED:
						pressX = x;
						pressY = y;
						for (int k = -1; k < 2; ++k) {
							for (int k2 = -1; k2 < 2; ++k2) {
								int nx = x + k;
								int ny = y + k2;
								if (nx > -1 && nx < states.length
								 && ny > -1 && ny < states[0].length
								 && states[nx][ny] == STATE_UNPRESSED)
									states[nx][ny] = STATE_DEPRESSED;
							}
						}
						break;
				}
			}
		}
		else if (window.mousePressed(GLFW_MOUSE_BUTTON_1) == Window.BUTTON_RELEASED) {
			Vector3f coords = window.getMouseCoords(camera);
			int x = (int) (coords.x / minDim);
			int y = (int) (coords.y / minDim);
			if (x > -1 && x < states.length
			 && y > -1 && y < states[0].length
			 && pressX == x && pressY == y) {
				switch (states[x][y]) {
					case STATE_DEPRESSED:
						states[x][y] = STATE_PRESSED;
						if (field[x][y] == 0)
							bucketEmpty(x, y, field, states);
						break;
					case STATE_PRESSED:
						int flagged = 0;
						for (int k = -1; k < 2; ++k) {
							for (int k2 = -1; k2 < 2; ++k2) {
								int nx = x + k;
								int ny = y + k2;
								if (nx > -1 && nx < states.length
								 && ny > -1 && ny < states[0].length
								 && states[nx][ny] == STATE_FLAGGED)
									++flagged;
							}
						}
						clearDepressed(
							flagged == field[x][y] ?
								STATE_PRESSED :
								STATE_UNPRESSED
						);
						break;
				}
			}
			else
				clearDepressed(STATE_UNPRESSED);
			pressX = -1;
			pressY = -1;
		}
		else if (window.mousePressed(GLFW_MOUSE_BUTTON_2) == Window.BUTTON_PRESSED) {
			Vector3f coords = window.getMouseCoords(camera);
			int x = (int) (coords.x / minDim);
			int y = (int) (coords.y / minDim);
			if (x > -1 && x < states.length
			 && y > -1 && y < states[0].length) {
				switch (states[x][y]) {
					case STATE_UNPRESSED: states[x][y] = STATE_FLAGGED;   break;
					case STATE_FLAGGED:   states[x][y] = STATE_UNPRESSED; break;
				}
			}
		}
	}

	private void clearDepressed(int state) {
		for (int i = 0; i < states.length; ++i)
			for (int j = 0; j < states[0].length; ++j)
				if (states[i][j] == STATE_DEPRESSED)
					states[i][j] = state;
	}

	private static void bucketEmpty(int x, int y, int[][] field, int[][] states) {
		for (int k = -1; k < 2; ++k) {
			for (int k2 = -1; k2 < 2; ++k2) {
				int nx = x + k;
				int ny = y + k2;
				if (nx > -1 && nx < states.length
				 && ny > -1 && ny < states[0].length
				 && states[nx][ny] == STATE_UNPRESSED) {
					states[nx][ny] = STATE_PRESSED;
					if (field[nx][ny] == 0)
						bucketEmpty(nx, ny, field, states);
				}
			}
		}
	}

	public void render() {
		for (int i = 0; i < states.length; ++i) {
			for (int j = 0; j < states[0].length; ++j) {
				switch (states[i][j]) {
					case STATE_UNPRESSED: renderTile(i, j, unpressed        ); break;
					case STATE_FLAGGED:   renderTile(i, j, flagged          ); break;
					case STATE_DEPRESSED: renderTile(i, j, depressed        ); break;
					case STATE_PRESSED:   renderTile(i, j, nums[field[i][j]]); break;
				}
			}
		}
	}

	private static void renderTile(int x, int y, TexRect rect) {
		float dims = rect.getWidth();
		rect.setPosition(x * dims, y * dims);
		rect.render();
	}
}

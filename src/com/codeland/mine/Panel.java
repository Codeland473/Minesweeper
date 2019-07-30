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

import static com.codeland.mine.Board.*;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_1;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_2;

public class Panel {

	private interface BoardPressAction {
		void execute(int x, int y);
	}

	private static TexRect unpressed = null;
	private static TexRect flagged   = null;
	private static TexRect depressed = null;
	private static TexRect dead      = null;
	private static TexRect[] nums    = null;

	private Window window;
	private Camera camera;

	private Board board;

	private float minDim;

	private float pressX;
	private float pressY;

	public Panel(Window window, Camera camera) {
		this.window = window;
		this.camera = camera;

		board = new Board();

		pressX = -1;
		pressY = -1;

		if (unpressed == null) {
			minDim = Math.min(
				camera.getWidth()  / board.width(),
				camera.getHeight() / board.height()
			);

			unpressed = new TexRect(camera, "res/img/tiles/unpressed.png", 0, 0, 0, minDim, minDim, 0, false);
			flagged   = new TexRect(camera, "res/img/tiles/flagged.png",   0, 0, 0, minDim, minDim, 0, false);
			depressed = new TexRect(camera, "res/img/tiles/depressed.png", 0, 0, 0, minDim, minDim, 0, false);
			dead      = new TexRect(camera, "res/img/tiles/mine-dead.png",      0, 0, 0, minDim, minDim, 0, false);

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
				camera.getWidth()  / board.width(),
				camera.getHeight() / board.height()
			);
			unpressed.setDims(minDim, minDim);
			flagged.setDims(minDim, minDim);
			depressed.setDims(minDim, minDim);
			for (int i = 0; i < nums.length; ++i)
				nums[i].setDims(minDim, minDim);
		}
		if (window.mousePressed(GLFW_MOUSE_BUTTON_1) >= Window.BUTTON_PRESSED)
			checkBoardPress(board::depress);
		else if (window.mousePressed(GLFW_MOUSE_BUTTON_1) == Window.BUTTON_RELEASED)
			checkBoardPress(board::release);
		else if (window.mousePressed(GLFW_MOUSE_BUTTON_2) == Window.BUTTON_PRESSED)
			checkBoardPress(board::flag);
	}

	public void render() {
		for (int i = 0; i < board.width(); ++i) {
			for (int j = 0; j < board.height(); ++j) {
				switch (board.getState(i, j)) {
					case STATE_UNPRESSED: renderTile(i, j, unpressed                 ); break;
					case STATE_FLAGGED:   renderTile(i, j, flagged                   ); break;
					case STATE_DEPRESSED: renderTile(i, j, depressed                 ); break;
					case STATE_DEAD:
					case STATE_PRESSED:   renderTile(i, j, nums[board.getMines(i, j)]); break;
				}
			}
		}
	}

	private void checkBoardPress(BoardPressAction action) {
		Vector3f coords = window.getMouseCoords(camera);
		int x = (int) (coords.x / minDim);
		int y = (int) (coords.y / minDim);
		if (x > -1 && x < board.width()
		 && y > -1 && y < board.height())
			action.execute(x, y);
	}

	private static void renderTile(int x, int y, TexRect rect) {
		float dims = rect.getWidth();
		rect.setPosition(x * dims, y * dims);
		rect.render();
	}
}

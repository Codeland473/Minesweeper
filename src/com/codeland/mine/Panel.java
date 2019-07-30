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

	private float tileDim;

	private float pressX;
	private float pressY;

	private float leftOffset;
	private float upOffset;

	public Panel(Window window, Camera camera) {
		this.window = window;
		this.camera = camera;

		board = new Board();

		pressX = -1;
		pressY = -1;

		// If the textures are unloaded load all the textures
		if (unpressed == null) {
			tileDim = Math.min(
				camera.getWidth()  / board.width(),
				camera.getHeight() / board.height()
			);

			unpressed = new TexRect(camera, "res/img/tiles/unpressed.png", 0, 0, 0, tileDim, tileDim, 0, false);
			flagged   = new TexRect(camera, "res/img/tiles/flagged.png",   0, 0, 0, tileDim, tileDim, 0, false);
			depressed = new TexRect(camera, "res/img/tiles/depressed.png", 0, 0, 0, tileDim, tileDim, 0, false);
			dead      = new TexRect(camera, "res/img/tiles/mine-dead.png",      0, 0, 0, tileDim, tileDim, 0, false);

			nums = new TexRect[10];
			for (int i = 0; i < nums.length - 1; ++i)
				nums[i] = new TexRect(camera, "res/img/tiles/num-" + i + ".png", 0, 0, 0, tileDim, tileDim, 0, false);
			nums[9] = new TexRect(camera, "res/img/tiles/mine.png", 0, 0, 0, tileDim, tileDim, 0, false);
		}
	}

	public void update() {
		// If the window was resized...
		if (window.wasResized()) {
			// Reset the camera dimensions to match the window dimensions
			camera.setDims(window.getWidth(), window.getHeight());

			// Get the largest tile dimension that will allow the entire board to fit within the window
			tileDim = Math.min(
				camera.getWidth()  / board.width(),
				camera.getHeight() / board.height()
			);

			// Reset all the tile dimensions
			unpressed.setDims(tileDim, tileDim);
			  flagged.setDims(tileDim, tileDim);
			depressed.setDims(tileDim, tileDim);

			for (int i = 0; i < nums.length; ++i)
				nums[i].setDims(tileDim, tileDim);
		}

		// Calculate the render offsets from the top and bottom of the window
		leftOffset = (window.getWidth()  - (board.width()  * tileDim)) / 2;
		upOffset   = (window.getHeight() - (board.height() * tileDim)) / 2;

		// If the left mouse button is pressed...
		if (window.mousePressed(GLFW_MOUSE_BUTTON_1) >= Window.BUTTON_PRESSED)
			// Trigger a depress action on the square that was clicked
			checkBoardPress(board::depress);
		// If the left mouse button was released this frame...
		else if (window.mousePressed(GLFW_MOUSE_BUTTON_1) == Window.BUTTON_RELEASED)
			// Trigger a release action on the square that was clicked
			checkBoardPress(board::release);
		// If the right mouse button was pressed this frame...
		else if (window.mousePressed(GLFW_MOUSE_BUTTON_2) == Window.BUTTON_PRESSED)
			// Trigger a flag action on the square that was clicked
			checkBoardPress(board::flag);
	}

	/**
	 * Renders the board centered in the window
	 */
	public void render() {
		// Render each tile in the board
		board.iterate((x, y, board) -> {
			// Calculate the position of the current tile
			float xPos = leftOffset + x * tileDim;
			float yPos = upOffset   + y * tileDim;
			// Render the tile for that state at the position calculated
			switch (board[x][y].state) {
				case STATE_UNPRESSED: renderTile(xPos, yPos, unpressed              ); break;
				case STATE_FLAGGED:   renderTile(xPos, yPos, flagged                ); break;
				case STATE_DEPRESSED: renderTile(xPos, yPos, depressed              ); break;
				case STATE_DEAD:      renderTile(xPos, yPos, dead                   ); break;
				case STATE_PRESSED:   renderTile(xPos, yPos, nums[board[x][y].mines]); break;
			}
		});
	}

	/**
	 * Checks whether the mouse lies within the bounds of the board.
	 * If it does it executes <code>action</code> and passes it the x and y coordinates that the mouse lies on
	 *
	 * @param action - The action to exectue if the mouse lies within the board
	 */
	private void checkBoardPress(BoardPressAction action) {
		// Get the mouse coordinates in camera space
		Vector3f coords = window.getMouseCoords(camera);
		// Get the position on the board that coordinate falls in
		int x = (int) ((coords.x - leftOffset) / tileDim);
		int y = (int) ((coords.y -   upOffset) / tileDim);
		// If the position is within the bounds of the board execute the action
		if (x > -1 && x < board.width()
		 && y > -1 && y < board.height())
			action.execute(x, y);
	}

	/**
	 * Renders a single tile of the board
	 *
	 * @param left - The left offset of the tile
	 * @param up   - The right offset of the tile
	 * @param rect - The tile to render
	 */
	private static void renderTile(float left, float up, TexRect rect) {
		// Set the position of the tile
		rect.setPosition(left, up);
		// Render the tile
		rect.render();
	}
}

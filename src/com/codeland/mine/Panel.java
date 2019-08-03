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
//import static com.codeland.mine.Button.BUTTON_STATE_RELEASED;
import static com.codeland.mine.ResetButton.*;
import static com.codeland.mine.ToggleButton.*;
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
	private static TexRect win       = null;
	private static TexRect[] backs   = null;
	private static TexRect[] nums    = null;

	private Window window;
	private Camera camera;

	private Board board;

	private float tileDim;

	private float leftOffset;
	private float upOffset;

	private BorderRect inner;
	private BorderRect outer;

	private BorderRect topBar;
	private ResetButton resetButton;
	private ToggleButton equalHighlighting;
	private ToggleButton exceedHighlighting;
	private ToggleButton zeroBoxHighlighting;

	private int highlightEqual;
	private int highlightExceed;
	private int highlightZero;

	private int status;

	public Panel(Window window, Camera camera) {
		this.window = window;
		this.camera = camera;

		board = new Board();
		board.reset(30, 16, 170);

		status = STATUS_PLAYING;

		tileDim = Math.min(
			camera.getWidth()  / (board.width()  + 2),
			camera.getHeight() / (board.height() + 6)
		);

		// If the textures are unloaded load all the textures
		if (unpressed == null) {
			unpressed = new TexRect(camera, "res/img/tiles/unpressed.png", 0, 0, 0, tileDim, tileDim, 0, false);
			flagged   = new TexRect(camera, "res/img/tiles/flagged.png",   0, 0, 0, tileDim, tileDim, 0, false);
			depressed = new TexRect(camera, "res/img/tiles/depressed.png", 0, 0, 0, tileDim, tileDim, 0, false);
			dead      = new TexRect(camera, "res/img/tiles/mine-dead.png", 0, 0, 0, tileDim, tileDim, 0, false);
			win       = new TexRect(camera, "res/img/tiles/mine-win.png",  0, 0, 0, tileDim, tileDim, 0, false);

			backs = new TexRect[3];
			backs[0] = new TexRect(camera, "res/img/tiles/num-background-plain.png",  0, 0, -0.1f, tileDim, tileDim, 0, false);
			backs[1] = new TexRect(camera, "res/img/tiles/num-background-equal.png",  0, 0, -0.1f, tileDim, tileDim, 0, false);
			backs[2] = new TexRect(camera, "res/img/tiles/num-background-exceed.png", 0, 0, -0.1f, tileDim, tileDim, 0, false);

			nums = new TexRect[10];
			for (int i = 0; i < nums.length - 1; ++i)
				nums[i] = new TexRect(camera, "res/img/tiles/num-" + i + ".png", 0, 0, 0, tileDim, tileDim, 0, false);
			nums[9] = new TexRect(camera, "res/img/tiles/mine.png", 0, 0, 0, tileDim, tileDim, 0, false);
		}

		// Calculate the render offsets from the top and bottom of the window
		leftOffset = (window.getWidth()  - (board.width() * tileDim)) / 2;
		upOffset   = (window.getHeight() - ((board.height() - 4) * tileDim)) / 2;

		outer = new BorderRect(camera, 0,           0,           0, camera.getWidth(),           camera.getHeight(),           tileDim / 2,false);
		inner = new BorderRect(camera, leftOffset - tileDim / 2, upOffset - tileDim / 2, 0, board.width() * tileDim + tileDim, board.height() * tileDim + tileDim, tileDim / 2, true);

		topBar = new BorderRect(camera, tileDim / 2, upOffset - tileDim * 4.5f, 0, camera.getWidth() - tileDim, tileDim * 4, tileDim / 2, true);
		resetButton = new ResetButton(window, camera, (camera.getWidth() - tileDim * 3) / 2, upOffset - tileDim * 4, tileDim * 3, tileDim * 3);
		equalHighlighting   = new ToggleButton(window, camera, camera.getWidth() - tileDim * 3.3333333f, upOffset - tileDim * 3.6666666f, tileDim * 2, tileDim, COLOR_GREEN, STATE_ON);
		exceedHighlighting  = new ToggleButton(window, camera, camera.getWidth() - tileDim * 3.3333333f, upOffset - tileDim * 2.3333333f, tileDim * 2, tileDim, COLOR_RED,   STATE_ON);
		zeroBoxHighlighting = new ToggleButton(window, camera, camera.getWidth() - tileDim * 5.6666666f, upOffset - tileDim * 3.6666666f, tileDim * 2, tileDim, COLOR_WHITE, STATE_OFF);
	}

	public void update() {
		// If the window was resized...
		if (window.wasResized()) {
			// Reset the camera dimensions to match the window dimensions
			camera.setDims(window.getWidth(), window.getHeight());

			// Get the largest tile dimension that will allow the entire board to fit within the window
			tileDim = Math.min(
				camera.getWidth() / (board.width() + 2),
				camera.getHeight() / (board.height() + 6)
			);

			// Calculate the render offsets from the top and bottom of the window
			leftOffset = (window.getWidth() - (board.width() * tileDim)) / 2;
			upOffset = (window.getHeight() - ((board.height() - 4) * tileDim)) / 2;

			// Reset all the tile dimensions
			unpressed.setDims(tileDim, tileDim);
			  flagged.setDims(tileDim, tileDim);
			depressed.setDims(tileDim, tileDim);
			     dead.setDims(tileDim, tileDim);
			      win.setDims(tileDim, tileDim);

			for (int i = 0; i < backs.length; ++i)
				backs[i].setDims(tileDim, tileDim);

			for (int i = 0; i < nums.length; ++i)
				nums[i].setDims(tileDim, tileDim);

			outer.set(0, 0, camera.getWidth(), camera.getHeight(), tileDim / 2);
			inner.set(leftOffset - tileDim / 2, upOffset - tileDim / 2, board.width() * tileDim + tileDim, board.height() * tileDim + tileDim, tileDim / 2);

			topBar.set(tileDim / 2, upOffset - tileDim * 4.5f, camera.getWidth() - tileDim, tileDim * 4, tileDim / 2);
			resetButton.set( (camera.getWidth() - tileDim * 3) / 2, upOffset - tileDim * 4, tileDim * 3, tileDim * 3);
			equalHighlighting.set(  camera.getWidth() - tileDim * 3.3333333f, upOffset - tileDim * 3.6666666f, tileDim * 2, tileDim);
			exceedHighlighting.set( camera.getWidth() - tileDim * 3.3333333f, upOffset - tileDim * 2.3333333f, tileDim * 2, tileDim);
			zeroBoxHighlighting.set(camera.getWidth() - tileDim * 5.6666666f, upOffset - tileDim * 3.6666666f, tileDim * 2, tileDim);
		}

		if (status == STATUS_PLAYING) {
			// If the left mouse button is pressed...
			if (window.mousePressed(GLFW_MOUSE_BUTTON_1) >= Window.BUTTON_PRESSED)
				// Trigger a depress action on the square that was clicked
				checkBoardPress(false, board::depress);
			// If the left mouse button was released this frame...
			else if (window.mousePressed(GLFW_MOUSE_BUTTON_1) == Window.BUTTON_RELEASED)
				// Trigger a release action on the square that was clicked
				checkBoardPress(true, board::release);
			// If the right mouse button was pressed this frame...
			else if (window.mousePressed(GLFW_MOUSE_BUTTON_2) == Window.BUTTON_PRESSED)
				// Trigger a flag action on the square that was clicked
				checkBoardPress(false, board::flag);
		}

		resetButton.update();
		if (resetButton.getState() == BUTTON_STATE_RELEASED) {
			board.reset(30, 16, 50);
			//board.reset("30x16:15#000000000100480002200000011080140040000000000080080000605008900001180818040401004008410800080002040801042410208000250240");
		}
		equalHighlighting.update();
		highlightEqual = equalHighlighting.getState();

		exceedHighlighting.update();
		highlightExceed = exceedHighlighting.getState();

		zeroBoxHighlighting.update();
		highlightZero = zeroBoxHighlighting.getState();

		status = board.checkStatus();

		switch (status) {
			case STATUS_LOSE:
				resetButton.setFace(FACE_STATE_DEAD);
				break;
			case STATUS_WIN:
				resetButton.setFace(FACE_STATE_WIN);
				break;
			case STATUS_PLAYING:
				if (board.depressed())
					resetButton.setFace(FACE_STATE_TENSE);
				else
					resetButton.setFace(FACE_STATE_ALIVE);
				break;
		}
	}

	/**
	 * Renders the board centered in the window
	 */
	public void render() {
		resetButton.render();
		equalHighlighting.render();
		exceedHighlighting.render();
		zeroBoxHighlighting.render();
		outer.render();
		inner.render();
		topBar.render();
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
				case STATE_WIN:       renderTile(xPos, yPos, win                    ); break;
				case STATE_PRESSED:
					int index = board[x][y].mines;
					if (index == MINE)
						renderTile(xPos, yPos, nums[index]);
					else if (index == 0) {
						if (highlightZero == 1 && status == STATUS_PLAYING)
							renderTile(xPos, yPos, backs[1]);
						else
							renderTile(xPos, yPos, nums[0]);
					}
					else {
						int flagCount = this.board.flagCount(x, y);
						switch (flagCount) {
							case 2: if (highlightExceed == 0) flagCount = 0; break;
							case 1: if (highlightEqual  == 0) flagCount = 0; break;
						}
						renderTile(xPos, yPos, backs[flagCount]);
						renderTile(xPos, yPos, nums[index]);
					}
					break;
			}
		});
	}

	/**
	 * Checks whether the mouse lies within the bounds of the board.
	 * If it does it executes <code>action</code> and passes it the x and y coordinates that the mouse lies on
	 *
	 * @param action - The action to exectue if the mouse lies within the board
	 */
	private void checkBoardPress(boolean override, BoardPressAction action) {
		// Get the mouse coordinates in camera space
		Vector3f coords = window.getMouseCoords(camera);
		// Get the position on the board that coordinate falls in
		int x = (int) ((coords.x - leftOffset) / tileDim);
		int y = (int) ((coords.y -   upOffset) / tileDim);
		// If the position is within the bounds of the board execute the action
		if ((x > -1 && x < board.width()
		  && y > -1 && y < board.height())
		  || override)
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

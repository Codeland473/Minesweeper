package com.codeland.mine;

import com.gnarly.engine.display.Camera;
import com.gnarly.engine.display.Window;
import com.gnarly.engine.model.TexRect;
import com.gnarly.engine.texture.Texture;
import org.joml.Vector3f;

import static com.gnarly.engine.display.Window.BUTTON_PRESSED;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_1;
import static org.lwjgl.opengl.GL11.GL_LINEAR;

public class ResetButton {

	private static Texture[] faces  = null;
	private static Texture[] button = null;

	public static final int
		BUTTON_STATE_RELEASED  = 0x00,
		BUTTON_STATE_UNPRESSED = 0x01,
		BUTTON_STATE_PRESSED   = 0x02,
		BUTTON_STATE_HELD      = 0x03;

	public static final int
		FACE_STATE_ALIVE = 0x00,
		FACE_STATE_TENSE = 0x01,
		FACE_STATE_DEAD  = 0x02,
		FACE_STATE_WIN   = 0x03;

	private Window window;
	private Camera camera;

	private int state;
	private int faceState;

	private TexRect faceRect;
	private TexRect buttonRect;

	public ResetButton(Window window, Camera camera, float x, float y, float width, float height) {
		if (faces == null) {
			faces = new Texture[4];
			faces[0] = new Texture("res/img/top-bar/reset-button/alive-face.png", GL_LINEAR);
			faces[1] = new Texture("res/img/top-bar/reset-button/tense-face.png", GL_LINEAR);
			faces[2] = new Texture("res/img/top-bar/reset-button/dead-face.png" , GL_LINEAR);
			faces[3] = new Texture("res/img/top-bar/reset-button/win-face.png"  , GL_LINEAR);

			button = new Texture[2];
			button[0] = new Texture("res/img/top-bar/reset-button/unpressed.png");
			button[1] = new Texture("res/img/top-bar/reset-button/pressed.png"  );
		}

		this.window = window;
		this.camera = camera;

		faceState = FACE_STATE_ALIVE;
		state = 	BUTTON_STATE_UNPRESSED;

		faceRect   = new TexRect(camera, (Texture) null, x + width / 6, y + height / 6, 0, width * 2 / 3, height * 2 / 3, 0, false);
		buttonRect = new TexRect(camera, (Texture) null, x, y, -0.1f, width, height, 0, false);
	}

	public void update() {
		Vector3f coords = window.getMouseCoords(camera);
		if (buttonRect.contains(coords)) {
			if (window.mousePressed(GLFW_MOUSE_BUTTON_1) >= BUTTON_PRESSED) {
				switch (state) {
					case BUTTON_STATE_HELD:
					case BUTTON_STATE_PRESSED:
						state = BUTTON_STATE_HELD;
						break;
					default:
						state = BUTTON_STATE_PRESSED;
						break;
				}
			}
			else switch (state) {
				case BUTTON_STATE_RELEASED:
				case 	BUTTON_STATE_UNPRESSED:
					state = 	BUTTON_STATE_UNPRESSED;
					break;
				default:
					state = BUTTON_STATE_RELEASED;
					break;
			}
		}
		else
			state = 	BUTTON_STATE_UNPRESSED;
	}

	public void set(float x, float y, float width, float height) {
		faceRect.set(x + width / 6, y + height / 6, width * 2 / 3, height * 2 / 3);
		buttonRect.set(x, y, width, height);
	}

	public void setFace(int state) {
		this.faceState = state;
	}

	public int getState() {
		return state;
	}

	public void render() {
		buttonRect.setTexture(button[state >= BUTTON_STATE_PRESSED ? 1 : 0]);
		faceRect.setTexture(faces[faceState]);

		faceRect.render();
		buttonRect.render();
	}
}

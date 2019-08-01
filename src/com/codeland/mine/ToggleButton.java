package com.codeland.mine;

import com.gnarly.engine.display.Camera;
import com.gnarly.engine.display.Window;
import com.gnarly.engine.model.TexRect;
import com.gnarly.engine.texture.Texture;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_1;

public class ToggleButton {

	public static final int
		COLOR_RED   = 0x00,
		COLOR_GREEN = 0x01,
		COLOR_BLUE  = 0x02,
		COLOR_PINK  = 0x03,
		COLOR_WHITE = 0x04;

	public static final int
		STATE_OFF = 0x00,
		STATE_ON  = 0x01;

	private static Texture   off = null;
	private static Texture[] on  = null;

	private Window window;
	private Camera camera;

	private int color;

	private TexRect rect;

	private int state;

	public ToggleButton(Window window, Camera camera, float x, float y, float width, float height, int color, int state) {
		if (off == null) {
			off = new Texture("res/img/top-bar/toggle-buttons/toggle-off.png");

			on = new Texture[5];
			on[0] = new Texture("res/img/top-bar/toggle-buttons/toggle-on-red.png"  );
			on[1] = new Texture("res/img/top-bar/toggle-buttons/toggle-on-green.png");
			on[2] = new Texture("res/img/top-bar/toggle-buttons/toggle-on-blue.png" );
			on[3] = new Texture("res/img/top-bar/toggle-buttons/toggle-on-pink.png" );
			on[4] = new Texture("res/img/top-bar/toggle-buttons/toggle-on-white.png");
		}

		this.window = window;
		this.camera = camera;
		this.color  = color;
		this.state  = state;

		rect = new TexRect(camera, (Texture) null, x, y, 0, width, height, 0, false);
	}

	public void update() {
		if (window.mousePressed(GLFW_MOUSE_BUTTON_1) == Window.BUTTON_PRESSED
		 && rect.contains(window.getMouseCoords(camera)))
			state ^= 1;
	}

	public void render() {
		switch (state) {
			case STATE_OFF: rect.setTexture(off);       break;
			case STATE_ON:  rect.setTexture(on[color]); break;
		}
		rect.render();
	}

	public void set(float x, float y, float width, float height) {
		rect.set(x, y, width, height);
	}

	public int getState() {
		return state;
	}
}

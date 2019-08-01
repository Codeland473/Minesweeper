/*******************************************************************************
 *
 * Copyright (c) 2019 Gnarly Narwhal
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

package com.gnarly.engine.display;

import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR;
import static org.lwjgl.glfw.GLFW.GLFW_DECORATED;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LAST;
import static org.lwjgl.glfw.GLFW.GLFW_MAXIMIZED;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LAST;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_CORE_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_FORWARD_COMPAT;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.GLFW_REPEAT;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_SAMPLES;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwGetCursorPos;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetMouseButtonCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.glfw.GLFW.glfwSetWindowSizeCallback;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL.createCapabilities;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TRUE;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL13.GL_MULTISAMPLE;

import org.joml.Vector3f;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;

public class Window {

	public static final int
		BUTTON_RELEASED  = 0,
		BUTTON_UNPRESSED = 1,
		BUTTON_PRESSED   = 2,
		BUTTON_HELD      = 3,
		BUTTON_REPEAT    = 4;
	
	public static float SCALE;
	
	private long window;
	private int width, height;
	private boolean resized;
	
	private int[] mouseButtons = new int[GLFW_MOUSE_BUTTON_LAST + 1];
	private int[] keys = new int[GLFW_KEY_LAST + 1];
	
	public Window(String title, boolean vSync) {
		init(0, 0, title, vSync, false, false, false);
	}
	
	public Window(String title, boolean vSync, boolean resizable, boolean decorated) {
		init(800, 500, title, vSync, resizable, decorated, true);
	}
	
	public Window(int width, int height, String title, boolean vSync, boolean resizable, boolean decorated) {
		init(width, height, title, vSync, resizable, decorated, false);
	}
	
	public void init(int lwidth, int lheight, String title, boolean vSync, boolean resizable, boolean decorated, boolean maximized) {
		glfwSetErrorCallback(GLFWErrorCallback.createPrint(System.err));
		
		for (int i = 0; i < mouseButtons.length; i++)
			mouseButtons[i] = 0;
		
		if(!glfwInit()) {
			System.err.println("GLFW failed to initialize!");
			System.exit(-1);
		}
		
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
		glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
		glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);

		glfwWindowHint(GLFW_RESIZABLE, resizable ? GLFW_TRUE : GLFW_FALSE);
		glfwWindowHint(GLFW_DECORATED, decorated ? GLFW_TRUE : GLFW_FALSE);
		glfwWindowHint(GLFW_MAXIMIZED, maximized ? GLFW_TRUE : GLFW_FALSE);
		
		GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
		if(lwidth == 0 || lheight == 0) {
			width = vidMode.width();
			height = vidMode.height();
			window = glfwCreateWindow(width, height, title, glfwGetPrimaryMonitor(), 0);
		}
		else {
			this.width = lwidth;
			this.height = lheight;
			window = glfwCreateWindow(width, height, title, 0, 0);
		}
		
		glfwMakeContextCurrent(window);
		createCapabilities();
		 
		glfwSwapInterval(vSync ? 1 : 0);
		
		glfwSetWindowSizeCallback(window, (long window, int w, int h) -> {
			width = w;
			height = h;
			resized = true;
			glViewport(0, 0, width, height);
		});
		
		glfwSetMouseButtonCallback(window, (long window, int button, int action, int mods) -> {
			if (action == GLFW_RELEASE)
				mouseButtons[button] = BUTTON_RELEASED;
			if (action == GLFW_PRESS)
				mouseButtons[button] = BUTTON_PRESSED;
			if (action == GLFW_REPEAT)
				mouseButtons[button] = BUTTON_REPEAT;
		});
		
		glfwSetKeyCallback(window, (long window, int key, int scancode, int action, int mods) -> {
			if (key != -1) {
				if (action == GLFW_RELEASE)
					keys[key] = BUTTON_RELEASED;
				if (action == GLFW_PRESS)
					keys[key] = BUTTON_PRESSED;
				if (action == GLFW_REPEAT)
					keys[key] = BUTTON_REPEAT;
			}
		});

		glEnable(GL_TEXTURE_2D);
		
		glEnable(GL_DEPTH_TEST);
		
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		
		int[] awidth = new int[1], aheight = new int[1];
		glfwGetWindowSize(window, awidth, aheight);
		width = awidth[0];
		height = aheight[0];
	}
	
	public void update() {
		for (int i = 0; i < mouseButtons.length; i++)
			if (mouseButtons[i] == BUTTON_RELEASED || mouseButtons[i] == BUTTON_PRESSED)
				++mouseButtons[i];
		for (int i = 0; i < keys.length; i++)
			if (keys[i] == BUTTON_RELEASED || keys[i] == BUTTON_PRESSED)
				++keys[i];
		resized = false;
		glfwPollEvents();
	}
	
	public void setClearColor(float r, float g, float b, float a) {
		glClearColor(r, g, b, a);
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public void clear() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
	}
	
	public void swap() {
		glfwSwapBuffers(window);
	}
	
	public void close() {
		glfwSetWindowShouldClose(window, true);
	}
	
	public static void terminate() {
		glfwTerminate();
	}
	
	public boolean shouldClose() {
		return glfwWindowShouldClose(window);
	}
	
	public int keyPressed(int keyCode) {
		return keys[keyCode];
	}
	
	public Vector3f getMouseCoords(Camera camera) {
		double[] x = new double[1], y = new double[1];
		glfwGetCursorPos(window, x, y);
		Vector3f ret = new Vector3f((float) x[0], (float) y[0], 0);
		return ret.mul(camera.getWidth() / this.width, camera.getHeight() / this.height, 1);
	}
	
	public int mousePressed(int button) {
		return mouseButtons[button];
	}
	
	public boolean wasResized() {
		return resized;
	}
}

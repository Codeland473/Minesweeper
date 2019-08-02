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

import static org.lwjgl.glfw.GLFW.*;
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
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWVidMode;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

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

	public void setIcon(String imagePath) {
		GLFWImage image = makeGLFWImage(imagePath);
		GLFWImage.Buffer buffer = GLFWImage.malloc(1);
		buffer.put(0, image);

		glfwSetWindowIcon(window, buffer);

	}
	public static GLFWImage makeGLFWImage(String imagePath) {
		BufferedImage b = null;
		try {
			b = ImageIO.read(new FileInputStream(imagePath));
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		int bwi = b.getWidth();
		int bhi = b.getHeight();
		int len = bwi * bhi;

		int[] rgbArray = new int[len];

		System.out.println();

		b.getRGB(0, 0, bwi, bhi, rgbArray, 0, bwi);

		ByteBuffer buffer = BufferUtils.createByteBuffer(len * 4);

		for(int i = 0; i < len; ++i) {
			int rgb = rgbArray[i];
			buffer.put((byte)(rgb >> 16 & 0xff));
			buffer.put((byte)(rgb >>  8 & 0xff));
			buffer.put((byte)(rgb       & 0xff));
			buffer.put((byte)(rgb >> 24 & 0xff));
		}

		buffer.flip();

		// create a GLFWImage
		GLFWImage img= GLFWImage.create();
		img.width(bwi);     // setup the images' width
		img.height(bhi);   // setup the images' height
		img.pixels(buffer);   // pass image data

		return img;
	}
}

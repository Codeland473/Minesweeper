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
import com.gnarly.engine.shaders.Shader;

public class Main {

	public static long FPS = 999;
	public static double dtime;

	private Window window;
	private Camera camera;

	private Panel panel;

	public void start() {
		long curTime, pastTime, nspf = 1000000000 / FPS;
		init();
		pastTime = System.nanoTime();
		while(!window.shouldClose()) {
			curTime = System.nanoTime();
			if (curTime - pastTime > nspf) {
				dtime = (curTime - pastTime) / 1000000000d;
				update();
				render();
				pastTime = curTime;
			}
		}
		Window.terminate();
	}

	private void init() {
		// window = new Window("Minesweeper", true);
		window = new Window(512, 512, "Minesweeper", true, true, true);
		window.setIcon("res/img/icon.png");
		window.setClearColor(0.09803921568f, 0.09803921568f, 0.09803921568f, 1);
		camera = new Camera(window.getWidth(), window.getHeight());
		Shader.init();

		panel = new Panel(window, camera);
	}

	private void update() {
		window.update();
		panel.update();
		camera.update();
	}

	private void render() {
		window.clear();
		panel.render();
		window.swap();
	}

	public static void main(String[] args) {
		new Main().start();
	}
}

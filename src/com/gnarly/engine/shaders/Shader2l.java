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

package com.gnarly.engine.shaders;

import static org.lwjgl.opengl.GL20.*;

public class Shader2l extends Shader {

	private int percentLoc;

	protected Shader2l() {
		super("res/shaders/s2l/vert.gls", "res/shaders/s2l/frag.gls");
		getUniforms();
	}
	
	public void setPercent(float percent) {
		glUniform1f(percentLoc, 1 - percent);
	}
	
	@Override
	protected void getUniforms() {
		percentLoc  = glGetUniformLocation(program, "percent");

		int backgroundLoc = glGetUniformLocation(program, "background");
		int foregroundLoc = glGetUniformLocation(program, "foreground");

		enable();
		glUniform1i(backgroundLoc, 0);
		glUniform1i(foregroundLoc, 1);
	}
}
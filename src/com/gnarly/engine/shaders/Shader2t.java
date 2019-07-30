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

import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform4f;

public class Shader2t extends Shader {

	private int alphaLoc;
	private int colorLoc;
	private int amountLoc;
	
	protected Shader2t() {
		super("res/shaders/s2t/vert.gls", "res/shaders/s2t/frag.gls");
		getUniforms();
	}

	public void setMixColor(float r, float g, float b, float a, float amount) {
		glUniform4f(colorLoc, r, g, b, a);
		glUniform1f(amountLoc, amount);
	}
	
	public void setAlpha(float a) {
		glUniform1f(alphaLoc, a);
	}
	
	@Override
	protected void getUniforms() {
		alphaLoc  = glGetUniformLocation(program, "alpha");
		colorLoc  = glGetUniformLocation(program, "iColor");
		amountLoc = glGetUniformLocation(program, "amount");
	}
}
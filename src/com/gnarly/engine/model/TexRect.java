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

package com.gnarly.engine.model;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.gnarly.engine.display.Camera;
import com.gnarly.engine.shaders.Shader;
import com.gnarly.engine.shaders.Shader2t;
import com.gnarly.engine.texture.Texture;

public class TexRect extends Rect {

	private Texture texture;
	private Shader2t shader = Shader.SHADER2T;
	protected float direction = 1;
	private float alpha = 1;
	private float r;
	private float g;
	private float b;
	private float a;
	private float amount = 1;
	
	public TexRect(Camera camera, String path, float x, float y, float z, float width, float height, float rotation, boolean gui) {
		super(camera, x, y, z, width, height, rotation, gui);
		texture = new Texture(path);
	}
	
	public TexRect(Camera camera, Texture texture, float x, float y, float z, float width, float height, float rotation, boolean gui) {
		super(camera, x, y, z, width, height, rotation, gui);
		this.texture = texture;
	}
	
	public void render() {
		texture.bind();
		shader.enable();
		Matrix4f cmat = gui ? camera.getProjection() : camera.getMatrix();
		shader.setMVP(cmat.translate(position.add(width * scale / 2, height * scale / 2, 0, new Vector3f())).rotateZ(rotation).scale(width * scale * direction, height * scale, 1).translate(-0.5f, -0.5f, 0));
		shader.setAlpha(alpha);
		shader.setMixColor(r, g, b, a, amount);
		vao.render();
		shader.disable();
		texture.unbind();
	}
	
	public void setCenter(float x, float y) {
		position.x = x - width / 2;
		position.y = y - height / 2;
	}
	
	public void setTexture(Texture texture) {
		this.texture = texture;
	}
	
	public void setMix(float r, float g, float b, float a, float amount) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
		this.amount = 1 - amount;
	}
	
	public void setAlpha(float alpha) {
		this.alpha = alpha;
	}
	
	public float getAlpha() {
		return alpha;
	}
}

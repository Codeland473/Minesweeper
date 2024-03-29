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

import org.joml.Vector3f;

import com.gnarly.engine.display.Camera;
import com.gnarly.engine.shaders.Shader;
import com.gnarly.engine.shaders.Shader2c;

public class Circle {

	private static Vao vao;
	
	private Camera camera;
	private Shader2c shader;
	private Vector3f position;
	private float radius;
	private float r, g, b, a;
	
	public Circle(Camera camera, float x, float y, float z, float radius) {
		this.camera = camera;
		position = new Vector3f(x, y, z);
		this.radius = radius;
		shader = Shader.SHADER2C;
		r = 1;
		g = 0;
		b = 0;
		a = 1;
		if(vao == null)
			initVao();
	}
	
	private void initVao() {
		final int NUM_POINTS = 30;
		float[] cVertices = new float[NUM_POINTS * 3];
		int[] cIndices = new int[(NUM_POINTS - 2) * 3];
		for (int i = 0; i < cVertices.length; i += 3) {
			double angle = Math.PI * 2 * i / (NUM_POINTS * 3);
			cVertices[i    ] = (float) Math.cos(angle);
			cVertices[i + 1] = (float) Math.sin(angle);
			cVertices[i + 2] = 0;
		}
		for (int i = 0; i < cIndices.length; i += 3) {
			cIndices[i    ] = 0;
			cIndices[i + 1] = i / 3 + 1;
			cIndices[i + 2] = i / 3 + 2;
		}
		vao = new Vao(cVertices, cIndices);
	}
	
	public void render() {
		shader.enable();
		shader.setMVP(camera.getMatrix().translate(position).scale(radius));
		shader.setColor(r, g, b, a);
		vao.render();
	}
	
	public Vector3f getPosition() {
		return position;
	}
	
	public void setX(float x) {
		position.x = x;
	}
	
	public void setY(float y) {
		position.y = y;
	}
	
	public void setZ(float z) {
		position.z = z;
	}
	
	public void setPosition(float x, float y) {
		position.x = x;
		position.y = y;
	}
	
	public void setPosition(float x, float y, float z) {
		position.x = x;
		position.y = y;
		position.z = z;
	}
	
	public void setPosition(Vector3f position) {
		this.position.set(position);
	}
	
	public void translate(float x, float y) {
		position.x += x;
		position.y += y;
	}
	
	public void translate(float x, float y, float z) {
		position.x += x;
		position.y += y;
		position.z += z;
	}
	
	public void translate(Vector3f position) {
		this.position.add(position);
	}
	
	public void setRadius(float radius) {
		this.radius = radius;
	}
	
	public void setDiameter(float diameter) {
		this.radius = diameter / 2;
	}
	
	public void setColor(float r, float g, float b, float a) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
	}
	
	public boolean contains(Vector3f vector) {
		return (position.sub(vector, new Vector3f()).lengthSquared() < radius * radius);
	}
}

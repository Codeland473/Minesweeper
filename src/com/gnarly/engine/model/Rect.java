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

import org.joml.Vector2f;
import org.joml.Vector3f;

import com.gnarly.engine.display.Camera;

public class Rect {

	protected static Vao vao;

	protected Camera camera;
	
	protected float width, height;
	protected Vector3f position;
	protected float rotation, scale;
	protected boolean gui;
	
	protected Rect(Camera camera, float x, float y, float z, float width, float height, float rotation, boolean gui) {
		this.camera = camera;
		this.width = width;
		this.height = height;
		position = new Vector3f(x, y, z);
		scale = 1;
		this.rotation = rotation;
		this.gui = gui;
		if(vao == null) {
			float vertices[] = {
				1, 0, 0, // Top left
				1, 1, 0, // Bottom left
				0, 1, 0, // Bottom right
				0, 0, 0  // Top right
			};
			int indices[] = {
				0, 1, 3,
				1, 2, 3
			};
			float[] texCoords = {
				1, 0,
				1, 1,
				0, 1,
				0, 0
			};
			vao = new Vao(vertices, indices);
			vao.addAttrib(texCoords, 2);
		}
	}	
	
	public float getX() {
		return position.x;
	}
	
	public float getY() {
		return position.y;
	}
	
	public float getWidth() {
		return width;
	}
	
	public float getHeight() {
		return height;
	}
	
	public void setX(float x) { 
		position.x = x;
	}
	
	public void setY(float y) { 
		position.y = y;
	}
	
	public void set(float x, float y, float width, float height) {
		position.x = x;
		position.y = y;
		this.width = width;
		this.height = height;
	}
	
	public void setWidth(float width) {
		this.width = width;
	}
	
	public void setHeight(float height) {
		this.height = height;
	}

	public void setDims(float width, float height) {
		this.width  = width;
		this.height = height;
	}

	public void setPosition(float x, float y) {
		position.set(x, y, position.z);
	}
	
	public void setPosition(float x, float y, float z) {
		position.set(x, y, z);
	}
	
	public void setPosition(Vector3f position) {
		this.position.x = position.x;
		this.position.y = position.y;
		this.position.z = position.z;
	}
	
	public void setPosition(Vector2f position) {
		this.position.x = position.x;
		this.position.y = position.y;
	}
	
	public void translate(Vector3f vector) {
		position.add(vector);
	}
	
	public void translate(float x, float y, float z) {
		position.add(x, y, z);
	}
	
	public void setRotation(float angle) {
		rotation = angle;
	}
	
	public void rotate(float angle) {
		rotation += angle;
	}
	
	public void setScale(float scale) {
		this.scale = scale;
	}
	
	public void setAngle(float angle) {
		this.rotation = angle;
	}
}

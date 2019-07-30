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

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {
	
	private Matrix4f projection, projView;
	
	private float width, height;
	private Vector3f position;
	private float rotation;
	
	public Camera(float width, float height) {
		setDims(width, height);
		position = new Vector3f();
		rotation = 0;
		projView = new Matrix4f();
	}
	
	public void setDims(float width, float height) {
		this.width = width;
		this.height = height;
		projection = new Matrix4f().setOrtho(0, width, height, 0, 0, 1);
	}
	
	public void update() {
		projection.translate(position.negate(new Vector3f()), projView).rotateZ(-rotation);
	}
	
	public Matrix4f getProjection() {
		return new Matrix4f(projection);
	}
	
	public Matrix4f getMatrix() {
		return new Matrix4f(projView);
	}
	
	public float getX() {
		return position.x;
	}
	
	public float getY() {
		return position.y;
	}
	
	public Vector3f getPosition() {
		return new Vector3f(position);
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
	
	public void setPosition(float x, float y) {
		position.set(x, y, position.z);
	}
	
	public void setPosition(Vector3f position) {
		this.position.x = position.x;
		this.position.y = position.y;
	}
	
	public void setCenter(float x, float y) {
		position.set(x - width / 2, y - height / 2, position.z);
	}
	
	public void setCenter(Vector3f position) {
		this.position.x = position.x - width / 2;
		this.position.y = position.y - height / 2;
	}
	
	public void translate(float x, float y, float z) {
		position.add(x, y, z);
	}
	
	public void translate(Vector3f transform) {
		position.add(transform);
	}
	
	public void setRotation(float angle) {
		rotation = angle;
	}
	
	public void rotate(float angle) {
		rotation += angle;
	}
}

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

package com.gnarly.engine.texture;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;

public class Texture {

	protected int id, width, height;
	
	public Texture(String name) {
		BufferedImage bi = null;
		try {
			bi = ImageIO.read(new File(name));
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (bi != null) {
			width = bi.getWidth();
			height = bi.getHeight();
			int[] pixels = bi.getRGB(0, 0, width, height, null, 0, width);
			ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);
			
			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {
					int pixel = pixels[i * width + j];
					buffer.put((byte)((pixel >> 16) & 0xFF)); // Red
					buffer.put((byte)((pixel >>  8) & 0xFF)); // Green
					buffer.put((byte)((pixel      ) & 0xFF)); // Blue
					buffer.put((byte)((pixel >> 24) & 0xFF)); // Alpha
				}
			}
			buffer.flip();
			
			id = glGenTextures();
			bind();
			glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
			glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
			glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
			glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
			glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
			unbind();
		}
	}

	public Texture(String name, int filterMode) {
		BufferedImage bi = null;
		try {
			bi = ImageIO.read(new File(name));
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (bi != null) {
			width = bi.getWidth();
			height = bi.getHeight();
			int[] pixels = bi.getRGB(0, 0, width, height, null, 0, width);
			ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);

			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {
					int pixel = pixels[i * width + j];
					buffer.put((byte)((pixel >> 16) & 0xFF)); // Red
					buffer.put((byte)((pixel >>  8) & 0xFF)); // Green
					buffer.put((byte)((pixel      ) & 0xFF)); // Blue
					buffer.put((byte)((pixel >> 24) & 0xFF)); // Alpha
				}
			}
			buffer.flip();

			id = glGenTextures();
			bind();
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, filterMode);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, filterMode);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
			glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
			unbind();
		}
	}
	
	public Texture(int id, int width, int height) {
		this.id = id;
		this.width = width;
		this.height = height;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public void bind() {
		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, id);
	}

	public void bind(int activeTexture) {
		glActiveTexture(GL_TEXTURE0 + activeTexture);
		glBindTexture(GL_TEXTURE_2D, id);
	}
	
	public void unbind() {
		glBindTexture(GL_TEXTURE_2D, 0);
	}
	
	public void destroy() {
		glDeleteTextures(id);
	}
}

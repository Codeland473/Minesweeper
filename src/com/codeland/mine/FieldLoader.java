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

public class FieldLoader {

	public static int[][] loadField() {
		int[][] ret = new int[16][16];
		for (int i = 0; i < ret.length; ++i)
			for (int j = 0; j < ret[0].length; ++j)
				if (Math.random() > 0.95)
					ret[i][j] = 9;
		return calculateNeighbors(ret);
	}

	public static int[][] calculateNeighbors(int[][] field) {
		for (int i = 0; i < field.length; ++i) {
			for (int j = 0; j < field[0].length; ++j) {
				if (field[i][j] == 9) {
					for (int k = -1; k < 2; ++k) {
						for (int k2 = -1; k2 < 2; ++k2) {
							int x = i + k;
							int y = j + k2;
							if (x > -1 && x < field.length
							 && y > -1 && y < field[0].length
							 && field[x][y] != 9)
								++field[x][y];
						}
					}
				}
			}
		}
		return field;
	}
}

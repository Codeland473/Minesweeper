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

package com.gnarly.engine.properties;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import com.gnarly.engine.properties.Properties.*;

public class PropertyReader {
	
	private static int lineNum;
	private static String path;
	
	public static Properties readProperties(String path) {
		Properties props = null;
		try {
			File file = new File(path);
			Scanner scanner = new Scanner(file);
			PropertyReader.path = path;
			lineNum = 0;
			props = readBlock(file.getName(), scanner).data;
			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return props;
	}
	
	private static BlockNode readBlock(String name, Scanner scanner) {
		BlockNode props = new BlockNode();
		props.key = name;
		props.data = new Properties(name);
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			line = line.replaceAll("\\s", "");
			if(line.equals("}"))
				break;
			else if(line.length() < 2 || !line.substring(0, 2).equals("//")){
				String[] pair = line.split(":");
				if (pair.length != 2)
					throw new ImproperFormattingException("Formatting exception on line " + line + " in file '" + path + "!");
				pair[1] = pair[1].replaceAll("\\s", "");
				if (pair[1].equals("{"))
					props.data.add(readBlock(pair[0], scanner));
				else if (pair[1].matches("(\\d+|0x[\\da-f]+)")) {
					IntNode node = new IntNode();
					node.key  = pair[0];
					node.data = Integer.decode(pair[1]);
					props.data.add(node);
				}
				else if (pair[1].matches("(\\d+|0x[\\d0-9]+)(,(\\d+|0x[\\d0-9]+))+")) {
					String[] data = pair[1].split(",");
					int[] ints = new int[data.length];
					for (int i = 0; i < ints.length; ++i)
						ints[i] = Integer.decode(data[i]);
					IntArrayNode node = new IntArrayNode();
					node.key  = pair[0];
					node.data = ints;
					props.data.add(node);
						
				}
				else if (pair[1].matches("\\d+\\.\\d+")) {
					DoubleNode node = new DoubleNode();
					node.key  = pair[0];
					node.data = Double.parseDouble(pair[1]);
					props.data.add(node);
				}
				else if (pair[1].matches("\\d+\\.\\d+(,\\d+\\.\\d+)+")) {
					String[] data = pair[1].split(",");
					double[] doubles = new double[data.length];
					for (int i = 0; i < doubles.length; ++i)
						doubles[i] = Double.parseDouble(data[i]);
					DoubleArrayNode node = new DoubleArrayNode();
					node.key  = pair[0];
					node.data = doubles;
					props.data.add(node);
						
				}
				else {
					StringNode node = new StringNode();
					node.key  = pair[0];
					node.data = pair[1];
					props.data.add(node);
				}
			}
			++lineNum;
		}
		return props;
	}
}

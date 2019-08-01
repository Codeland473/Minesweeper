package com.codeland.mine;

import org.joml.Vector2i;

public class PositionStack {

	private int index;
	private int[] stack;

	public PositionStack(int capacity) {
		index = 0;
		stack = new int[capacity];
	}

	public void push(int position) {
		ensureCapacity();
		stack[index] = position;
		++index;
	}

	public int pop() {
		return stack[--index];
	}

	public boolean isEmpty() {
		return index == 0;
	}

	private void ensureCapacity() {
		if (index == stack.length) {
			int[] newStack = new int[stack.length * 2];
			for (int i = 0; i < stack.length; ++i)
				newStack[i] = stack[i];
			stack = newStack;
		}
	}
}

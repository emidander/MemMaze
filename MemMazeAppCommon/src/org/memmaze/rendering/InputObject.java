package org.memmaze.rendering;


public class InputObject {
	
	public final int action;
	public final float x;
	public final float y;

	public InputObject(int action, float x, float y) {
		this.action = action;
		this.x = x;
		this.y = y;
	}

	@Override
	public String toString() {
		return String.format("Action = %d @ %f,%f", action, x, y);
	}
	
}
package com.codeland.mine;

import com.gnarly.engine.display.Camera;
import com.gnarly.engine.model.TexRect;
import org.joml.Vector2f;

public class BorderRect {

	private static TexRect[][] borders  = null;
	private static TexRect[][] inverted = null;

	private Vector2f[][] positions;

	private boolean invert;

	public BorderRect(Camera camera, float x, float y, float z, float width, float height, float thickness, boolean invert) {
		if (borders == null) {
			borders = new TexRect[3][3];
			borders[0][0] = new TexRect(camera, "res/img/border/top-left.png",     0, 0, 0, 1, 1, 0, false);
			borders[1][0] = new TexRect(camera, "res/img/border/top.png",          0, 0, 0, 1, 1, 0, false);
			borders[2][0] = new TexRect(camera, "res/img/border/top-right.png",    0, 0, 0, 1, 1, 0, false);
			borders[0][1] = new TexRect(camera, "res/img/border/left.png",         0, 0, 0, 1, 1, 0, false);
			borders[1][1] = null;
			borders[2][1] = new TexRect(camera, "res/img/border/right.png",        0, 0, 0, 1, 1, 0, false);
			borders[0][2] = new TexRect(camera, "res/img/border/bottom-left.png",  0, 0, 0, 1, 1, 0, false);
			borders[1][2] = new TexRect(camera, "res/img/border/bottom.png",       0, 0, 0, 1, 1, 0, false);
			borders[2][2] = new TexRect(camera, "res/img/border/bottom-right.png", 0, 0, 0, 1, 1, 0, false);

			inverted = new TexRect[3][3];
			inverted[0][0] = new TexRect(camera, "res/img/border/top-left-inverted.png",     0, 0, 0, 1, 1, 0, false);
			inverted[1][0] = new TexRect(camera, "res/img/border/top-inverted.png",          0, 0, 0, 1, 1, 0, false);
			inverted[2][0] = new TexRect(camera, "res/img/border/top-right-inverted.png",    0, 0, 0, 1, 1, 0, false);
			inverted[0][1] = new TexRect(camera, "res/img/border/left-inverted.png",         0, 0, 0, 1, 1, 0, false);
			inverted[1][1] = null;
			inverted[2][1] = new TexRect(camera, "res/img/border/right-inverted.png",        0, 0, 0, 1, 1, 0, false);
			inverted[0][2] = new TexRect(camera, "res/img/border/bottom-left-inverted.png",  0, 0, 0, 1, 1, 0, false);
			inverted[1][2] = new TexRect(camera, "res/img/border/bottom-inverted.png",       0, 0, 0, 1, 1, 0, false);
			inverted[2][2] = new TexRect(camera, "res/img/border/bottom-right-inverted.png", 0, 0, 0, 1, 1, 0, false);
		}

		positions = new Vector2f[4][4];
		for (int i = 0; i < 4 * 4; ++i)
			positions[i / 4][i % 4] = new Vector2f();
		set(x, y, width, height, thickness);
		this.invert = invert;
	}

	public void set(float x, float y, float width, float height, float thickness) {
		positions[0][0].set(x,                     y                     );
		positions[1][0].set(x + thickness,         y                     );
		positions[2][0].set(x + width - thickness, y                     );
		positions[3][0].set(x + width            , y                     );
		positions[0][1].set(x,                     y + thickness         );
		positions[1][1].set(x + thickness,         y + thickness         );
		positions[2][1].set(x + width - thickness, y + thickness         );
		positions[3][1].set(x + width            , y + thickness         );
		positions[0][2].set(x,                     y + height - thickness);
		positions[1][2].set(x + thickness,         y + height - thickness);
		positions[2][2].set(x + width - thickness, y + height - thickness);
		positions[3][2].set(x + width            , y + height - thickness);
		positions[0][3].set(x,                     y + height            );
		positions[1][3].set(x + thickness,         y + height            );
		positions[2][3].set(x + width - thickness, y + height            );
		positions[3][3].set(x + width            , y + height            );
	}

	public void render() {
		TexRect[][] border = invert ? BorderRect.inverted : BorderRect.borders;
		for (int i = 0; i < borders.length; ++i) {
			for (int j = 0; j < borders[0].length; ++j) {
				if (border[i][j] != null) {
					border[i][j].setPosition(positions[i][j]);
					border[i][j].setDims(positions[i + 1][j + 1].sub(positions[i][j], new Vector2f()));
					border[i][j].render();
				}
			}
		}
	}
}

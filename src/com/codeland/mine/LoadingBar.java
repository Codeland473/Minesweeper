package com.codeland.mine;

import com.gnarly.engine.display.Camera;
import com.gnarly.engine.model.Rect;
import com.gnarly.engine.shaders.Shader;
import com.gnarly.engine.shaders.Shader2l;
import com.gnarly.engine.texture.Texture;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class LoadingBar extends Rect {

	private static Texture background;
	private static Texture foreground;

	private Shader2l shader = Shader.SHADER2L;

	private float percent;

	public LoadingBar(Camera camera, float x, float y, float width, float height) {
		super(camera, x, y, 0, width, height, 0, false);
		percent = 0;
		if (background == null) {
			background = new Texture("res/img/top-bar/loading/mine-background.png");
			foreground = new Texture("res/img/top-bar/loading/mine-foreground.png");
		}
	}

	public void render() {
		background.bind();
		foreground.bind(1);
		shader.enable();
		Matrix4f cmat = gui ? camera.getProjection() : camera.getMatrix();
		shader.setMVP(cmat.translate(position.add(width * scale / 2, height * scale / 2, 0, new Vector3f())).rotateZ(rotation).scale(width * scale, height * scale, 1).translate(-0.5f, -0.5f, 0));
		shader.setPercent(percent);
		vao.render();
		shader.disable();
		background.unbind();
	}

	public void setPercent(float percent) {
		this.percent = percent;
	}
}

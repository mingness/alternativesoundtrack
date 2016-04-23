package altsoundtrack.video;

import java.nio.file.Files;
import java.nio.file.Path;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PVector;

public class Mask {
	protected PApplet p5;
	private PGraphics pg;
	private PImage img;
	private boolean initialized = false;
	private boolean enabled = false;
	private int numPixels;
	private boolean[] mask;
	private String path;
	private PVector lastClickPos;

	public Mask(PApplet p5, boolean enabledDefault) {
		this.p5 = p5;
		enabled = enabledDefault;
	}

	public void load(Path p) {
		path = p.toString();
		if (Files.exists(p)) {
			img = p5.loadImage(path);
		}
		pg.beginDraw();
		pg.image(img, 0, 0);
		pg.endDraw();

		updateMask();
	}

	public void save() {
		// here I had pg.save and it was saving the image with the
		// wrong size. It was taking the size from the main applet.
		// I even had a println(pg.width, pg.height) to make sure,
		// and the size was 640x480, but the resulting file was
		// larger! (P5 bug)
		img.save(path);
	}

	public void setClick(float x, float y) {
		lastClickPos = new PVector(x, y);
	}

	private void updateMask() {
		numPixels = img.width * img.height;
		img.loadPixels();
		mask = new boolean[numPixels];
		float white = pg.color(255);
		for (int i = 0; i < numPixels; i++) {
			mask[i] = img.pixels[i] == white;
		}
	}

	public void drawDot() {
		if (pg != null) {
			float w = pg.width * 0.078125f * 2;
			pg.beginDraw();
			pg.fill(255);
			pg.noStroke();
			pg.ellipse(lastClickPos.x * pg.width, lastClickPos.y * pg.height, w,
					w);
			pg.endDraw();
			img = pg.get();
			updateMask();
			save();
		}
	}

	public void clear() {
		if (pg != null) {
			pg.beginDraw();
			pg.clear();
			pg.endDraw();
			img = pg.get();
			updateMask();
			save();
		}
	}

	public void process(PImage img) {
		if (numPixels == 0) {
			return;
		}
		img.loadPixels();
		final int masked = pg.color(255, 0, 255);
		final int currNumPixels = img.width * img.height;
		if (currNumPixels == numPixels) {
			for (int i = 0; i < numPixels; i++) {
				if (mask[i]) {
					img.pixels[i] = masked;
				}
			}
		}
		img.updatePixels();
	}

	public boolean isInitialized() {
		return initialized;
	}

	public void initialize(int w, int h, int fps) {
		pg = p5.createGraphics(w, h);
		img = p5.createImage(w, h, PConstants.ARGB);
		initialized = true;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean e) {
		enabled = e;
	}
}

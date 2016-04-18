package altsoundtrack.video;

import java.nio.file.Files;
import java.nio.file.Path;

import processing.core.PApplet;
import processing.core.PImage;

public class BgSubtract {
	protected PApplet p5;
	private PImage img;
	private boolean enabled;
	private int numPixels = 0;
	private int[] bgR;
	private int[] bgG;
	private int[] bgB;
	private String path;

	public BgSubtract(PApplet p5, boolean enabledDefault) {
		this.p5 = p5;
		enabled = enabledDefault;
	}

	public void load(Path p) {
		if (Files.exists(p)) {
			path = p.toString();
			img = p5.loadImage(path);
			setImage(img);
		}
	}

	public void save(PImage newImg) {
		newImg.save(path);
		setImage(newImg);
	}

	private void setImage(PImage newImg) {
		if (newImg == null) {
			numPixels = 0;
			return;
		}
		numPixels = newImg.width * newImg.height;
		bgR = new int[numPixels];
		bgG = new int[numPixels];
		bgB = new int[numPixels];
		for (int i = 0; i < numPixels; i++) {
			int currColor = newImg.pixels[i];
			// Extract the red, green, and blue components from current pixel
			bgR[i] = (currColor >> 16) & 0xFF; // Like red(), but faster
			bgG[i] = (currColor >> 8) & 0xFF;
			bgB[i] = currColor & 0xFF;
		}
	}

	public void process(PImage img) {
		if (numPixels == 0) {
			return;
		}

		img.loadPixels();
		int currNumPixels = img.width * img.height;
		if (currNumPixels == numPixels) {
			for (int i = 0; i < numPixels; i++) {
				int currColor = img.pixels[i];
				// Extract the red, green, and blue components from current
				// pixel
				int currR = (currColor >> 16) & 0xFF; // Like red(), but faster
				int currG = (currColor >> 8) & 0xFF;
				int currB = currColor & 0xFF;
				// Compute the difference of the red, green, and blue values
				int diffR = ((currR - bgR[i]) < 0) ? 0 : (currR - bgR[i]);
				int diffG = ((currG - bgG[i]) < 0) ? 0 : (currG - bgG[i]);
				int diffB = ((currB - bgB[i]) < 0) ? 0 : (currB - bgB[i]);
				int a = 255;
				a = a << 24;
				diffR = diffR << 16;
				diffG = diffG << 8;

				// Equivalent to "color argb = color(r, g, b, a)" but faster
				img.pixels[i] = a | diffR | diffG | diffB;
			}
		}
		img.updatePixels();
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean e) {
		enabled = e;
	}
}

package altsoundtrack.video;

import processing.core.PApplet;
import processing.core.PImage;

public class BgSubtract {
	protected PApplet p5;
	private int numPixels;
	private int[] bgR;
	private int[] bgG;
	private int[] bgB;

	public BgSubtract(PApplet p5, PImage bgImage) {
		this.p5 = p5;
		update(bgImage);
	}

	public PImage subtract(PImage img) {
		int currNumPixels = img.width * img.height;
		if (currNumPixels == numPixels) {
			for (int i = 0; i < numPixels; i++) {
				int currColor = img.pixels[i];
				// Extract the red, green, and blue components from current pixel
				int currR = (currColor >> 16) & 0xFF; // Like red(), but faster
				int currG = (currColor >> 8) & 0xFF;
				int currB = currColor & 0xFF;
				// Compute the difference of the red, green, and blue values
			int diffR = ((currR - bgR[i]) < 0) ? 0 : (currR - bgR[i]);
			int diffG = ((currG - bgG[i]) < 0) ? 0 : (currG - bgG[i]);
			int diffB = ((currB - bgB[i]) < 0) ? 0 : (currB - bgB[i]);
				img.pixels[i] = p5.color(diffR, diffG, diffB);
			}
		}
		return img;
	}

	public void update(PImage bgImage) {
		numPixels = bgImage.width * bgImage.height;
		bgR = new int[numPixels];
		bgG = new int[numPixels];
		bgB = new int[numPixels];
		for (int i = 0; i < numPixels; i++) {
			int currColor = bgImage.pixels[i];
			// Extract the red, green, and blue components from current pixel
			bgR[i] = (currColor >> 16) & 0xFF; // Like red(), but faster
			bgG[i] = (currColor >> 8) & 0xFF;
			bgB[i] = currColor & 0xFF;
		}
	}
	
}


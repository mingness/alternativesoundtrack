package analysis;

import oscP5.OscMessage;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;

/**
 * Video frame difference analyzer
 *
 * @author hamoid
 *
 */
public class FrameDiffAnalysis implements IAnalysis {
	private final PApplet p5;
	private final int numPixelTests = 100;
	private int numPixels;
	private int firstTestPixel;
	private int pixelStepSize;
	private int movementSum;
	private final int[] pixelR = new int[numPixelTests];
	private final int[] pixelG = new int[numPixelTests];
	private final int[] pixelB = new int[numPixelTests];
	private final PGraphics diffGraph;
	private final int sceneCutThreshold = 35;
	// Maybe to implement:
	// private final int sceneCutMinFrameDist = 5;

	/**
	 * @param p5
	 *            Receive the Processing context so this class can access the
	 *            Processing API and draw things on the screen
	 */
	public FrameDiffAnalysis(PApplet p5) {
		this.p5 = p5;
		diffGraph = p5.createGraphics(p5.width, 128);
	}

	/**
	 * Analyze a video frame. Do calculations based on the received bitmap,
	 * which can be later drawn to the screen and/or sent as OSC to
	 * Supercollider.
	 *
	 * @param img
	 *            The video frame to be analyzed
	 */
	@Override
	public void analyze(PImage img) {
		numPixels = img.width * img.height;
		firstTestPixel = numPixels / 4;
		pixelStepSize = (numPixels / 2) / numPixelTests;
		movementSum = 0;
		for (int i = 0; i < numPixelTests; i++) {
			int px = firstTestPixel + i * pixelStepSize;

			int currColor = img.pixels[px];
			// Extract the red, green, and blue components from current pixel
			int currR = (currColor >> 16) & 0xFF; // Like red(), but faster
			int currG = (currColor >> 8) & 0xFF;
			int currB = currColor & 0xFF;
			// Compute the difference of the red, green, and blue values
			int diffR = Math.abs(currR - pixelR[i]);
			int diffG = Math.abs(currG - pixelG[i]);
			int diffB = Math.abs(currB - pixelB[i]);
			// Add these differences to the running tally
			movementSum += (diffR + diffG + diffB) / 3;
			// Save the current color into the 'previous' buffer
			pixelR[i] = currR;
			pixelG[i] = currG;
			pixelB[i] = currB;
		}
		movementSum /= numPixelTests;
	}

	/**
	 * Draw histogram for debugging purposes
	 */
	@Override
	public void draw() {
		int x = p5.frameCount % p5.width;
		diffGraph.beginDraw();
		if (x == 0) {
			diffGraph.clear();
		}
		diffGraph.stroke(255);
		diffGraph.line(x, diffGraph.height, x,
				diffGraph.height - movementSum / 2);
		diffGraph.endDraw();
		p5.image(diffGraph, 0, p5.height - diffGraph.height);
	}

	/**
	 * Creates an OSC message contaning the result of the analysis
	 *
	 * @return Message including an OscMessage if there was a scene cut or null
	 *         otherwise
	 */
	@Override
	public OscMessage getOSCmsg() {
		// Send OSC msg to Supercollider
		return movementSum > sceneCutThreshold ? new OscMessage("/scenecut")
				: null;
	}

}

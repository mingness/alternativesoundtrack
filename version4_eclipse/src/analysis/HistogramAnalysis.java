package analysis;

import oscP5.OscMessage;
import processing.core.PApplet;
import processing.core.PImage;

/**
 * Video histogram analyzer
 *
 * @author mingness
 * @author hamoid
 *
 */
public class HistogramAnalysis implements IAnalysis {
	private final int numBaseFreqs = 7;
	private final int numOctaves = 7;
	private final int numWaves = numBaseFreqs * numOctaves;
	private final float[] histLeft = new float[numWaves];
	private final float[] histRight = new float[numWaves];
	private float H, S, B;
	private final float greyThreshold = 25;
	private int numPixels;
	private final int cap = 0;
	private final float power = 2;
	private final PApplet p5;

	/**
	 * @param p5
	 *            Receive the Processing context so this class can access the
	 *            Processing API and draw things on the screen
	 */
	public HistogramAnalysis(PApplet p5) {
		this.p5 = p5;
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

		for (int i = 0; i < numWaves; i++) {
			histLeft[i] = 0;
			histRight[i] = 0;
		}

		// Calculate the histogram
		for (int i = 0; i < numPixels; i++) {
			int thisColor = img.pixels[i];

			S = p5.saturation(thisColor); // min threshold? otherwise grey
			if (S > greyThreshold) {

				H = p5.hue(thisColor);

				float mappedH;
				if (H > 212.5 || H <= 10) {
					mappedH = 0; // red
				} else if (H <= 32) {
					mappedH = 1; // orange
				} else if (H <= 53) {
					mappedH = 2; // yellow
				} else if (H <= 106.25) {
					mappedH = 3; // green
				} else if (H <= 149) {
					mappedH = 4; // cyan
				} else if (H <= 184) {
					mappedH = 5; // blue
				} else {
					mappedH = 6; // violet
				}

				B = p5.brightness(thisColor);
				// ignore black black, and white white, borders can max
				// normalization
				if (B >= cap && B <= 255 - cap) {
					float mappedB = PApplet.map(B, cap, 255 - cap, 0,
							numOctaves - 1);

					int thisIndex = PApplet.round(numBaseFreqs * mappedB
							+ mappedH);
					if ((i % img.width) < (img.width / 2)) {
						histLeft[thisIndex] += 1;
					} else {
						histRight[thisIndex] += 1;
					}
				} // B
			} // if S
		} // for

		// Find the largest value in the histogram
		float maxValLeft = 0;
		float maxValRight = 0;
		for (int i = 0; i < numWaves; i++) {
			if (histLeft[i] > maxValLeft) {
				maxValLeft = histLeft[i];
			}
			if (histRight[i] > maxValRight) {
				maxValRight = histRight[i];
			}
		}

		// Normalize the histogram to values between 0 and 0.5 (normalization to
		// 1 causes distortion) and power the hist
		for (int i = 0; i < numWaves; i++) {
			histLeft[i] = PApplet.pow(histLeft[i] / maxValLeft * 0.5f, power);
			histRight[i] = PApplet
					.pow(histRight[i] / maxValRight * 0.5f, power);
		}
	}

	/**
	 * Draw histogram for debugging purposes
	 */
	@Override
	public void draw() {
		if (p5.frameCount % 50 == 0) {
			PApplet.printArray(histLeft);
			PApplet.printArray(histRight);
		}
		p5.stroke(255);
		p5.strokeWeight(1);
		for (int i = 0; i < numWaves; i++) {
			float x = PApplet.map(i, 0, numWaves, 0, p5.width / 2);
			p5.line(x, p5.height, x, p5.height - histLeft[i] * 200);
			x += p5.width / 2;
			p5.line(x, p5.height, x, p5.height - histRight[i] * 200);
		}

	}

	/**
	 * Creates an OSC message contaning the result of the analysis
	 *
	 * @return Message including the left histogram and the right histogram as
	 *         one array of floats
	 */
	@Override
	public OscMessage getOSCmsg() {
		// Send OSC msg to Supercollider
		OscMessage msg = new OscMessage("/hist");
		msg.add(histLeft);
		msg.add(histRight);
		return msg;
	}

}

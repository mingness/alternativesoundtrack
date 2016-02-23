package analysis;

import oscP5.OscMessage;
import processing.core.PApplet;
import processing.core.PImage;

/**
 * Create regular events out of video frame data
 *
 * @author hamoid
 *
 */
public class SequencerAnalysis implements IAnalysis {
	private final PApplet p5;
	private final int numPixelTestsH = 4;
	private final int numPixelTestsV = 4;
	private final int numPixelTests = numPixelTestsH * numPixelTestsV;
	private int result;
	private int step;
	private int nextCheckMs = 0;
	private float x, y;
	private int imgWidth = 0;
	private int imgHeight = 0;
	public boolean initialized = false;

	/**
	 * @param p5
	 *            Receive the Processing context so this class can access the
	 *            Processing API and draw things on the screen
	 */
	public SequencerAnalysis(PApplet p5) {
		this.p5 = p5;
	}

	public void setSize(int w, int h) {
		imgWidth = w;
		imgHeight = h;
		initialized = true;
	}

	private void incStep() {
		step = (step + 1) % numPixelTests;
		nextCheckMs += 125;
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
		// calculate normalized coordinates
		x = 0.25f + 1f / (numPixelTestsH + 2) * (step % numPixelTestsH);
		y = 0.25f + 1f / (numPixelTestsV - 1) * (step / numPixelTestsV) / 2;
		// calculate which pixel to sample from the image
		int px = (int) (imgWidth * x + y * imgHeight * imgWidth);
		// get the color
		result = img.pixels[px];
		// scale x and y to match the window to visualize the sampling location
		x *= p5.width;
		y *= p5.height;
	}

	/**
	 * Draw current step position for debugging purposes
	 */
	@Override
	public void draw() {
		p5.noFill();
		p5.stroke(0, 100);
		p5.ellipse(x, y, 10, 10);
		p5.stroke(255);
		p5.ellipse(x, y - 1, 10, 10);
	}

	/**
	 * Creates an OSC message contaning the result of the analysis
	 *
	 * @return Message including an OscMessage if there was a scene cut or null
	 *         otherwise
	 */
	@Override
	public OscMessage getOSCmsg() {
		// Send OSC msg to Supercollider if
		// we are at the right time.
		// This is a crude and simple technique to
		// keep the tempo. Better would be to decide the
		// actual BPM we want, and observe the current
		// milliseconds timestamp, or use some kind of
		// precise clock better suited for music.
		if (p5.millis() > nextCheckMs) {
			incStep();
			OscMessage msg = new OscMessage("/seq");
			msg.add(p5.hue(result) / 255f);
			msg.add(p5.saturation(result) / 255f);
			msg.add(p5.brightness(result) / 255f);
			return msg;
		}
		return null;
	}

}

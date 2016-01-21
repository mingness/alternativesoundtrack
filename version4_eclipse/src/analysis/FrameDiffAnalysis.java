package analysis;

import oscP5.OscMessage;
import processing.core.PApplet;
import processing.core.PImage;

/**
 * Video histogram analyzer
 *
 * @author hamoid
 *
 */
public class FrameDiffAnalysis implements IAnalysis {
	private final PApplet p5;

	/**
	 * @param p5
	 *            Receive the Processing context so this class can access the
	 *            Processing API and draw things on the screen
	 */
	public FrameDiffAnalysis(PApplet p5) {
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
	}

	/**
	 * Draw histogram for debugging purposes
	 */
	@Override
	public void draw() {
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
		OscMessage msg = new OscMessage("/frmdiff");
		// msg.add(histLeft);
		// msg.add(histRight);
		return msg;
	}

}

/**
 *
 */
package analysis;

import oscP5.OscMessage;
import processing.core.PApplet;
import processing.core.PImage;

/**
 * @author hamoid
 *
 */
public class BaseAnalysis {
	protected final PApplet p5;
	private boolean initialized = false;
	private boolean enabled = false;
	protected int width;
	protected int height;
	protected int fps;

	public BaseAnalysis(PApplet p5) {
		this.p5 = p5;
	}

	public void analyze(PImage img) {
	}

	public void draw() {
	}

	public boolean isInitialized() {
		return initialized;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean e) {
		enabled = e;
	}

	public void setParams(int id, float val) {
	}

	public void initialize(int w, int h, int fps) {
		width = w;
		height = h;
		this.fps = fps;
		initialized = true;
	}

	public void restart() {
		initialized = false;
	}

	public OscMessage getOSCmsg() {
		return null;
	}

}

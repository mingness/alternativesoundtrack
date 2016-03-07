package analysis;

import oscP5.OscMessage;
import processing.core.PImage;

/**
 * Interface that all Analysis classes must implement
 *
 * @author hamoid
 *
 */
public interface IAnalysis {
	public void analyze(PImage img);

	public void draw();

	public boolean isInitialized();

	public boolean isEnabled();

	public void initialize(int w, int h, int fps);

	public void restart();

	public OscMessage getOSCmsg();
}

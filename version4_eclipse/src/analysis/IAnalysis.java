package analysis;

import oscP5.OscMessage;
import processing.core.PImage;

/**
 * Interface that all Analysis classe must implement
 *
 * @author hamoid
 *
 */
public interface IAnalysis {
	public void analyze(PImage img);

	public void draw();

	public boolean isInitialized();

	public void initialize(int w, int h, int fps);

	public OscMessage getOSCmsg();
}

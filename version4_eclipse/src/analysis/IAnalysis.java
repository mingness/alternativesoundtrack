package analysis;

import oscP5.OscMessage;
import processing.core.PImage;

public interface IAnalysis {

	public void analyze(PImage img);

	public void draw();

	public OscMessage getOSCmsg();
}

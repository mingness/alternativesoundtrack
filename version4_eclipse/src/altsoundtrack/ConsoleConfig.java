package altsoundtrack;

/**
 *         Config data for the web-based console (rhizome, nexusUI);
 *         This initializes the console to the current settings; 
 *         This is saved after every change by the console
 *         to a javascript file, in JSON format, in the app folder.
 *
 */
public class ConsoleConfig {
	// Tweak
	public boolean displayEnabled = true;
	public boolean enableBGSub = false;
	public double opticalFlowReg = 0.5; 
	public double opticalFlowSm = 0.5; 
	public double videoTime = 0.5; 
	// Mask
	public boolean enableMask = false;
}

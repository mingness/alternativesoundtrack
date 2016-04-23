package altsoundtrack;

/**
 * @author aBe
 *         Config data; a series of public properties of different types.
 *         They are stored in a json file for persistence.
 *
 */
public class Config {
	public String moviePath = "movies/";
	public String dataPath = "data/";
	public String screenshotPath = "control_panel/pages/screenshot/screenshot.jpg";
	public String bgImageFile = "bgImage.tif";
	public String maskImageFile = "maskImage.tif";

	public String supercolliderIp = "127.0.0.1";
	public int supercolliderPort = 57120;

	public String rhizomeIp = "127.0.0.1";
	public int rhizomePort = 57130;

	public int listenOnPort = 57140;

	public int frameRate = 30;
	public boolean useWebcam = true;

	public String webcamName = "/dev/video0";
	public int webcamWidth = 640;
	public int webcamHeight = 480;
	public int webcamFPS = 30;
}

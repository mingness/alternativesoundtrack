package altsoundtrack;

/**
 * @author aBe
 *         Config data; a series of public properties of different types.
 *         They are stored in a json file for persistence.
 *
 */
public class Config {
	public String moviePath = "movies/";
	public String[] movieFilenames = { "helloworld.mp4" };

	public String supercolliderIp = "127.0.0.1";
	public int supercolliderPort = 57120;

	public int frameRate = 30;
	public boolean useWebcam = false;
	public int webcamId = 0;
}

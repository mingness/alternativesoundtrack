package altsoundtrack;

import java.io.File;

import netP5.NetAddress;
import oscP5.OscMessage;
import oscP5.OscP5;
import processing.core.PApplet;
import processing.video.Movie;

public class AltSoundtrack2 extends PApplet {
	// OSC
	private OscP5 osc;
	private NetAddress supercollider;

	// Config
	private Config cfg;
	@SuppressWarnings("unused")
	private ConfigManager cfgManager;

	// Video
	Movie video;

	// Other
	private final float[] L = new float[15];
	private final float[] R = new float[15];

	/*
	 * (non-Javadoc)
	 * @see processing.core.PApplet#settings()
	 */
	@Override
	public void settings() {
		size(600, 600);
		// fullScreen();
	}

	/*
	 * (non-Javadoc)
	 * @see processing.core.PApplet#setup()
	 */
	@Override
	public void setup() {
		// Config
		cfgManager = new ConfigManager("altSoundtrackConfig.json");
		if (cfgManager.configExists()) {
			cfg = cfgManager.load();
		} else {
			cfg = new Config();
		}

		// OSC
		osc = new OscP5(this, 12000);
		supercollider = new NetAddress(cfg.supercolliderIp,
				cfg.supercolliderPort);

		File f = new File(sketchPath() + File.separator + cfg.moviePath
				+ File.separator + cfg.movieFilenames[0]);

		// Movie
		video = new Movie(this, f.getAbsolutePath());
		video.loop();
		video.volume(0);

		frameRate(cfg.frameRate);
	}

	/*
	 * (non-Javadoc)
	 * @see processing.core.PApplet#draw()
	 */
	@Override
	public void draw() {
		if (!video.available()) {
			return;
		}
		video.read();
		video.loadPixels();

		background(0);

		image(video, 0, 0);

		fill(255);
		text("Sending noise to Supercollider", 20, frameCount % height);

		for (int i = 0; i < L.length; i++) {
			L[i] = 0.5f * abs(noise(i * 0.08f, 0.1f, frameCount * 0.1f) - 0.5f);
			R[i] = 0.5f * abs(noise(i * 0.08f, 0.5f, frameCount * 0.1f) - 0.5f);
		}

		OscMessage msg = new OscMessage("/starhit");
		msg.add(L);
		msg.add(R);
		osc.send(msg, supercollider);

	}

	public static void main(String[] args) {
		String[] options = { "--bgcolor=#000000", "--hide-stop",
		"altsoundtrack.AltSoundtrack2" };

		PApplet.main(options);
	}

}

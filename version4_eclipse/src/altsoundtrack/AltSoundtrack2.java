package altsoundtrack;

import java.io.File;

import netP5.NetAddress;
import oscP5.OscP5;
import processing.core.PApplet;
import processing.video.Movie;
import analysis.HistogramAnalysis;

public class AltSoundtrack2 extends PApplet {
	// OSC
	private OscP5 osc;
	private NetAddress supercollider;

	// Config
	private Config cfg;
	private ConfigManager cfgManager;

	// Video
	Movie video;

	// Analysis
	HistogramAnalysis histogram;

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

		histogram = new HistogramAnalysis(this);

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

		image(video, 0, 0, width, height);
		drawProgressBar();

		histogram.analyze(video);
		histogram.draw();
		osc.send(histogram.getOSCmsg(), supercollider);
	}

	public static void main(String[] args) {
		String[] options = { "--bgcolor=#000000", "--hide-stop",
				"altsoundtrack.AltSoundtrack2" };

		PApplet.main(options);
	}

	private void drawProgressBar() {
		float time = video.time() / video.duration();
		stroke(0, 150, 0);
		strokeWeight(4);
		line(0, height - 2, time * width, height - 2);
	}

}

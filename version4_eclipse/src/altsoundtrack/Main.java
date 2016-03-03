package altsoundtrack;

import java.io.File;
import java.util.ArrayList;

import analysis.FrameDiffAnalysis;
import analysis.HistogramAnalysis;
import analysis.IAnalysis;
import analysis.OpticalFlowAnalysis;
import analysis.SequencerAnalysis;
import netP5.NetAddress;
import oscP5.OscMessage;
import oscP5.OscP5;
import processing.core.PApplet;
import processing.video.Movie;

/**
 * Main project file
 *
 * @author hamoid
 *
 */
public class Main extends PApplet {
	// OSC
	private OscP5 osc;
	private NetAddress supercollider;

	// Config
	private Config cfg;
	private ConfigManager cfgManager;

	// Video
	Movie video;

	// Analyses
	ArrayList<IAnalysis> analyses = new ArrayList<IAnalysis>();

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
			cfgManager.save(cfg);
		}

		// OSC
		osc = new OscP5(this, 12000);
		supercollider = new NetAddress(cfg.supercolliderIp,
				cfg.supercolliderPort);

		File f = new File(sketchPath() + File.separator + cfg.moviePath
				+ File.separator + cfg.movieFilenames[1]);

		// Movie
		video = new Movie(this, f.getAbsolutePath());
		video.loop();
		video.volume(0);

		analyses.add(new HistogramAnalysis(this));
		analyses.add(new FrameDiffAnalysis(this));
		analyses.add(new OpticalFlowAnalysis(this));
		analyses.add(new SequencerAnalysis(this));

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

		// Run all analyses
		for (IAnalysis a : analyses) {
			if (a.isInitialized()) {
				a.analyze(video);
				a.draw();
				sendOsc(a.getOSCmsg());
			} else {
				a.initialize(video.width, video.height,
						Math.round(video.frameRate));
			}
		}
	}

	public static void main(String[] args) {
		String[] options = { "--bgcolor=#000000", "--hide-stop",
				"altsoundtrack.Main" };

		PApplet.main(options);
	}

	private void drawProgressBar() {
		float time = video.time() / video.duration();
		stroke(0, 150, 0);
		strokeWeight(4);
		line(0, height - 2, time * width, height - 2);
	}

	private void sendOsc(OscMessage msg) {
		if (msg != null) {
			osc.send(msg, supercollider);
		}
	}

}

package altsoundtrack;

import java.util.ArrayList;

import altsoundtrack.video.AltMovie;
import altsoundtrack.video.AltMovieFile;
import altsoundtrack.video.AltMovieWebcam;
import analysis.FrameDiffAnalysis;
import analysis.HistogramAnalysis;
import analysis.IAnalysis;
import analysis.OpticalFlowAnalysis;
import analysis.SequencerAnalysis;
import netP5.NetAddress;
import oscP5.OscMessage;
import oscP5.OscP5;
import processing.core.PApplet;
import processing.core.PImage;

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
	AltMovie video;

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

		if (cfg.useWebcam) {
			video = new AltMovieWebcam(this, cfg);
		} else {
			video = new AltMovieFile(this, cfg, 1);
		}

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

		video.display();
		drawProgressBar();

		// Run all analyses
		for (IAnalysis a : analyses) {
			if (a.isInitialized()) {
				a.analyze(video.getImg());
				a.draw();
				sendOsc(a.getOSCmsg());
			} else {
				PImage v = video.getImg();
				a.initialize(v.width, v.height, video.getFrameRate());
			}
		}
	}

	public static void main(String[] args) {
		String[] options = { "--bgcolor=#000000", "--hide-stop",
				"altsoundtrack.Main" };

		PApplet.main(options);
	}

	private void drawProgressBar() {
		stroke(0, 150, 0);
		strokeWeight(4);
		line(0, height - 2, video.currPos() * width, height - 2);
	}

	private void sendOsc(OscMessage msg) {
		if (msg != null) {
			osc.send(msg, supercollider);
		}
	}

}

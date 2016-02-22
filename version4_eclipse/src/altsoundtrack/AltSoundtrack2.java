package altsoundtrack;

import java.io.File;

import analysis.FrameDiffAnalysis;
import analysis.HistogramAnalysis;
import analysis.OpticalFlowAnalysis;
import analysis.SequencerAnalysis;
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
	private ConfigManager cfgManager;

	// Video
	Movie video;

	// Analysis
	HistogramAnalysis a_histogram;
	FrameDiffAnalysis a_frameDiff;
	OpticalFlowAnalysis a_optFlow;
	SequencerAnalysis a_sequencer;

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

		a_histogram = new HistogramAnalysis(this);
		a_frameDiff = new FrameDiffAnalysis(this);
		a_optFlow = new OpticalFlowAnalysis(this);
		a_sequencer = new SequencerAnalysis(this);

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

		// a_histogram.analyze(video);
		// a_histogram.draw();
		// sendOsc(a_histogram.getOSCmsg());

		// a_frameDiff.analyze(video);
		// a_frameDiff.draw();
		// sendOsc(a_frameDiff.getOSCmsg());

		/*
		 * if (a_optFlow.initialized) {
		 * a_optFlow.analyze(video);
		 * a_optFlow.draw();
		 * sendOsc(a_optFlow.getOSCmsg());
		 * } else {
		 * a_optFlow.setSize(video.width, video.height, 30);
		 * a_optFlow.setFPS((int) video.frameRate);
		 * }
		 */

		if (a_sequencer.initialized) {
			a_sequencer.analyze(video);
			a_sequencer.draw();
			sendOsc(a_sequencer.getOSCmsg());
		} else {
			a_sequencer.setSize(video.width, video.height);
		}
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

	private void sendOsc(OscMessage msg) {
		if (msg != null) {
			osc.send(msg, supercollider);
		}
	}

}

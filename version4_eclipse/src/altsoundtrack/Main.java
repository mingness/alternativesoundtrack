package altsoundtrack;

import java.io.File;
import java.util.ArrayList;

import altsoundtrack.video.AltMovie;
import altsoundtrack.video.AltMovieFile;
import altsoundtrack.video.AltMovieWebcam;
import analysis.FrameDiffAnalysis;
import analysis.HistogramAnalysis;
import analysis.IAnalysis;
import analysis.OpticalFlowAnalysis;
import analysis.SequencerAnalysis;
import controlP5.CallbackEvent;
import controlP5.CallbackListener;
import controlP5.ControlP5;
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
	private AltMovie video;
	private File[] movies;
	private int whichMovie = 0;
	private boolean loadMovie = false;

	// Analyses
	ArrayList<IAnalysis> analyses = new ArrayList<IAnalysis>();

	// Control panel
	ControlFrame cf;
	CallbackListener cb;

	// In Processing 3 you specify size() inside settings()
	@Override
	public void settings() {
		size(600, 600);
		// fullScreen();
	}

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

		analyses.add(new HistogramAnalysis(this));
		analyses.add(new FrameDiffAnalysis(this));
		analyses.add(new OpticalFlowAnalysis(this));
		analyses.add(new SequencerAnalysis(this));

		frameRate(cfg.frameRate);

		if (cfg.useWebcam) {
			video = new AltMovieWebcam(this);
			video.play(cfg.webcamId);
		} else {
			File path = new File(cfg.moviePath);
			if (path.isDirectory()) {
				video = new AltMovieFile(this);

				movies = path.listFiles();

				video.play(movies[whichMovie].getAbsolutePath());
			}
		}

		cb = new CallbackListener() {
			@Override
			public void controlEvent(CallbackEvent e) {
				if (e.getAction() == ControlP5.ACTION_BROADCAST) {
					whichMovie = (int) e.getController().getValue();
					loadMovie = true;
				}
			}
		};

		cf = new ControlFrame(analyses, movies, cb);
	}

	public void onLoadMovie() {

	}

	/*
	 * (non-Javadoc)
	 * @see processing.core.PApplet#draw()
	 */
	@Override
	public void draw() {
		if (loadMovie) {
			video.stop();
			video.play(movies[whichMovie].getAbsolutePath());

			// Restart analyses, since video resolution
			// might have changed
			for (IAnalysis analysis : analyses) {
				analysis.restart();
			}
			loadMovie = false;
		}

		if (!video.available()) {
			return;
		}

		video.display();
		drawProgressBar();

		// Run all analyses
		for (IAnalysis analysis : analyses) {
			if (analysis.isInitialized()) {
				if (analysis.isEnabled()) {
					analysis.analyze(video.getImg());
					analysis.draw();
					sendOsc(analysis.getOSCmsg());
				}
			} else {
				PImage v = video.getImg();
				analysis.initialize(v.width, v.height, video.getFrameRate());
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

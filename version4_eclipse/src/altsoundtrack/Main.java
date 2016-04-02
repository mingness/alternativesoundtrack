package altsoundtrack;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import altsoundtrack.video.AltMovie;
import altsoundtrack.video.AltMovieFile;
import altsoundtrack.video.AltMovieWebcam;
import altsoundtrack.video.BgSubtract;
import analysis.BaseAnalysis;
import analysis.BlobAnalysis;
import analysis.HistogramAnalysis;
import analysis.OpticalFlowAnalysis;
import analysis.SequencerAnalysis;
import controlP5.CallbackEvent;
import controlP5.CallbackListener;
import controlP5.ControlP5;
import controlP5.Controller;
import netP5.NetAddress;
import oscP5.OscMessage;
import oscP5.OscP5;
import processing.core.PApplet;
import processing.core.PImage;

/**
 * Main project file
 *
 * @author hamoid, mingness
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
	private boolean useWebcam;
	private File[] movies;
	private int whichMovie = 0;
	private final boolean bgsubDefault = true;
	private PImage bgImage;
	private BgSubtract bgsub;

	private boolean webcamChanged = false;
	private boolean whichMovieChanged = false;

	// Analyses
	ArrayList<BaseAnalysis> analyses = new ArrayList<BaseAnalysis>();

	// Control panel
	ControlFrame cf;
	CallbackListener cb;

	// In Processing 3 you specify size() inside settings()
	@Override
	public void settings() {
		// size(600, 600);
		fullScreen();
	}

	@Override
	public void setup() {
		// Config
		cfgManager = new ConfigManager("altSoundtrackConfig.json");
		if (cfgManager.configExists()) {
			cfg = cfgManager.load();
		} else {
			cfg = new Config();
		}
		cfgManager.save(cfg);
		useWebcam = cfg.useWebcam;

		// OSC
		osc = new OscP5(this, 12000);
		supercollider = new NetAddress(cfg.supercolliderIp,
				cfg.supercolliderPort);

		// Process
		bgsub = new BgSubtract(this, bgsubDefault);
		if (Files.exists(Paths.get(cfg.dataPath, cfg.bgImageFile))) {
			bgImage = loadImage(
					Paths.get(cfg.dataPath, cfg.bgImageFile).toString());
		}
		bgsub.setBGImage(bgImage);

		analyses.add(new HistogramAnalysis(this));
		// analyses.add(new FrameDiffAnalysis(this));
		analyses.add(new OpticalFlowAnalysis(this));
		analyses.add(new SequencerAnalysis(this));
		analyses.add(new BlobAnalysis(this));

		frameRate(cfg.frameRate);

		File path = new File(cfg.moviePath);
		if (path.isDirectory()) {
			movies = path.listFiles();
		}
		if (useWebcam) {
			video = new AltMovieWebcam(this);
			video.play(cfg.webcamId);
		} else {
			if (movies != null) {
				video = new AltMovieFile(this);
				video.play(movies[whichMovie].getAbsolutePath());
			}
		}

		cb = new CallbackListener() {
			@Override
			public void controlEvent(CallbackEvent e) {
				if (e.getAction() == ControlP5.ACTION_BROADCAST) {
					Controller<?> c = e.getController();
					String name = c.getName();
					if (name.equals("movies")) {
						whichMovie = (int) c.getValue();
						whichMovieChanged = true;
					} else if (name
							.startsWith(ControlFrame.TOGGLE_ANALYSIS_LABEL)) {
						analyses.get(c.getId()).toggleEnabled();
					} else if (name.equals("bgsub")) {
						bgsub.toggleEnabled();
					} else if (name.equals("webcam")) {
						useWebcam = c.getValue() != 0.;
						webcamChanged = true;
					}
				}
			}
		};

		cf = new ControlFrame();
		cf.setAnalyses(analyses);
		cf.setMovies(movies, whichMovie);
		cf.setBgSub(bgsubDefault);
		cf.setWebcam(useWebcam);
		cf.setCallback(cb);
	}

	private void update() {
		if (whichMovieChanged) {
			if (useWebcam) {
				useWebcam = false;
				cf.setWebcam(useWebcam);
				video.stop();
				video = new AltMovieFile(this);
			} else {
				video.stop();
			}
			video.play(movies[whichMovie].getAbsolutePath());

			// Restart analyses, since video resolution
			// might have changed
			for (BaseAnalysis analysis : analyses) {
				analysis.restart();
			}
			whichMovieChanged = false;
		} else if (webcamChanged) {
			video.stop();
			if (useWebcam) {
				video = new AltMovieWebcam(this);
				video.play(cfg.webcamId);
			} else {
				video = new AltMovieFile(this);
				video.play(movies[whichMovie].getAbsolutePath());
			}
			// Restart analyses, since video resolution
			// might have changed
			for (BaseAnalysis analysis : analyses) {
				analysis.restart();
			}
			webcamChanged = false;
		}
	}

	@Override
	public void draw() {
		update();

		if (!video.available()) {
			return;
		}

		// video.display();
		PImage v = video.getImg().copy();
		if (bgsub.isEnabled() & bgImage != null) {
			v = bgsub.subtract(v);
		}
		image(v, 0, 0, width, height);
		drawProgressBar();

		// Run all analyses
		for (BaseAnalysis analysis : analyses) {
			if (analysis.isInitialized()) {
				if (analysis.isEnabled()) {
					// analysis.analyze(video.getImg());
					analysis.analyze(v);
					analysis.draw();
					sendOsc(analysis.getOSCmsg());
				}
			} else {
				// PImage v = video.getImg();
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

	@Override
	public void keyPressed() {
		switch (key) {
			case 'b':
			case 'B':
				bgImage = video.getImg();
				bgImage.save(
						Paths.get(cfg.dataPath, cfg.bgImageFile).toString());
				bgsub.setBGImage(bgImage);
				break;
			case 's':
			case 'S':
				save("/tmp/" + System.currentTimeMillis() + ".png");
				break;
		}
	}
}

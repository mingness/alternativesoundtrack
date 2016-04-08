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
	private NetAddress rhizome;

	// Config
	private Config cfg;
	private ConfigManager cfgManager;

	// Video
	private AltMovie video;
	private boolean useWebcam;
	private File[] movies;
	private final int whichMovie = 0;
	private final boolean bgsubDefault = false;
	private PImage bgImage;
	private BgSubtract bgsub;
	private boolean display_enabled = true;

	private boolean webcamChanged = false;
	private boolean whichMovieChanged = false;

	// Analyses
	ArrayList<BaseAnalysis> analyses = new ArrayList<BaseAnalysis>();

	// Commands
	private final static int CMD_NONE = 0;
	private final static int CMD_SCREENSHOT = 1;
	private final static int CMD_SET_BGSUB = 2;
	private static int CMD = CMD_NONE;

	// In Processing 3 you specify size() inside settings()
	@Override
	public void settings() {
		size(960, 540);
		// fullScreen(P2D);
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
		osc = new OscP5(this, cfg.listenOnPort);

		supercollider = new NetAddress(cfg.supercolliderIp,
				cfg.supercolliderPort);

		rhizome = new NetAddress(cfg.rhizomeIp, cfg.rhizomePort);

		// Subscribe to Rhizome.
		OscMessage subscribeMsg = new OscMessage("/sys/subscribe");
		subscribeMsg.add(cfg.listenOnPort);
		subscribeMsg.add("/p5");
		osc.send(subscribeMsg, rhizome);

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

	}

	private void update() {
		if (whichMovieChanged) {
			if (useWebcam) {
				useWebcam = false;
				// cf.setWebcam(useWebcam);
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

	private void processCommand() {
		switch (CMD) {
			case CMD_NONE:
				return;
			case CMD_SCREENSHOT:
				String fname = "/tmp/" + System.currentTimeMillis() + ".png";
				save(fname);
				println("saved", fname);
				break;
			case CMD_SET_BGSUB:
				bgImage = video.getImg();
				bgImage.save(
						Paths.get(cfg.dataPath, cfg.bgImageFile).toString());
				bgsub.setBGImage(bgImage);
				println("bg image set");
				break;
		}
		CMD = CMD_NONE;
	}

	@Override
	public void draw() {
		processCommand();
		update();

		if (!video.available()) {
			return;
		}

		PImage v;
		if (bgsub.isEnabled() & bgImage != null) {
			v = video.getImg().copy();
			v = bgsub.subtract(v);
			if (display_enabled) {
				image(v, 0, 0, width, height);
			}
		} else {
			v = video.getImg();
			if (display_enabled) {
				video.display();
			}
		}

		if (display_enabled) {
			drawProgressBar();
		}

		// Run all analyses
		for (BaseAnalysis analysis : analyses) {
			if (analysis.isInitialized()) {
				if (analysis.isEnabled()) {
					analysis.analyze(v);
					if (display_enabled) {
						analysis.draw();
					}
					sendOsc(analysis.getOSCmsg());
				}
			} else {
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

	// It's good to implement an OscEventListener.
	// It's a bit more verbose than oscEvent(), but
	// this way OSC error messages are much clearer.
	// Without it, errors are catched inside oscP5
	// and we have no clue of knowing what's going
	// wrong.
	public void oscEvent(OscMessage msg) {
		float val = 0;
		if (msg.checkTypetag("f")) {
			val = msg.get(0).floatValue();
		}
		switch (msg.addrPattern()) {
			case "/sys/subscribed":
				println("subscribed to Rhizome");
				break;
			case "/p5/a_hist":
				analyses.get(0).setEnabled(val > 0.5);
				break;
			case "/p5/a_of":
				analyses.get(1).setEnabled(val > 0.5);
				break;
			case "/p5/a_seq":
				analyses.get(2).setEnabled(val > 0.5);
				break;
			case "/p5/a_blob":
				analyses.get(3).setEnabled(val > 0.5);
				break;
			case "/p5/bgsub":
				bgsub.setEnabled(val > 0.5);
				break;
			case "/p5/set_bg":
				CMD = CMD_SET_BGSUB;
				break;
			case "/p5/screenshot":
				CMD = CMD_SCREENSHOT;
				break;
			case "/p5/display_enabled":
				display_enabled = val > 0.5;
				break;
			case "/p5/of_regression":
				analyses.get(0).setParams(0, val);
				break;
			case "/p5/of_smoothness":
				analyses.get(0).setParams(1, val);
				break;
			default:
				println("unexpected message received: " + msg);
		}
	}
}

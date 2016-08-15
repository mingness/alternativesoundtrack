package altsoundtrack;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;

import altsoundtrack.video.AltMovie;
import altsoundtrack.video.AltMovieFile;
import altsoundtrack.video.AltMovieWebcam;
import altsoundtrack.video.BgSubtract;
import altsoundtrack.video.Mask;
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
	private int whichMovie = 0;
	private final boolean bgsubDefault = false;
	private BgSubtract bgsub;
	private Mask mask;
	private boolean display_enabled = true;

	// Analyses
	ArrayList<BaseAnalysis> analyses = new ArrayList<BaseAnalysis>();

	// Control
	private ConsoleConfig console;
	// Commands are used because OSC messages arrive in a different thread,
	// which is not allowed to draw on the screen. So instead of drawing
	// immediately, we store the command, and use it later inside the main
	// graphics thread (draw loop).
	private boolean webcamChanged = false;
	private boolean whichMovieChanged = false;
	private boolean takeScreenshot = false;
	private boolean setBg = false;
	private boolean sendMovieList = false;

	// In Processing 3 you specify size() inside settings()
	@Override
	public void settings() {
		size(640, 480);
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
		
		// Console Config
		console = new ConsoleConfig();

		// Process
		bgsub = new BgSubtract(this, bgsubDefault);
		bgsub.load(Paths.get(cfg.dataPath, cfg.bgImageFile));

		mask = new Mask(this, false);

		analyses.add(new OpticalFlowAnalysis(this));
		analyses.add(new HistogramAnalysis(this));
		// analyses.add(new FrameDiffAnalysis(this));
		analyses.add(new SequencerAnalysis(this));
		analyses.add(new BlobAnalysis(this));

		frameRate(cfg.frameRate);

		File path = new File(cfg.moviePath);
		if (path.isDirectory()) {
			movies = path.listFiles();
			if (movies != null) {
				String[] ll = new String[movies.length];
				for (int i = 0; i < movies.length; i++) {
					ll[i] = movies[i].getName();
				}
				console.movieList = ll;
			}
		}
		if (useWebcam) {
			video = new AltMovieWebcam(this);
			video.play(cfg.webcamWidth, cfg.webcamHeight, cfg.webcamName,
					cfg.webcamFPS);
		} else {
			if (movies != null) {
				video = new AltMovieFile(this);
				video.play(movies[whichMovie].getAbsolutePath());
			}
		}
	}

	private void updateVideoSource() {
		if (whichMovieChanged) {
			if (useWebcam) {
				useWebcam = false;
				video.stop();
				video = new AltMovieFile(this);
			} else {
				video.stop();
			}
			video.play(movies[whichMovie].getAbsolutePath());

			sendOsc("/panel/movies", whichMovie, rhizome);
			sendOsc("/panel/webcam", useWebcam ? 1 : 0, rhizome);
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
				video.play(cfg.webcamWidth, cfg.webcamHeight, cfg.webcamName,
						cfg.webcamFPS);
			} else {
				video = new AltMovieFile(this);
				video.play(movies[whichMovie].getAbsolutePath());
			}
			sendOsc("/panel/webcam", useWebcam ? 1 : 0, rhizome);
			// Restart analyses, since video resolution
			// might have changed
			for (BaseAnalysis analysis : analyses) {
				analysis.restart();
			}
			webcamChanged = false;
		}
	}

	private void rebroadcastConsoleState(NetAddress to) {
		sendOsc("/panel/p5", 1, to);
		sendOsc("/panel/webcam", useWebcam ? 1 : 0, to);
		sendOsc("/panel/movies", whichMovie, to);
		sendOsc("/panel/video_time", console.video_time, to);
		sendOsc("/panel/display_enabled", console.display_enabled ? 1 : 0, to);
		sendOsc("/panel/mask_enabled", console.mask_enabled ? 1 : 0, to);
		sendOsc("/panel/a_of", analyses.get(0).isEnabled() ? 1 : 0, to);
		sendOsc("/panel/a_hist", analyses.get(1).isEnabled() ? 1 : 0, to);
		sendOsc("/panel/a_seq", analyses.get(2).isEnabled() ? 1 : 0, to);
		sendOsc("/panel/a_blob", analyses.get(3).isEnabled() ? 1 : 0, to);
		sendOsc("/panel/bgsub", console.bgsub ? 1 : 0, to);
		sendOsc("/panel/of_regression",console.of_regression, to);
		sendOsc("/panel/of_smoothness",console.of_smoothness, to);
	}
	
	@Override
	public void draw() {
		updateVideoSource();

		if (!video.available()) {
			return;
		}
		
		PImage v = video.getImg().copy();

		if (sendMovieList) {
			OscMessage msg = new OscMessage("/conf/movies");
			for (int i = 0; i < console.movieList.length; i++) {
				msg.add(console.movieList[i]);
			}
			osc.send(msg, rhizome);
			sendMovieList = false;
		}

		if (takeScreenshot) {
			String fname = cfg.screenshotPath;
			v.save(fname);
			sendOsc("/panel/screenshot", 1, rhizome);
			takeScreenshot = false;
		}
		// Update panel only every 10 frames to reduce network traffic.
		if (frameCount % 10 == 0) {
			sendOsc("/panel/p5", 1, rhizome);
			sendOsc("/panel/video_time", console.video_time, rhizome);
			console.video_time = video.currPos();
		}

		if (setBg) {
			bgsub.save(v);		
			setBg = false;
		}
		if (bgsub.isEnabled()) {
			bgsub.process(v);
		}
		if (mask.isEnabled()) {
			if (mask.isInitialized()) {
				mask.process(v);
			} else {
				mask.initialize(v.width, v.height, video.getFrameRate());
				mask.load(Paths.get(cfg.dataPath, cfg.maskImageFile));
			}
		}
		if (display_enabled) {
			image(v, 0, 0, width, height);
		}

		// Run all analyses
		for (BaseAnalysis analysis : analyses) {
			if (analysis.isInitialized()) {
				if (analysis.isEnabled()) {
					analysis.analyze(v);
					if (display_enabled) {
						analysis.draw();
					}
					sendOsc(analysis.getOSCmsg(), supercollider);
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

	private void sendOsc(OscMessage msg, NetAddress to) {
		if (msg != null) {
			osc.send(msg, to);
		}
	}

	private void sendOsc(String path, float val, NetAddress to) {
		OscMessage msg = new OscMessage(path);
		msg.add(val);
		osc.send(msg, to);
	}
	private void sendOsc(String path, boolean val, NetAddress to) {
		OscMessage msg = new OscMessage(path);
		msg.add(val);
		osc.send(msg, to);
	}
	private void sendOsc(String path, double val, NetAddress to) {
		OscMessage msg = new OscMessage(path);
		msg.add(val);
		osc.send(msg, to);
	}

	// It's good to implement an OscEventListener.
	// It's a bit more verbose than oscEvent(), but
	// this way OSC error messages are much clearer.
	// Without it, errors are catched inside oscP5
	// and we have no clue of knowing what's going
	// wrong.
	public void oscEvent(OscMessage msg) {
//		println(msg.toString());
		float val = 0;
		if (msg.checkTypetag("f")) {
			val = msg.get(0).floatValue();
		}
		switch (msg.addrPattern()) {
			case "/sys/subscribed":
				println("subscribed to Rhizome");
				rebroadcastConsoleState(rhizome);
				break;
			case "/p5/a_of":
				analyses.get(0).setEnabled(val > 0.5);
				sendOsc("/panel/a_of", val, rhizome);
				break;
			case "/p5/a_hist":
				analyses.get(1).setEnabled(val > 0.5);
				sendOsc("/panel/a_hist", val, rhizome);
				break;
			case "/p5/a_seq":
				analyses.get(2).setEnabled(val > 0.5);
				sendOsc("/panel/a_seq", val, rhizome);
				break;
			case "/p5/a_blob":
				analyses.get(3).setEnabled(val > 0.5);
				sendOsc("/panel/a_blob", val, rhizome);
				break;
			case "/p5/bgsub":
				bgsub.setEnabled(val > 0.5);
				console.bgsub = bgsub.isEnabled();
				sendOsc("/panel/bgsub", val, rhizome);
				break;
			case "/p5/mask_enabled":
				mask.setEnabled(val > 0.5);
				console.mask_enabled = mask.isEnabled();
				sendOsc("/panel/mask_enabled", val, rhizome);
				break;
			case "/p5/clear_mask":
				mask.clear();
				break;
			case "/p5/add_mask_point":
				mask.setClick(msg.get(0).floatValue(), msg.get(1).floatValue());
				mask.drawDot();
				break;
			case "/p5/set_bg":
				setBg = true;
				break;
			case "/p5/screenshot":
				takeScreenshot = true;
				break;
			case "/p5/display_enabled":
				display_enabled = val > 0.5;
				console.display_enabled = display_enabled;
				sendOsc("/panel/display_enabled", val, rhizome);
				break;
			case "/p5/of_regression":
				analyses.get(0).setParams(0, val);
				console.of_regression = val;
				sendOsc("/panel/of_regression",val, rhizome);
				break;
			case "/p5/of_smoothness":
				analyses.get(0).setParams(1, val);
				console.of_smoothness = val;
				sendOsc("/panel/of_smoothness",val, rhizome);
				break;
			case "/p5/video_time":
				video.setPos(val);
				console.video_time = val;
				break;
			case "/p5/webcam":
				useWebcam = val == 1;
				webcamChanged = true;
				break;
			case "/p5/movies":
				whichMovie = round(val);
				sendOsc("/panel/movies", whichMovie, rhizome);
				sendOsc("/panel/webcam", 0, rhizome);
				whichMovieChanged = true;
				break;
			case "/conf/movies":
				sendMovieList = true;
				break;
			case "/conf/init":
				rebroadcastConsoleState(rhizome);
				break;
			case "/p5/init":
				rebroadcastConsoleState(rhizome);
				break;
			default:
				println("unexpected message received: " + msg);
		}
	}
}

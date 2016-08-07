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

		// analyses.add(new HistogramAnalysis(this));
		// analyses.add(new FrameDiffAnalysis(this));
		analyses.add(new OpticalFlowAnalysis(this));
		// analyses.add(new SequencerAnalysis(this));
		// analyses.add(new BlobAnalysis(this));

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
			// Restart analyses, since video resolution
			// might have changed
			for (BaseAnalysis analysis : analyses) {
				analysis.restart();
			}
			webcamChanged = false;
		}
	}

	private void rebroadcastConsoleState() {
		if (!useWebcam) {
			sendOsc("/panel/video_time", video.currPos(), rhizome);
		}
		OscMessage msg = new OscMessage("/panel/pr_params");
		msg.add(console.displayEnabled ? 1 : 0);
		msg.add(console.enableBGSub ? 1 : 0);
		msg.add(console.opticalFlowReg);
		msg.add(console.opticalFlowSm);
		msg.add(console.videoTime);
		msg.add(console.enableMask ? 1 : 0);
		osc.send(msg, rhizome);

		sendOsc("/conf/p5", 1, rhizome);
		sendOsc("/ctrl/webcam", useWebcam, rhizome);
		sendOsc("/ctrl/movies", whichMovie, rhizome);
		sendOsc("/ctrl/video_time", console.videoTime, rhizome);
		sendOsc("/ctrl/a_of", analyses.get(0).isEnabled(), rhizome);
		sendOsc("/ctrl/bgsub", console.enableBGSub, rhizome);
		sendOsc("/ctrl/of_regression",console.opticalFlowReg, rhizome);
		sendOsc("/ctrl/of_smoothness",console.opticalFlowSm, rhizome);
	}
	
	@Override
	public void draw() {
		if (!video.available()) {
			return;
		}
		
		PImage v = video.getImg().copy();
		updateVideoSource();

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
		// rebroadcast console state
		// Update panel only every 10 frames to reduce network traffic.
		if (frameCount % 10 == 0) {
			rebroadcastConsoleState();
			console.videoTime = video.currPos();
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
		println(msg.toString());
		float val = 0;
		if (msg.checkTypetag("f")) {
			val = msg.get(0).floatValue();
		}
		switch (msg.addrPattern()) {
			case "/sys/subscribed":
				println("subscribed to Rhizome");
				break;
			case "/p5/a_of":
				analyses.get(0).setEnabled(val > 0.5);
				break;
			case "/p5/a_hist":
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
				console.enableBGSub = bgsub.isEnabled();
				break;
			case "/p5/mask_enabled":
				mask.setEnabled(val > 0.5);
				console.enableMask = mask.isEnabled();
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
				console.displayEnabled = display_enabled;
				break;
			case "/p5/of_regression":
				analyses.get(0).setParams(0, val);
				console.opticalFlowReg = val;
				break;
			case "/p5/of_smoothness":
				analyses.get(0).setParams(1, val);
				console.opticalFlowSm = val;
				break;
			case "/p5/video_time":
				video.setPos(val);
				console.videoTime = val;
				break;
			case "/p5/webcam":
				useWebcam = val == 1;
				webcamChanged = true;
				break;
			case "/p5/movies":
				whichMovie = round(val);
				whichMovieChanged = true;
				break;
			case "/conf/movies":
				sendMovieList = true;
				break;
			default:
				println("unexpected message received: " + msg);
		}
	}
}

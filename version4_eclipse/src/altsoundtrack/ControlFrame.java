package altsoundtrack;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import analysis.BaseAnalysis;
import controlP5.CallbackListener;
import controlP5.ControlP5;
import controlP5.ScrollableList;
import controlP5.Toggle;
import controlP5.Slider;
import processing.core.PApplet;

/**
 * Control panel built using controlP5
 * See http://www.sojamo.de/libraries/controlP5/
 *
 * @author hamoid, mingness
 *
 */
public class ControlFrame extends PApplet {
	public static String TOGGLE_ANALYSIS_LABEL = "toggleAnalysis";

	private ControlP5 cp5;

	private ArrayList<BaseAnalysis> analyses;
	private boolean useWebcam;
	private File[] movies;
	private int whichMovie;
	private boolean bgsubEnabled;
	private float mLocation;
	private CallbackListener cb;
	private Slider ml; 

	private final ArrayList<Toggle> analysisToggles = new ArrayList<Toggle>();

	private boolean analysesChanged = false;
	private boolean bgsubChanged = false;
	private boolean moviesChanged = false;
	private boolean webcamChanged = false;
	private boolean movieLocationChanged = false;
	private boolean cbChanged = false;

	public void setAnalyses(ArrayList<BaseAnalysis> analyses) {
		this.analyses = analyses;
		analysesChanged = true;
	}

	public void setMovies(File[] movies, int whichMovie) {
		this.movies = movies;
		this.whichMovie = whichMovie;
		moviesChanged = true;
	}

	public void setBgSub(boolean bgsubEnabled) {
		this.bgsubEnabled = bgsubEnabled;
		bgsubChanged = true;
	}

	public void setWebcam(boolean useWebcam) {
		this.useWebcam = useWebcam;
		webcamChanged = true;
	}

	public void setMovieLocation(float mLocation) {
		this.mLocation = mLocation;
		movieLocationChanged = true;
	}

	public void setCallback(CallbackListener cb) {
		this.cb = cb;
		cbChanged = true;
	}

	public ControlFrame() {
		super();
		PApplet.runSketch(new String[] { this.getClass().getName() }, this);
	}

	@Override
	public void settings() {
		size(300, 400);
	}

	@Override
	public void setup() {
		// Window settings
		surface.setLocation(50, 100);
		surface.setTitle("AltSndtrck - Control panel");

		buildPanel();
	}

	private void buildPanel() {
		// Start ControlP5 to create the control panel
		cp5 = new ControlP5(this);

		cp5.addToggle("bgsub").setLabel("Background\nSubtraction").setPosition(20, 30)
				.setColorActive(color(210,0,100)).setColorBackground(color(100,0,0))
				.setColorForeground(color(190,0,0));

		cp5.addToggle("webcam").setLabel("Webcam").setPosition(100, 30);

		cp5.addScrollableList("movies").setPosition(100, 80).setSize(180, 100)
				.setBarHeight(20).setItemHeight(20)
				.setType(ScrollableList.LIST);
		ml = cp5.addSlider("movieLocation").setLabel("Playback\nLocation")
				.setPosition(20,350).setSize(200,20).setRange(0, 1);
	}

	/**
	 * Update is required because the main program runs on a different thread
	 * and it should not trigger visual changes on this program. Instead, it
	 * changes variables and sets a flag to indicate that an update is required.
	 * The flags are called xxxChanged.
	 */
	private void update() {
		if (bgsubChanged) {
			cp5.get(Toggle.class, "bgsub").setValue(bgsubEnabled);
			bgsubChanged = false;
		}
		if (analysesChanged) {
			for (Toggle t : analysisToggles) {
				t.remove();
			}
			int y = 80;
			// Regexp to find "TheName" in "analysis.TheNameAnalysis"
			Pattern p = Pattern.compile(".*?([A-Z].+)[A-Z].*");
			// Create a toggle button for each analysis
			for (int i = 0; i < analyses.size(); i++) {
				BaseAnalysis analysis = analyses.get(i);
				String className = analysis.getClass().getName();
				Matcher m = p.matcher(className);

				if (m.find()) {
					String name = m.group(1);
					// When pressed, toggle the "enabled" property
					// of the analysis.
					analysisToggles.add(cp5.addToggle(TOGGLE_ANALYSIS_LABEL + i)
							.setLabel(name).setPosition(20, y).setId(i));
					y += 40;
				}
			}
			analysesChanged = false;
		}
		if (webcamChanged) {
			cp5.get(Toggle.class, "webcam").setValue(useWebcam);
			webcamChanged = false;
		}
		if (moviesChanged && movies != null) {
			String[] ll = new String[movies.length];
			for (int i = 0; i < movies.length; i++) {
				ll[i] = movies[i].getName();
			}
			cp5.get(ScrollableList.class, "movies").setItems(ll).setValue(whichMovie);
			whichMovie = cp5.get(ScrollableList.class, "movies").getId();
			moviesChanged = false;
		}
		if (movieLocationChanged) {
			if (!useWebcam) {
				ml.setBroadcast(false);
				ml.setValue(mLocation);
				ml.setBroadcast(true);
			}
			movieLocationChanged = false;
		}
		if (cbChanged) {
			cp5.addCallback(cb);
			cbChanged = false;
		}
	}
	
	
	@Override
	public void draw() {
		background(40);
		update();
	}
}
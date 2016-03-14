package altsoundtrack;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import analysis.IAnalysis;
import controlP5.CallbackListener;
import controlP5.ControlP5;
import controlP5.ScrollableList;
import controlP5.Toggle;
import processing.core.PApplet;

/**
 * Control panel built using controlP5
 * See http://www.sojamo.de/libraries/controlP5/
 *
 * @author hamoid
 *
 */
public class ControlFrame extends PApplet {
	private ControlP5 cp5;

	private ArrayList<IAnalysis> analyses;
	private File[] movies;
	private CallbackListener cb;

	private final ArrayList<Toggle> analysisToggles = new ArrayList<Toggle>();

	private boolean analysesChanged = false;
	private boolean moviesChanged = false;
	private boolean cbChanged = false;

	public void setAnalyses(ArrayList<IAnalysis> analyses) {
		this.analyses = analyses;
		analysesChanged = true;
	}

	public void setMovies(File[] movies) {
		this.movies = movies;
		moviesChanged = true;
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

		cp5.addScrollableList("movies").setPosition(100, 30).setSize(180, 100)
				.setBarHeight(20).setItemHeight(20)
				.setType(ScrollableList.LIST);
	}

	/**
	 * Update is required because the main program runs on a different thread
	 * and it should not trigger visual changes on this program. Instead, it
	 * changes variables and sets a flag to indicate that an update is required.
	 * The flags are called xxxChanged.
	 */
	private void update() {
		if (analysesChanged) {
			for (Toggle t : analysisToggles) {
				t.remove();
			}
			int y = 30;
			// Regexp to find "TheName" in "analysis.TheNameAnalysis"
			Pattern p = Pattern.compile(".*?([A-Z].+)[A-Z].*");
			// Create a toggle button for each analysis
			for (IAnalysis analysis : analyses) {
				String className = analysis.getClass().getName();
				Matcher m = p.matcher(className);

				if (m.find()) {
					String name = m.group(1);
					// When pressed, toggle the "enabled" property
					// of the analysis.
					analysisToggles.add(cp5.addToggle(analysis, "enabled")
							.setLabel(name).setPosition(20, y));
					y += 40;
				}
			}
			analysesChanged = false;
		}
		if (moviesChanged && movies != null) {
			String[] ll = new String[movies.length];
			for (int i = 0; i < movies.length; i++) {
				ll[i] = movies[i].getName();
			}
			cp5.get(ScrollableList.class, "movies").setItems(ll);
			moviesChanged = false;
		}
		if (cbChanged) {
			cp5.addCallback(cb);
			cbChanged = false;
		}

	}

	@Override
	public void draw() {
		update();
		background(40);
	}
}
package altsoundtrack;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import analysis.IAnalysis;
import controlP5.CallbackListener;
import controlP5.ControlP5;
import controlP5.ScrollableList;
import processing.core.PApplet;

public class ControlFrame extends PApplet {
	private ControlP5 cp5;
	private final ArrayList<IAnalysis> analyses;
	private final File[] movies;
	private final CallbackListener cb;

	public ControlFrame(ArrayList<IAnalysis> analyses, File[] movies,
			CallbackListener cb) {
		super();
		PApplet.runSketch(new String[] { this.getClass().getName() }, this);
		this.analyses = analyses;
		this.movies = movies;
		this.cb = cb;
	}

	@Override
	public void settings() {
		size(300, 400);
	}

	@Override
	public void setup() {
		// Window settings
		surface.setLocation(10, 10);
		surface.setTitle("AltSndtrck - Control panel");

		buildPanel();
	}

	private void buildPanel() {
		// Start ControlP5 for creating an interface
		cp5 = new ControlP5(this);
		cp5.addCallback(cb);

		/*
		 * See http://www.sojamo.de/libraries/controlP5/
		 * Some examples.
		 * cp5.addButton("Save Frame").plugTo(parent, "saveFrame");
		 * cp5.addNumberbox("seed").plugTo(parent, "seed").setRange(0, 360)
		 * .setValue(1).setSize(100, 20);
		 * cp5.addSlider("speed").plugTo(parent, "speed").setRange(0, 0.1f)
		 * .setValue(0.01f).setSize(200, 20);
		 */

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
				cp5.addToggle(analysis, "enabled").setLabel(name).linebreak();
			}
		}

		cp5.addScrollableList("movies").setPosition(100, 30).setSize(180, 100)
				.setBarHeight(20).setItemHeight(20)
				.setType(ScrollableList.LIST);

		String[] ll = new String[movies.length];
		for (int i = 0; i < movies.length; i++) {
			ll[i] = movies[i].getName();
		}
		cp5.get(ScrollableList.class, "movies").setItems(ll);
	}

	@Override
	public void draw() {
		background(40);
	}
}
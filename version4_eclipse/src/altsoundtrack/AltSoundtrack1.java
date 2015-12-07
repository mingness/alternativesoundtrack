package altsoundtrack;

import java.util.ArrayList;

import processing.core.PApplet;
import analysis.TestAnalysis1;

public class AltSoundtrack1 extends PApplet {
	TestAnalysis1 ta;

	@Override
	public void settings() {
		size(600, 600);
		// fullScreen();
	}

	@Override
	public void setup() {
		ta = new TestAnalysis1();
		println(ta.getX());
	}

	@Override
	public void draw() {
		background(frameCount % 256);
	}

	public static void main(String[] args) {
		ArrayList<String> options = new ArrayList<String>();
		options.add("--bgcolor=#000000");
		// options.add("--display=0");
		// options.add("--full-screen");
		options.add("--hide-stop");
		options.add("altsoundtrack.AltSoundtrack1"); // com.x.Class
		String[] optionsArray = new String[options.size()];
		optionsArray = options.toArray(optionsArray);

		PApplet.main(optionsArray);

	}

}

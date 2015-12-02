package altsoundtrack;

import java.util.ArrayList;

import processing.core.PApplet;

public class AltSoundtrack2 extends PApplet {
	@Override
	public void settings() {
		size(600, 600);
	}

	@Override
	public void setup() {
	}

	@Override
	public void draw() {
		background(random(255));
	}

	public static void main(String[] args) {
		ArrayList<String> options = new ArrayList<String>();
		options.add("--bgcolor=#000000");
		// options.add("--display=0");
		options.add("--hide-stop");
		// options.add("--full-screen");
		options.add("altsoundtrack.AltSoundtrack2"); // com.x.Class
		String[] optionsArray = new String[options.size()];
		optionsArray = options.toArray(optionsArray);

		PApplet.main(optionsArray);

	}

}

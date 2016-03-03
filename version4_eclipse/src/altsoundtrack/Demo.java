package altsoundtrack;

import java.util.ArrayList;

import processing.core.PApplet;

/*
 * Example program to show how to set things up
 */
public class Demo extends PApplet {

	@Override
	public void settings() {
		size(600, 600);
		// fullScreen();
	}

	@Override
	public void setup() {
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
		options.add("altsoundtrack.Demo"); // com.x.Class
		String[] optionsArray = new String[options.size()];
		optionsArray = options.toArray(optionsArray);

		PApplet.main(optionsArray);

	}

}

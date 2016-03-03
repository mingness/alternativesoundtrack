package altsoundtrack.check;

import processing.core.PApplet;
import processing.video.Capture;

public class Check3_Webcam extends PApplet {

	// Webcam
	Capture video;

	@Override
	public void settings() {
		size(640, 480); //this needs to match your cam settings
	}

	@Override
	public void setup() {

		// WEBCAM ----
		video = new Capture(this, width, height, 30);
		video.start();
	}


	@Override
	public void draw() {
		if (!video.available()) {
			return;
		}
		video.read();
		image(video, 0, 0, width, height);
	}

	public static void main(String[] args) {
		String[] options = { "--bgcolor=#000000", "--hide-stop",
		"altsoundtrack.Check3_Webcam" };

		PApplet.main(options);
	}

}

package altsoundtrack.video;

import processing.core.PApplet;
import processing.core.PImage;
import processing.video.Capture;

/**
 * Webcam player. See AltMovie for more details.
 *
 * @author hamoid
 *
 */
public class AltMovieWebcam extends AltMovie {
	private Capture v;

	public AltMovieWebcam(PApplet p5) {
		this.p5 = p5;
	}

	@Override
	public void play(Object id) {
		String[] cameras = Capture.list();
		// PApplet.printArray(cameras);

		v = new Capture(p5, cameras[(int) id]);
		v.start();
	}

	@Override
	public void stop() {
		v.stop();
		v = null;
	}

	@Override
	public boolean available() {
		if (v != null && v.available()) {
			v.read();
			v.loadPixels();
			return true;
		}
		return false;
	}

	@Override
	public void display() {
		p5.image(v, 0, 0, p5.width, p5.height);
	}

	@Override
	public PImage getImg() {
		return v;
	}

	@Override
	public int getFrameRate() {
		return Math.round(v.frameRate);
	}
}

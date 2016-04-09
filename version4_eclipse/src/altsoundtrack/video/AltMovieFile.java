package altsoundtrack.video;

import java.io.File;

import processing.core.PApplet;
import processing.core.PImage;
import processing.video.Movie;

/**
 * File movie player. See AltMovie for more details.
 *
 * @author hamoid
 *
 */
public class AltMovieFile extends AltMovie {
	private Movie v;

	public AltMovieFile(PApplet p5) {
		this.p5 = p5;
	}

	@Override
	public void play(Object path) {
		File f = new File((String) path);

		v = new Movie(p5, f.getAbsolutePath());
		v.loop();
		v.volume(0);
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

	@Override
	public float currPos() {
		return v.time() / v.duration();
	}

	@Override
	public void setPos(float mLocation) {
		v.jump(mLocation*v.duration());
	}
}

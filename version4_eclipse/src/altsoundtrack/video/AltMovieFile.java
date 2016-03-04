package altsoundtrack.video;

import java.io.File;

import altsoundtrack.Config;
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
	private final Movie v;

	public AltMovieFile(PApplet p5, Config cfg, int movieId) {
		this.p5 = p5;

		File f = new File(p5.sketchPath() + File.separator + cfg.moviePath
				+ File.separator + cfg.movieFilenames[1]);

		v = new Movie(p5, f.getAbsolutePath());
		v.loop();
		v.volume(0);
	}

	@Override
	public boolean available() {
		if (v.available()) {
			v.read();
			v.loadPixels();
			return true;
		} else {
			return false;
		}
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
}

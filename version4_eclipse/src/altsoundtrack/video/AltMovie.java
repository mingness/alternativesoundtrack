package altsoundtrack.video;

import processing.core.PApplet;
import processing.core.PImage;

/**
 * Generic Movie class that must be extended by AltMovieFile and AltMovieWebcam.
 * Thanks to this the Main program can ignore the differences between Movie
 * and Capture.
 *
 * @author hamoid
 *
 */
public class AltMovie {
	protected PApplet p5;

	/**
	 * Returns true if a new frame is available from the video
	 *
	 * @return true when a frame is available
	 */
	public boolean available() {
		return false;
	}

	/**
	 * Displays the current frame on the screen
	 */
	public void display() {
	}

	public void play(Object... args) {
	}

	public void stop() {
	}

	/**
	 * Provides the current bitmap to be analyzed
	 *
	 * @return current video image as PImage
	 */
	public PImage getImg() {
		return null;
	}

	/**
	 * Return playback frameRate as rounded integer
	 *
	 * @return frame rate as integer
	 */
	public int getFrameRate() {
		return 0;
	}

	/**
	 * Returns the normalized playback time or 0 for webcam streams
	 *
	 * @return Playback time as normalized float
	 */
	public float currPos() {
		return 0;
	}

	/**
	 * Set the normalized playback time
	 */
	public void setPos(float mLocation) {
	}
}

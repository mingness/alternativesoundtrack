package altsoundtrack;

import java.io.File;

import netP5.NetAddress;
import oscP5.OscMessage;
import oscP5.OscP5;
import processing.core.PApplet;
import processing.video.Movie;

public class AltSoundtrack2 extends PApplet {
	// OSC
	private OscP5 osc;
	private NetAddress supercollider;

	// Config
	private Config cfg;
	private ConfigManager cfgManager;

	// Video
	Movie video;

	// Analysis
	int numBaseFreqs = 7;
	int numOctaves = 7;
	int numWaves = numBaseFreqs * numOctaves;
	float[] histLeft = new float[numWaves];
	float[] histRight = new float[numWaves];
	float H, S, B;
	float greyThreshold = 25;
	int numPixels;
	int cap = 0;
	float power = 2;

	/*
	 * (non-Javadoc)
	 * @see processing.core.PApplet#settings()
	 */
	@Override
	public void settings() {
		size(600, 600);
		// fullScreen();
	}

	/*
	 * (non-Javadoc)
	 * @see processing.core.PApplet#setup()
	 */
	@Override
	public void setup() {
		// Config
		cfgManager = new ConfigManager("altSoundtrackConfig.json");
		if (cfgManager.configExists()) {
			cfg = cfgManager.load();
		} else {
			cfg = new Config();
		}

		// OSC
		osc = new OscP5(this, 12000);
		supercollider = new NetAddress(cfg.supercolliderIp,
				cfg.supercolliderPort);

		File f = new File(sketchPath() + File.separator + cfg.moviePath
				+ File.separator + cfg.movieFilenames[0]);

		// Movie
		video = new Movie(this, f.getAbsolutePath());
		video.loop();
		video.volume(0);

		frameRate(cfg.frameRate);
	}

	/*
	 * (non-Javadoc)
	 * @see processing.core.PApplet#draw()
	 */
	@Override
	public void draw() {
		if (!video.available()) {
			return;
		}
		video.read();
		video.loadPixels();

		image(video, 0, 0);

		numPixels = video.width * video.height;

		// Calculate the histogram
		for (int i = 0; i < numPixels; i++) {
			int thisColor = video.pixels[i];

			S = saturation(thisColor); // min threshold? otherwise grey
			if (S > greyThreshold) {

				H = hue(thisColor);

				float mappedH;
				if (H > 212.5 || H <= 10) {
					mappedH = 0; // red
				} else if (H <= 32) {
					mappedH = 1; // orange
				} else if (H <= 53) {
					mappedH = 2; // yellow
				} else if (H <= 106.25) {
					mappedH = 3; // green
				} else if (H <= 149) {
					mappedH = 4; // cyan
				} else if (H <= 184) {
					mappedH = 5; // blue
				} else {
					mappedH = 6; // violet
				}

				B = brightness(thisColor);
				// ignore black black, and white white, borders can max
				// normalization
				if (B >= cap && B <= 255 - cap) {
					float mappedB = map(B, cap, 255 - cap, 0, numOctaves - 1);

					int thisIndex = round(numBaseFreqs * mappedB + mappedH);
					if ((i % width) < (width / 2)) {
						// left
						histLeft[thisIndex] += 1;
					} else {
						// right
						histRight[thisIndex] += 1;
					}
				} // B
			} // if S
		} // for

		// Find the largest value in the histogram
		float maxvalLeft = 0;
		float maxvalRight = 0;
		for (int i = 0; i < numWaves; i++) {
			if (histLeft[i] > maxvalLeft) {
				maxvalLeft = histLeft[i];
			}
			if (histRight[i] > maxvalRight) {
				maxvalRight = histRight[i];
			}
		}

		// Normalize the histogram to values between 0 and 0.5 (normalization to
		// 1 causes distortion) and power the hist
		for (int i = 0; i < numWaves; i++) {
			histLeft[i] = pow(histLeft[i] / maxvalLeft * 0.5f, power);
			histRight[i] = pow(histRight[i] / maxvalRight * 0.5f, power);
		}

		// MOVIE ----
		// Draw progress bar
		stroke(0, 150, 0);
		strokeWeight(4);
		line(0, height - 2, video.time() / video.duration() * width, height - 2);
		// ---- MOVIE

		// Draw half of the histogram
		stroke(255);
		strokeWeight(1);
		for (int i = 0; i < numWaves; i++) {
			float x = map(i, 0, numWaves, 0, width / 2);
			line(x, height, x, height - histLeft[i] * 200);
			x = map(i, 0, numWaves, width / 2, width);
			line(x, height, x, height - histRight[i] * 200);
		}

		// Send OSC msg to Supercollider
		OscMessage myMessage = new OscMessage("/hist");
		myMessage.add(histLeft);
		myMessage.add(histRight);
		osc.send(myMessage, supercollider);
	}

	public static void main(String[] args) {
		String[] options = { "--bgcolor=#000000", "--hide-stop",
		"altsoundtrack.AltSoundtrack2" };

		PApplet.main(options);
	}

}

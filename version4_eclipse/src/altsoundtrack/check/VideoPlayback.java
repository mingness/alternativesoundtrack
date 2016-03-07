package altsoundtrack.check;

import java.io.File;

import altsoundtrack.Config;
import altsoundtrack.ConfigManager;
import processing.core.PApplet;
import processing.video.Movie;

public class VideoPlayback extends PApplet {

	// Config
	private Config cfg;
	private ConfigManager cfgManager;

	// Video
	Movie video;

	@Override
	public void settings() {
		size(600, 600);
		// fullScreen();
	}

	@Override
	public void setup() {
		// Config
		cfgManager = new ConfigManager("altSoundtrackConfig.json");
		if (cfgManager.configExists()) {
			cfg = cfgManager.load();
		} else {
			cfg = new Config();
			cfgManager.save(cfg);
		}

		File path = new File(cfg.moviePath);
		if (path.isDirectory()) {
			File[] movies = path.listFiles();

			// Movie
			video = new Movie(this, movies[0].getAbsolutePath());
			video.loop();
		} else {
			println("No movies found at", cfg.moviePath);
		}

		frameRate(cfg.frameRate);
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
				"altsoundtrack.check.VideoPlayback" };

		PApplet.main(options);
	}

}

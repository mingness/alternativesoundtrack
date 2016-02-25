package altsoundtrack;

import java.io.File;

import processing.core.PApplet;
import processing.video.Movie;

public class Check2_VideoPlayback extends PApplet {

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

		File f = new File(sketchPath() + File.separator + cfg.moviePath
				+ File.separator + cfg.movieFilenames[0]);

		// Movie
		video = new Movie(this, f.getAbsolutePath());
		video.loop();
		
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
		"altsoundtrack.Check2_VideoPlayback" };

		PApplet.main(options);
	}

}

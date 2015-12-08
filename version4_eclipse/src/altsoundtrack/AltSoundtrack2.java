package altsoundtrack;

import netP5.NetAddress;
import oscP5.OscMessage;
import oscP5.OscP5;
import processing.core.PApplet;
import analysis.TestAnalysis1;

public class AltSoundtrack2 extends PApplet {
	private OscP5 osc;
	private NetAddress supercollider;
	private Config cfg;
	@SuppressWarnings("unused")
	private ConfigManager cfgManager;

	private final float[] L = new float[15];
	private final float[] R = new float[15];

	private TestAnalysis1 ta;

	@Override
	public void settings() {
		size(600, 600);
		// fullScreen();
	}

	@Override
	public void setup() {
		// Set up configuration
		cfg = new Config();
		cfgManager = new ConfigManager("altSoundtrackConfig.json", cfg);

		osc = new OscP5(this, 12000);
		supercollider = new NetAddress(cfg.supercolliderIp,
				cfg.supercolliderPort);

		frameRate(cfg.frameRate);

		// just an example of a class, will be gone
		ta = new TestAnalysis1();

		// All prints at the end, because instantiating OSC prints
		// a lot of stuff to the console.
		println(ta.getX());
		printArray(cfg.movieFilenames);
	}

	@Override
	public void draw() {
		background(0);
		fill(255);
		text("Sending noise to Supercollider", 20, frameCount % height);

		for (int i = 0; i < L.length; i++) {
			L[i] = 0.5f * abs(noise(i * 0.08f, 0.1f, frameCount * 0.1f) - 0.5f);
			R[i] = 0.5f * abs(noise(i * 0.08f, 0.5f, frameCount * 0.1f) - 0.5f);
		}

		OscMessage msg = new OscMessage("/starhit");
		msg.add(L);
		msg.add(R);
		osc.send(msg, supercollider);

	}

	public static void main(String[] args) {
		String[] options = { "--bgcolor=#000000", "--hide-stop",
				"altsoundtrack.AltSoundtrack2" };

		PApplet.main(options);
	}

}

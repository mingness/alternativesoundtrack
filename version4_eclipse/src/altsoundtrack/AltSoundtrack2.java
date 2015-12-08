package altsoundtrack;

import java.util.ArrayList;

import netP5.NetAddress;
import oscP5.OscMessage;
import oscP5.OscP5;
import processing.core.PApplet;
import analysis.TestAnalysis1;

public class AltSoundtrack2 extends PApplet {
	private OscP5 osc;
	private NetAddress supercollider;

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
		frameRate(30);

		osc = new OscP5(this, 12000);
		supercollider = new NetAddress("127.0.0.1", 57120);

		ta = new TestAnalysis1();
		println(ta.getX());
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
		ArrayList<String> options = new ArrayList<String>();
		options.add("--bgcolor=#000000");
		// options.add("--display=0");
		// options.add("--full-screen");
		options.add("--hide-stop");
		options.add("altsoundtrack.AltSoundtrack2");
		String[] optionsArray = new String[options.size()];
		optionsArray = options.toArray(optionsArray);

		PApplet.main(optionsArray);

	}

}

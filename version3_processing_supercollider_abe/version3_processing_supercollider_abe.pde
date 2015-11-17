import oscP5.*;
import netP5.*;

OscP5 osc;
NetAddress supercollider;

float[] L = new float[15];
float[] R = new float[15];

void setup() {
  frameRate(30);

  osc = new OscP5(this, 12000);
  supercollider = new NetAddress("127.0.0.1", 57120);
}
void draw() {
  for(int i=0; i<L.length; i++) {
    L[i] = 0.5 * abs(noise(i * 0.08, 0.1, frameCount * 0.1) - 0.5);
    R[i] = 0.5 * abs(noise(i * 0.08, 0.5, frameCount * 0.1) - 0.5);
  }
 
  OscMessage msg = new OscMessage("/starhit");
  msg.add(L);
  msg.add(R);
  osc.send(msg, supercollider);
}
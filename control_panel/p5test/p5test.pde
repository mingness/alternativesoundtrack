import oscP5.*;
import netP5.*;

int serverOSCPort = 9000;
int appPort = 9001;
String serverIP = "127.0.0.1";
int band = -1;
float val;
TestListener listener;

OscP5 oscP5;
NetAddress rhizomeLocation;

void setup() {
  size(600, 600);
  background(255);
  noStroke();
  oscP5 = new OscP5(this, appPort);
  listener = new TestListener();
  oscP5.addListener(listener);
  rhizomeLocation = new NetAddress(serverIP, serverOSCPort);
  
  // Subscribe to receive messages from /drawing
  OscMessage subscribeMsg = new OscMessage("/sys/subscribe");
  subscribeMsg.add(appPort);
  subscribeMsg.add("/slider");
  oscP5.send(subscribeMsg, rhizomeLocation);
}

void draw() {
  if(band > -1) {
    fill(255);
    rect(0, 5 + band * 20, width, 15);
    fill(0);
    rect(0, 5 + band * 20, width * val, 15);
    band = -1;
  }
}
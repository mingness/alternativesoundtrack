import java.util.concurrent.ConcurrentHashMap;
import java.util.*;
import oscP5.*;
import netP5.*;

/*
  This example visualizes and lets you control
  a number of sliders. Note that when clicking
  on the screen, this sketch does not update
  any values directly, but sends the click data
  to Rhizome, which then sends the data back
  to Processing, which then updates the graphics.
  
  The reason to do it this way is that then, the
  sketch does not care who is actually sending
  those updated values. They may be triggered by
  the user clicking on the Processing sketch,
  but they could also come from Supercollider,
  or from the browser (maybe running on a phone
  or o tablet).
  
  The updated slider values are stored in a
  ConcurrentHashMap, which is just like a HashMap,
  but avoids concurrency problems. If you use a
  normal HashMap, and you try to read this HashMap
  (to draw the sliders on the screen) while at
  the same time you receive an OSC message, which
  tries to update the HashMap, the program crashes.
  The ConcurrentHashMap makes sure that only 
  reading and writing does not happen at the same
  time.
*/

int bands = 4;

int rhizomeOSCPort = 9000;
int listenOnPort = 9001;
String rhizomeIP = "127.0.0.1";
NetAddress rhizome;

OscP5 oscP5;
ConcurrentHashMap<Integer, Float> values = new ConcurrentHashMap<Integer, Float>();
OscListener listener;


void setup() {
  size(600, 600, P2D);
  background(#880088);
  noStroke();
  
  oscP5 = new OscP5(this, listenOnPort);
  listener = new OscListener();
  oscP5.addListener(listener);
  
  rhizome = new NetAddress(rhizomeIP, rhizomeOSCPort);

  // Subscribe to Rhizome. 
  // Ask it to send us /slider messages on port 9001
  OscMessage subscribeMsg = new OscMessage("/sys/subscribe");
  subscribeMsg.add(listenOnPort);
  subscribeMsg.add("/slider");
  oscP5.send(subscribeMsg, rhizome);
}

void draw() {
  int h = height / bands;
  // We may have received a few messages since
  // the last time draw() ran.
  // Go over each message, redrawing the right
  // band.
  Iterator it = values.entrySet().iterator();
  while (it.hasNext()) {
    ConcurrentHashMap.Entry<Integer, Float> v = (ConcurrentHashMap.Entry)it.next();
    float y = h * v.getKey();
    float w = width * v.getValue();
        
    // draw the band with the right width
    fill(255);
    rect(0, y, w, h * 0.95);
    
    // clear the right side of the band
    fill(#880088);
    rect(w, y, width - w, h * 0.95);

    // Remove the value from the HashMap 
    it.remove();
  }
}

void mousePressed() {
  int band = (int)(bands * mouseY / (float)height);
  float val = mouseX / (float)width;
 
  // Send a message to Rhizome including which
  // band was clicked, and its new value
  OscMessage sliderMsg = new OscMessage("/slider");
  sliderMsg.add(band);
  sliderMsg.add(val);
  oscP5.send(sliderMsg, rhizome);
}

// It's good to implement an OscEventListener.
// It's a bit more verbose than oscEvent(), but 
// this way OSC error messages are much clearer. 
// Without it, errors are catched inside oscP5 
// and we have no clue of knowing what's going 
// wrong.

class OscListener implements OscEventListener {

  public void oscEvent(OscMessage msg) {
    if (msg.addrPattern().equals("/sys/subscribed")) {
      println("subscribed successfully");
    } else if (msg.addrPattern().equals("/slider")) {
      int band = (int)msg.get(0).floatValue();
      float val = msg.get(1).floatValue(); 
      values.put(band, val);
    } else {
      println("unexpected message received " + msg);
    }
  }

  public void oscStatus(OscStatus status) {
    println("osc status : "+status.id());
  }
}
// hues converted to 7 notes of major scale
// red, orange, yellow, green, cyan, blue, violet
// https://en.wikipedia.org/wiki/Major_scale

import processing.video.*;
import ddf.minim.*;
import ddf.minim.ugens.*;
// SUPERCOLLIDER ----
import oscP5.*;
import netP5.*;
// ---- SUPERCOLLIDER

//-------------------------------------
// User Input
// put here your custom mp4 files in movieLocation.
// power is the exponent for the amplitude of the histogram; allows 
//    you to adjust relative volumes
// cap is the buffer on either extreme of the histogram bins that you 
//    will ignore in playback.

//String movieLocation = "../../Wonderfalls.s01e03.mp4";
//float power = 1;
//int cap = 0;

String movieLocation = "../../Coyote_Roadrunner.mp4";
float power = 2;
int cap = 0;

//String movieLocation = "../../into_the_colorflow.mp4";
//float power = 1;
//int cap = 0;

//String movieLocation = "../../An_Optical_Poem.mp4";
//float power = 0.5;
//int cap = 0;

//-------------------------------------

//// Map notes, to keys to scales
// 1. the frequencies of notes, 2 octaves long, so we can assemble the scale from these notes
// 2. major or minor? the delta-index between notes
// 3. the actual notes to assign to baseFreqs

int numBaseFreqs = 7;
int numOctaves = 7;
int numWaves = numBaseFreqs*numOctaves;
float H;
float S;
float B;
float greyThreshold = 25;
int numPixels;

//// WEBCAM ----
//Capture video;
//// ---- WEBCAM

// MOVIE ----
Movie video;
// ---- MOVIE

// SUPERCOLLIDER ----
OscP5 oscP5;
NetAddress supercollider;
String addr = "/starhit";
// ---- SUPERCOLLIDER

//--------------------------------------------
void setup() {
//  // WEBCAM ----
//  size(640, 480);
//  // ---- WEBCAM

  // MOVIE ----
  size(640, 352);
  // ---- MOVIE



//  // WEBCAM ----
//  video = new Capture(this, width, height, 30);
//  //OR
////  String[] cameras = Capture.list();
////  printArray(cameras);
////  video = new Capture(this, cameras[3]);
//  // Start capturing the images from the camera
//  video.start();
//  // ---- WEBCAM
    
  // MOVIE ----
  //converted from avi with avconv -i ThisVideo.avi -codec copy ThisVideo.avi
    video = new Movie(this, movieLocation);
    video.loop();
    video.volume(0);
  //  video.read();
  //  println(video.width, video.height);
  // ---- MOVIE

  background(0);

// SUPERCOLLIDER ----
  oscP5 = new OscP5(this,12000);
  supercollider = new NetAddress("127.0.0.1",57120);
// ---- SUPERCOLLIDER
}

//--------------------------------------------
void draw() {
  if (video.available()) {
    // When using video to manipulate the screen, use video.available() and
    // video.read() inside the draw() method so that it's safe to draw to the screen
    background(0);
    video.read(); // Read the new frame from the camera
    video.loadPixels(); // Make its pixels[] array available
    image(video,0,0,width, height);

    float[] histLeft = new float[numWaves];
    float[] histRight = new float[numWaves];

    // Calculate the histogram
    for (int i=0; i<video.width*video.height; i++) {
        color thisColor = video.pixels[i];

        S = saturation(thisColor); //min threshold? otherwise grey
      if (S > greyThreshold) {

        // white has 0 hue - arbitrary weighting - normalize somehow with brightness or saturation?
        H = hue(thisColor);
        //http://www.rapidtables.com/web/color/RGB_Color.htm
        // hue is red to red, 0-255. 255/6 = 42.5
        // red:0, yellow:42.5, green:85, cyan:127.5, blue:170, magenta: 212.5
        // orange: 10-32, violet: 191.25
        // BINS - what is predominant color
        // red: 212.5-0-10, orange: 10-32 (center half), yellow: 32-53 (half centered on yellow), 
        // green:53-106.25 (boundary at half), cyan: 106.25-149 (boundary at half), 
        // blue: 149-184 (boundary third in), violet: 184-212.5 (until magenta)
        // hue from red to violet is linear 0-255. 
        
        float mappedH;
        if (H>212.5 || H<=10) {
          mappedH = 0; //red
        } else if (H>10 && H<=32) {
          mappedH = 1; //orange
        } else if (H>32 && H<=53) {
          mappedH = 2; //yellow
        } else if (H>53 && H<=106.25) {
          mappedH = 3; //green
        } else if (H>106.25 && H<=149) {
          mappedH = 4; //cyan
        } else if (H>149 && H<=184) {
          mappedH = 5; //blue
        } else {
          mappedH = 6; //violet
        }

        B = brightness(thisColor);
        // ignore black black, and white white, borders can max normalization
        if (B>=cap && B<=255-cap) {
          float mappedB = map(B,cap,255-cap,0,numOctaves-1);
          
          int thisIndex= round(numBaseFreqs*mappedB+mappedH);
          if ((i % width) < (width/2)) {
            //left
            histLeft[thisIndex] += 1;  
          } else {
            //right
            histRight[thisIndex] += 1;  
          }            
        } // B 
      } //if S
    } //for
    
    // Find the largest value in the histogram
    float maxvalLeft = 0;
    float maxvalRight = 0;
    for (int i=0; i<histLeft.length; i++) {
      if(histLeft[i] > maxvalLeft) {
        maxvalLeft = histLeft[i];
      }  
      if(histRight[i] > maxvalRight) {
        maxvalRight = histRight[i];
      }  
    }
//    println(maxval);
    
    // Normalize the histogram to values between 0 and 0.5 (normalization to 1 causes distortion)
    // and power the hist
    for (int i=0; i<histLeft.length; i++) {
      histLeft[i] = histLeft[i]/maxvalLeft/2;
      histLeft[i] = pow(histLeft[i],power);
      histRight[i] = histRight[i]/maxvalRight/2;
      histRight[i] = pow(histRight[i],power);
    }
    
  // MOVIE ----
    // Draw progress bar
    stroke(0,150,0);  
    strokeWeight(4);  
    line(0,height-2,video.time()/video.duration()*width,height-2);
  // ---- MOVIE
    
    // Draw half of the histogram (skip every second value)
    stroke(255);
    strokeWeight(1);
    for (int i=0; i<histLeft.length; i++) {
      int x = floor(map(i,0,histLeft.length,0,width/2));
      line(x, height, x, height-floor(histLeft[i]*200));
    }
    for (int i=0; i<histRight.length; i++) {
      int x = floor(map(i,0,histRight.length,width/2,width));
      line(x, height, x, height-floor(histRight[i]*200));
    }    
    
// SUPERCOLLIDER ----
    OscMessage myMessage = new OscMessage(addr);
        
    for (int i=0; i<histLeft.length; i++) {
      myMessage.add(histLeft[i]); 
    }
    for (int i=0; i<histRight.length; i++) {
      myMessage.add(histRight[i]); 
    }
  
    /* send the message */
    oscP5.send(myMessage, supercollider); 
// ---- SUPERCOLLIDER 

    
  
  } // if video available
}

void mousePressed() {
  // MOVIE ----
//  println(float(mouseX)/float(width), video.duration()); // debug ****************
  video.jump(float(mouseX)/float(width)*video.duration());
  // ---- MOVIE
}

// convert colors to tones - create buckets, 12 tones of chromatic scale,
// 6 colors R Y G C B M half to create 12 colors
// dark and light relates to harmonics (or amplitude)

// create distribution, 0-11 hues, * 15 octaves = 180
// or, 204.8 hues * 10 octaves

// later, translate colors between violet and red as a lin comb

// OR dominate color relates to key, and other colors relate to chords
// pca - giving angle, provides amplitude (or harmonics). bending of tone?

//toxic libs

// later, frame differencing to give... high notes? melodic 
//optical flow

//openCV - computer vision

// thought experiment
//what if have red circle on green background, moving in a circle on the screen. 
//Then, the relative amount of colors don't change, or brightness
// pca may change, and frame differencing would give signal. 
// would somehow expect the tone to cycle. could also be amplitude, harmonics, 

//popcorn.js




import processing.video.*;
import ddf.minim.*;
import ddf.minim.ugens.*;

Minim minim;
AudioOutput out;

// http://www.phy.mtu.edu/~suits/notefreqs.html
float[] baseFreqs = {16.35, 17.32, 18.35, 19.45, 20.60, 21.83, 
                    23.12, 24.50, 25.96, 27.50, 29.14, 30.87};
int numOctaves = 7;
int numWaves = baseFreqs.length*numOctaves;
Oscil[] oscils;

float H;
float S;
float B;
int numPixels;

// WEBCAM ----
Capture video;
// ---- WEBCAM
//// MOVIE ----
//Movie video;
//// ---- MOVIE

//--------------------------------------------
void setup() {
  // WEBCAM ----
  size(640, 480);
  // ---- WEBCAM
//  // MOVIE ----
//  size(640, 352);
//  // ---- MOVIE
  minim = new Minim(this);

  // WEBCAM ----
  video = new Capture(this, width, height, 30);
  //OR
//  String[] cameras = Capture.list();
//  printArray(cameras);
//  video = new Capture(this, cameras[3]);
  // Start capturing the images from the camera
  video.start();
  // ---- WEBCAM
    
//  // MOVIE ----
//  //converted from avi with avconv -i ThisVideo.avi -codec copy ThisVideo.avi
//    video = new Movie(this, "../../ThisVideo.mp4");
//    video.loop();
//    video.volume(0);
//  //  video.read();
//  //  println(video.width, video.height);
//  // ---- MOVIE

  background(0);
  
  // use the getLineOut method of the Minim object to get an AudioOutput object
  out = minim.getLineOut();
  
//  println(baseFreqs); //debug
//  println(numWaves); //debug
  oscils = new Oscil[numWaves];
  float freq;
  for(int i = 0; i < numWaves; i++){
    freq = baseFreqs[i % baseFreqs.length] * pow(2,floor(i/baseFreqs.length));
    println(freq); //debug
    oscils[i] = new Oscil(freq, 0.01f, Waves.SINE );
    oscils[i].patch( out );
  }

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

    float[] hist = new float[numWaves];

    // Calculate the histogram
    for (int i=0; i<video.width*video.height; i++) {
        color thisColor = video.pixels[i];
        // white has 0 hue - arbitrary weighting - normalize somehow with brightness or saturation?
        H = hue(thisColor);
//        S = saturation(thisColor); //later adds baseline
        B = brightness(thisColor);
        float mappedH = map(H,0,255,0,baseFreqs.length-1);
        int thisIndex= round(baseFreqs.length*map(B,0,255,0,numOctaves-1)+mappedH);
//        println(video.width*video.height,octaveWidth,thisIndex); //debug
        hist[thisIndex] += 1;         
    } 
    
    // Find the largest value in the histogram
    float maxval = 0;
    for (int i=0; i<hist.length; i++) {
      if(hist[i] > maxval) {
        maxval = hist[i];
      }  
    }
//    println(maxval);
    
    // Normalize the histogram to values between 0 and 1
    for (int i=0; i<hist.length; i++) {
      hist[i] = hist[i]/maxval;
    }
    
    // Draw half of the histogram (skip every second value)
    stroke(255);
    for (int i=0; i<hist.length; i++) {
      int x = floor(map(i,0,hist.length,0,width));
      line(x, height, x, height-floor(hist[i]*200));
    }
    
    for(int i = 0; i < numWaves; i++){
      oscils[i].setAmplitude( hist[i]/numWaves );
    }

  //  wave.setAmplitude( amp );
  //  wave.setFrequency( freq );
    
  
  } // if video available
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




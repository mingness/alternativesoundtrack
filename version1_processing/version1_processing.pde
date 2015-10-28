// hues converted to 7 notes of major scale
// red, orange, yellow, green, cyan, blue, violet
// https://en.wikipedia.org/wiki/Major_scale

import processing.video.*;
import ddf.minim.*;
import ddf.minim.ugens.*;

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

Minim minim;
Summer sum;
AudioOutput out;

//// Map notes, to keys to scales
// 1. the frequencies of notes, 2 octaves long, so we can assemble the scale from these notes
// 2. major or minor? the delta-index between notes
// 3. the actual notes to assign to baseFreqs
float[] allFreqs = {Frequency.ofPitch( "C0" ).asHz(),
                    Frequency.ofPitch( "C#0" ).asHz(),
                    Frequency.ofPitch( "D0" ).asHz(),
                    Frequency.ofPitch( "D#0" ).asHz(),
                    Frequency.ofPitch( "E0" ).asHz(),
                    Frequency.ofPitch( "F0" ).asHz(),
                    Frequency.ofPitch( "F#0" ).asHz(),
                    Frequency.ofPitch( "G0" ).asHz(),
                    Frequency.ofPitch( "G#0" ).asHz(),
                    Frequency.ofPitch( "A0" ).asHz(),
                    Frequency.ofPitch( "A#0" ).asHz(),
                    Frequency.ofPitch( "B0" ).asHz(),
                    Frequency.ofPitch( "C1" ).asHz(),
                    Frequency.ofPitch( "C#1" ).asHz(),
                    Frequency.ofPitch( "D1" ).asHz(),
                    Frequency.ofPitch( "D#1" ).asHz(),
                    Frequency.ofPitch( "E1" ).asHz(),
                    Frequency.ofPitch( "F1" ).asHz(),
                    Frequency.ofPitch( "F#1" ).asHz(),
                    Frequency.ofPitch( "G1" ).asHz(),
                    Frequency.ofPitch( "G#1" ).asHz(),
                    Frequency.ofPitch( "A1" ).asHz(),
                    Frequency.ofPitch( "A#1" ).asHz(),
                    Frequency.ofPitch( "B1" ).asHz()};

// number of halfnote intervals between notes, starting from base note
int[] majorIntervals = {2,2,1,2,2,2}; 
int[] natMinorIntervals = {2,1,2,2,1,2};
int[] harMinorIntervals = {2,1,2,2,1,3};
// initialize with C major scale
float[] baseFreqs = new float[7];
int numOctaves = 7;
int numWaves = baseFreqs.length*numOctaves;
Oscil[] oscilsLeft;
Oscil[] oscilsRight;
Pan[] pansLeft;
Pan[] pansRight;
float kickDur = 0.8;
float snareDur = 0.2;

float H;
float S;
float B;
float greyThreshold = 25;
float kickThreshold = 4000000;
int numPixels;
int[] previousFrame;

//// WEBCAM ----
//Capture video;
//// ---- WEBCAM

// MOVIE ----
Movie video;
// ---- MOVIE

//--------------------------------------------
void setup() {
//  // WEBCAM ----
//  size(640, 480);
//  // ---- WEBCAM

  // MOVIE ----
  size(640, 352, P2D);
  // ---- MOVIE

  minim = new Minim(this);

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
    while (!video.available()) {print(".");}
    video.read();
  //  println(video.width, video.height);
  // ---- MOVIE

//  background(0);
  numPixels = video.width * video.height;
  // Create an array to store the previously captured frame
  previousFrame = new int[numPixels];
//--------- color
int[] thisScaleIntervals = majorIntervals;
int keyIndex = 0; //key of C
baseFreqs[0] = allFreqs[keyIndex];
for (int i=0; i< thisScaleIntervals.length; i++) {
  keyIndex += thisScaleIntervals[i];
  baseFreqs[i+1] = allFreqs[keyIndex];
}
  
  // use the getLineOut method of the Minim object to get an AudioOutput object
  out = minim.getLineOut();
  sum = new Summer();
  sum.patch( out );
  pansLeft = new Pan[numWaves];
  pansRight = new Pan[numWaves];
  
//  println(allFreqs); //debug
//  println(baseFreqs); //debug
//  println(numWaves); //debug
  oscilsLeft = new Oscil[numWaves];
  oscilsRight = new Oscil[numWaves];

  float freq;
  for(int i = 0; i < numWaves; i++){
    freq = baseFreqs[i % baseFreqs.length] * pow(2,floor(i/baseFreqs.length));
//    println(freq); //debug ********************************************
    oscilsLeft[i] = new Oscil(freq, 0.01f, Waves.SINE );
    oscilsRight[i] = new Oscil(freq, 0.01f, Waves.SINE );
    pansLeft[i] = new Pan(-1);
    pansRight[i] = new Pan(1);
    oscilsLeft[i].patch( pansLeft[i] );
    oscilsRight[i].patch( pansRight[i] );
    pansLeft[i].patch( out );
    pansRight[i].patch( out );
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
          
          int thisIndex= round(baseFreqs.length*mappedB+mappedH);
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
    
    // Normalize the histogram to values between 0 and 1
    // and power the hist
    for (int i=0; i<histLeft.length; i++) {
      histLeft[i] = histLeft[i]/maxvalLeft;
      histLeft[i] = pow(histLeft[i],power);
      histRight[i] = histRight[i]/maxvalRight;
      histRight[i] = pow(histRight[i],power);
    }
    

    int movementSum = 0; // Amount of movement in the frame
    for (int i = 0; i < video.width*video.height; i++) { // For each pixel in the video frame...
      color currColor = video.pixels[i];
      color prevColor = previousFrame[i];
      // Extract the red, green, and blue components from current pixel
      int currR = (currColor >> 16) & 0xFF; // Like red(), but faster
      int currG = (currColor >> 8) & 0xFF;
      int currB = currColor & 0xFF;
      // Extract red, green, and blue components from previous pixel
      int prevR = (prevColor >> 16) & 0xFF;
      int prevG = (prevColor >> 8) & 0xFF;
      int prevB = prevColor & 0xFF;
      // Compute the difference of the red, green, and blue values
      int diffR = abs(currR - prevR);
      int diffG = abs(currG - prevG);
      int diffB = abs(currB - prevB);
      // Add these differences to the running tally
      movementSum += diffR + diffG + diffB;
      // Render the difference image to the screen
//      pixels[i] = color(diffR, diffG, diffB);
      // The following line is much faster, but more confusing to read
      //pixels[i] = 0xff000000 | (diffR << 16) | (diffG << 8) | diffB;
      // Save the current color into the 'previous' buffer
      previousFrame[i] = currColor;
    }
    println(movementSum);
    if (movementSum > kickThreshold) {
      out.playNote( 0, kickDur, new KickInstrument( sum ) );
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
    
    ////////////////
    // Change sounds
    for(int i = 0; i < numWaves; i++){
      oscilsLeft[i].setAmplitude( histLeft[i]/numWaves );
      oscilsRight[i].setAmplitude( histRight[i]/numWaves );
    }

  //  wave.setAmplitude( amp );
  //  wave.setFrequency( freq );
    
  
  } // if video available
}

void mousePressed() {
//  // MOVIE ----
////  println(float(mouseX)/float(width), video.duration()); // debug ****************
//  video.jump(float(mouseX)/float(width)*video.duration());
//  // ---- MOVIE
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




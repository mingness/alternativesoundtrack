# Alternative Soundtrack (Rhizome version)

A program to sonify movies. The first (current) version uses the Processing libraries (inside Eclipse) to analyze the video being played back.
It then sends OSC messages to Supercollider, which produces sounds based on different features of the video.

So far we have implemented:

* Histogram analysis
* Simple cut detection
* Optical flow
* A step sequencer that samples 16 pixels on the video frame

# Rhizome

This version requires Rhizome to show the control panel. See
https://github.com/sebpiq/rhizome

To install you need nodejs and npm.

Once you have those:

```npm install -g rhizome-server```

(You often need to use sude with -g)

Finally, go to the control panel folder and type

```rhizome config.js```

to start the Rhizome server and open

http://localhost:8000 on a browser.

You can also access that address by typing http://yourServerIP:8000, maybe from
a tablet or smartphone.

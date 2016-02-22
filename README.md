# Alternative Soundtrack

A program to sonify movies. The first (current) version uses the Processing libraries (inside Eclipse) to analyze the video being played back.
It then sends OSC messages to Supercollider, which produces sounds based on different features of the video.

So far we have implemented:

* Histogram analysis
* Simple cut detection
* Optical flow
* A step sequencer that samples 16 pixels on the video frame


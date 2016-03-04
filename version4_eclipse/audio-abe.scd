NetAddr.localAddr;

s.scope;

(
SynthDef('histogramSynth', {
    arg ch = 0,
   freqs = #[16.351597,18.354046,20.601725,21.826763,24.499718,27.5,30.86771,32.703194,36.70809,41.20345,43.653526,48.999435,55.0,61.73542,65.40639,73.41618,82.4069,87.30705,97.99887,110.0,123.47084,130.81277,146.83237,164.8138,174.6141,195.99774,220.0,246.94168,261.62555,293.66473,329.6276,349.2282,391.99548,440.0,493.88336,523.2511,587.32947,659.2552,698.4564,783.99097,880.0,987.7667,1046.5022,1174.6589,1318.5104,1396.9128,1567.9819,1760.0,1975.5334],
    amps = #[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],
    phases = #[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0];
	var sig;
	sig = 0.2 * DynKlang.ar(`[ freqs, amps, phases ]);
    Out.ar(ch, sig);
}).add;

// Test scenecut sound. To be improved.
SynthDef('scenecutSynth', {
    var sig;
    sig = 0.2 * SinOsc.ar(60) * EnvGen.kr(Env.linen(), doneAction: 2);
    Out.ar(0, sig ! 2);
}).add;

// Optical flow synth test.
SynthDef('opticalFlowSynth', {
	arg pan, pitch;
	var sig;
	sig = 0.2 * SinOsc.ar([pitch * 20 + 440, pitch * 20 + 442]);
	Out.ar(0, Balance2.ar(sig[0], sig[1], pan.clip(-1, 1)));
}).add;

// Test sequencer sound. To be improved.
SynthDef('seqSynth', {
	arg pitch = 400, amp = 0.5;
    var sig;
    sig = 0.2 * LFSaw.ar(pitch, mul: amp) * EnvGen.kr(Env.linen(0.01, 0.1, 0.3), doneAction: 2);
    Out.ar(0, sig ! 2);
}).add;

)

(
~histogramL = Synth('histogramSynth');
~histogramR = Synth('histogramSynth');
~histogramL.set(\ch, 0);
~histogramR.set(\ch, 1);
)
(
~opticalflow = Synth('opticalFlowSynth');
)

(
OSCdef('histListener', {
    arg msg;
    ~histogramL.setn(\amps, msg[1..49]);
    ~histogramR.setn(\amps, msg[50..98]);
}, "/hist");
OSCdef('scenecutListener', {
    arg msg;
    Synth('scenecutSynth');
}, "/scenecut");
OSCdef('ofListener', {
    arg msg;
	~opticalflow.setn(\pan, msg[1]);
    ~opticalflow.setn(\pitch, msg[2]);
}, "/of");
OSCdef('seqListener', {
	arg msg;
	Synth('seqSynth', [\amp, msg[3], \pitch, ((msg[1] * 12).round + 72).midicps]);
}, "seq");
)

(
OSCdef('histListener').free;
OSCdef('scenecutListener').free;
~histogramL.free;
~histogramR.free;
~opticalflow.free;
)

(
SynthDef('dynKlangSynth', {
    arg ch = 0,
   freqs = #[16.351597,18.354046,20.601725,21.826763,24.499718,27.5,30.86771,32.703194,36.70809,41.20345,43.653526,48.999435,55.0,61.73542,65.40639,73.41618,82.4069,87.30705,97.99887,110.0,123.47084,130.81277,146.83237,164.8138,174.6141,195.99774,220.0,246.94168,261.62555,293.66473,329.6276,349.2282,391.99548,440.0,493.88336,523.2511,587.32947,659.2552,698.4564,783.99097,880.0,987.7667,1046.5022,1174.6589,1318.5104,1396.9128,1567.9819,1760.0,1975.5334],
    amps = #[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],
    phases = #[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0];

    Out.ar(ch, DynKlang.ar(`[ freqs, amps, phases ]));
}).add;

// Test scenecut sound. To be improved.
SynthDef('scenecutSynth', {
    var sig;
    sig = SinOsc.ar(60) * EnvGen.kr(Env.linen(), doneAction: 2);
    Out.ar(0, sig ! 2);
}).add;
)

(
l = Synth('dynKlangSynth');
r = Synth('dynKlangSynth');
l.set(\ch, 0);
r.set(\ch, 1);
)

NetAddr.localAddr

(
OSCdef('histListener', {
    arg msg;
    l.setn(\amps, msg[1..49]);
    r.setn(\amps, msg[50..98]);
}, "/hist");
OSCdef('scenecutListener', {
    arg msg;
    Synth('scenecutSynth');
}, "/scenecut");
)

(
OSCdef('histListener').free;
OSCdef('scenecutListener').free;
l.free;
r.free;
)

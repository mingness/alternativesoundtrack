(
// 1. List files in the recordingsDir and store them in a variable

q = ();
q.files = PathName.new(thisProcess.platform.recordingsDir).files;
)

(
// 2. Load one of the files

Buffer.freeAll;
// 1bass 2wild 3crispy 9sharp 12uf 13voice 18round 20
// only wav and aiff can be loaded
q.file = q.files[13].fullPath;
(q.file ++ " loading").post;
q.buffer = Buffer.read(s, q.file, action: {
	(q.file ++ " loaded").postln;
});
"";
)

(
// 3. Simple playbuf grain synthdef - note the all important doneAction

SynthDef(\grain, {
	arg bus, bufnum=0, startPos=0.0, dur = 0.06, amp = 0.1;
	var grain, env, snd;

	env = EnvGen.kr(Env.new([0, 1, 0], [0.01, dur - 0.02]), doneAction: 2);
	grain = PlayBuf.ar(2,
		bufnum,
		BufRateScale.kr(bufnum) * EnvGen.kr(Env.new([1, 0.2], [dur], \linear)),
		1,
		BufFrames.ir(bufnum) * startPos,
		0);
	snd = Compander.ar(grain, grain,
	thresh: 0.2,
	slopeBelow: 0.1,
	slopeAbove: 1,
	clampTime: 0.01,
	relaxTime: 0.01);

	Out.ar(bus, snd * env * amp);
}).add;

// Simple effects (reverb + LPF)

SynthDef("effect", {
	arg outBus = 0, inBus, cutoff = 200;
    var input;
    input = In.ar(inBus, 2);

	input = GVerb.ar(input, 200, 2, 0.3, 0.34, drylevel: -3, earlyreflevel: -9, taillevel: -11);
	input = LPF.ar(input, Lag2.kr(cutoff, 5));

    Out.ar(outBus, input);
}).add;

)

(
// 4. Set up effects bus

q.fxBus = Bus.audio(s, 2);
q.fxSynth = Synth.new("effect", [\inBus, q.fxBus]);
)

(
// 5a. Individual grain test

Synth.grain(\grain, [
	\bufnum, q.buffer.bufnum,
	\startPos, rrand(0.0, 1.0),
	\amp, 0.2,
	\bus, q.fxBus
]);
)

(
// 5b. AlternativeSoundtrack listener. Event triggers Grain.

OSCdef('ofListener', {
    arg msg;
	var tmp, x, y, id, len;
	# tmp, x, y, id = msg;
	len = sqrt(x.squared + y.squared); // not normalized. Can be > 10
	Synth.grain(\grain, [
		\bufnum, q.buffer.bufnum,
		\startPos, id * 0.8 + 0.1 + rrand(-0.05, 0.05),
		\amp, len * 0.01,
		\bus, q.fxBus
	]);
}, "/of");
)


(
// 5c. AlternativeSoundtrack listener. Event modifies existing sequencer.

q.ofDelay = 0.1;
q.ofAmp = 0;
q.ofPos = 0;

OSCdef('ofListener', {
    arg msg;
	var tmp, x, y, id, len;
	# tmp, x, y, id = msg;
	len = sqrt(x.squared + y.squared); // not normalized. Can be > 10
	q.ofDelay = (0.1 / (1 + len)).max(0.01);
	q.ofAmp = len * 0.01;
	q.ofPos = id;
}, "/of");
)

(
q.fxSynth.set(\cutoff, 100.rrand(800));
{
	while(
		{
			true
		},
		{
			Synth.grain(\grain, [
				\bufnum, q.buffer.bufnum,
				\startPos, q.ofPos * 0.8 + 0.1 + rrand(-0.01, 0.01),
				\amp, q.ofAmp,
				\bus, q.fxBus
			]);
			q.ofDelay.wait;
		}
)}.fork
)


(
// 5d. Autonomous envelope of 1000 grains
{
	var p, i, ampEnv = Env.new([0, 0.1, 0], times: [0.01, 0.35], curve: [2, -7]);
	p = 0.3.rrand(0.7);
	i = 0;
	q.fxSynth.set(\cutoff, 100.rrand(800));
	while(
		{
			i < 1000
		},
		{
			var amp = ampEnv.at(i/1000);
			Synth.grain(\grain, [
				\bufnum, q.buffer.bufnum,
				\startPos, p,
				\amp, amp,
				\bus, q.fxBus
			]);
			p = p + -0.002.rrand(0.002);
			i = i + 1;
			0.02.wait;
		}
)}.fork
)

// Recording stuff

s.prepareForRecord
s.record
s.pauseRecording
s.stopRecording

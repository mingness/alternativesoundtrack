~lerp = { |a,b,t| b-a*t+a };

s.waitForBoot({
	"".postln;
	"".postln;
	"START".postln;

	fork {

		1.wait; // ------------------------- ---------------------------



		// just a container for things
		q = ();

		q.soundMorphDuration = 40.0;
		q.maxSynths = 60;
		q.maxAmp = 0.9;
		q.lerpSpeed = 0.05;

		q.rhizomeOSCPort = 57130;
		q.listenToRzOnPort = 57150;
		q.listenToP5OnPort = 57120;
		q.rhizomeIP = "127.0.0.1";

		q.rhizome = NetAddr(q.rhizomeIP, q.rhizomeOSCPort);
		q.rhizome.sendMsg("/sys/subscribe", q.listenToRzOnPort, "/sc");
		"Subscribe to Rhizome".postln;

		// Read folder and store file names in an randomized array
		"Reading and scrambling folder content".postln;
		q.files = PathName
		.new(thisProcess.nowExecutingPath.dirname +/+ "audio")
		.files
		.select({ |item,i| item.fileName.toLower.endsWith(".wav")})
		.scramble;
		q.files.do({ arg item, i; ("  Found " ++ item.fullPath).postln; });
		q.filesNext = 0;

		q.loadNext = {
			var file = q.files[q.filesNext],
			path = file.fullPath,
			fname = file.fileName;
			("  Loading " ++ fname ++ " into " ++ q.buffersNext).postln;
			// Only wav and aiff can be loaded
			q.buffers[q.buffersNext] = Buffer.read(s, path,
				action: { ("  Loaded " ++ fname).postln; },
				bufnum: q.buffersNext );
			q.filesNext = (q.filesNext + 1) % q.files.size;
			q.buffersNext = (q.buffersNext + 1) % q.buffers.size;
		};
		q.playNext = {
			q.playingBuf[0] = q.playingBuf[2];
			q.playingBuf[1] = q.playingBuf[3];
			q.playingBuf[2] = q.playingBuf[2] + 2 % q.buffers.size;
			q.playingBuf[3] = q.playingBuf[3] + 2 % q.buffers.size;
		};

		1.wait; // ------------------------- ---------------------------

		// Drop all buffers
		Buffer.freeAll;

		"Creating buffers and loading audio files".postln;
		q.buffers = Array.newClear(6);
		q.buffersNext = 0;
		6.do({ q.loadNext.value });
		q.playingBuf = [0, 1, 2, 3];
		q.time = 0;

		1.wait; // ------------------------- ---------------------------



		// Simple playbuf grain synthdef - note the important doneAction: 2

		SynthDef(\grain, {
			arg bus,
			bufNumCurrTop=0, bufNumCurrBottom=0, bufNumNextTop=0, bufNumNextBottom=0,
			x = -1.0,
			y = -1.0,
			time = 0.0,
			dur = 0.06,
			amp = 0.1,
			pan = 0;
			var grain, grainCurr, grainNext, env, snd,
			grainCurrTop, grainCurrBot, grainNextTop, grainNextBot;

			x = (x + 1) / 2;

			env = EnvGen.kr(Env.new([0, 1, 0], [dur, dur * 4]), doneAction: 2);
			grainCurrTop = PlayBuf.ar(2, bufNumCurrTop,    BufRateScale.kr(bufNumCurrTop),    1, BufFrames.ir(bufNumCurrTop)    * x, 0);
			grainCurrBot = PlayBuf.ar(2, bufNumCurrBottom, BufRateScale.kr(bufNumCurrBottom), 1, BufFrames.ir(bufNumCurrBottom) * x, 0);
			grainNextTop = PlayBuf.ar(2, bufNumNextTop,    BufRateScale.kr(bufNumNextTop),    1, BufFrames.ir(bufNumNextTop)    * x, 0);
			grainNextBot = PlayBuf.ar(2, bufNumNextBottom, BufRateScale.kr(bufNumNextBottom), 1, BufFrames.ir(bufNumNextBottom) * x, 0);

			grainCurr = XFade2.ar(grainCurrTop, grainCurrBot, y);
			grainNext = XFade2.ar(grainNextTop, grainNextBot, y);

			grain = XFade2.ar(grainCurr, grainNext, time * 2 - 1);

			snd = Compander.ar(grain, grain,
				thresh: 0.2,
				slopeBelow: 0.1,
				slopeAbove: 1,
				clampTime: dur * 0.2,
				relaxTime: dur * 0.2);

			snd = snd * env;

			Out.ar(bus, Balance2.ar(snd[0], snd[1], pan * 0.8, amp));
		}).add;

		// Simple effects (reverb + LPF)

		SynthDef("effect", {
			arg outBus = 0, inBus, k = 0.85;
			var input = In.ar(inBus, 2);

			input = OnePole.ar(input, Lag2.kr(k, 1));

			input = GVerb.ar(input,
				roomsize: 100,
				revtime: 5,
				damping: 0.2,
				inputbw: 0.3,
				spread: 25,
				drylevel: 0.8,
				earlyreflevel: 0.15,
				taillevel: 0.15
			);

			Out.ar(outBus, input);
		}).add;

		"Synth and effects created".postln;

		1.wait; // ------------------------- ---------------------------



		// Set up effects bus

		q.fxBus = Bus.audio(s, 2);
		q.fxSynth = Synth.new("effect", [
			\inBus, q.fxBus,
			\k, 0.85;
		]);

		"Effects bus set up".postln;

		1.wait; // ------------------------- ---------------------------



		// OSC listener. Event modifies existing sequencer.

		q.ofDelay = 0.1;
		q.ofAmp = 0;
		q.ofX = 0;
		q.ofY = 0;
		q.ofAngle = 0;
		q.testA = 0; // amp curve
		q.testC = 0; // min threshold

		// From Processing

		OSCdef('ofListener', {
			arg msg;
			var foo, len, angle, x, y; //len max value around 60
			# foo, len, angle, x, y = msg;
			// len.postln; // test values, when change optical flow grid
			q.ofDelay = (0.1 / (1 + len)).max(0.01);
			q.ofAmp = (len * 0.05).min(q.maxAmp).lincurve(q.testC * 0.5, 1, 0, 1, 3 - (q.testA * 6));
			q.ofX = ~lerp.value(q.ofX, x, q.lerpSpeed);
			q.ofY = ~lerp.value(q.ofY, y, q.lerpSpeed);
			q.ofAngle = angle;
		}, "/of", recvPort: q.listenToP5OnPort);

		// From control panel (Rhizome)

		OSCdef('testA', { |msg| q.testA = msg[1]; }, "/sc/testA", recvPort: q.listenToRzOnPort);
		OSCdef('testB', { |msg| q.fxSynth.set(\k, 1 - msg[1]); }, "/sc/testB", recvPort: q.listenToRzOnPort);
		OSCdef('testC', { |msg| q.testC = msg[1]; }, "/sc/testC", recvPort: q.listenToRzOnPort);

		"Listening to Processing and Rhizome via OSC".postln;

		1.wait; // ------------------------- ---------------------------

		fork {
			while(
				{
					true
				},
				{
					if(q.time < q.soundMorphDuration, {
						if(s.numSynths < q.maxSynths, {
							Synth.grain(\grain, [
								\bufNumCurrTop,    q.playingBuf[0],
								\bufNumCurrBottom, q.playingBuf[1],
								\bufNumNextTop,    q.playingBuf[2],
								\bufNumNextBottom, q.playingBuf[3],
								\x, q.ofX * 2 - 1 + 0.01.bilinrand,
								\y, q.ofY * 2 - 1 + 0.01.bilinrand,
								\time, q.time / q.soundMorphDuration,
								\dur, q.ofDelay * 5,
								\amp, q.ofAmp,
								\bus, q.fxBus,
								\pan, q.ofAngle.sin
							]);
						});
						}, {
							q.time = 0.0;

							"- - -".postln;

							// start using next two buffers
							q.playNext.value;
							0.05.wait;

							// load files into the two buffers which just became available
							q.loadNext.value;
							0.05.wait;
							q.loadNext.value;
							0.05.wait;

					});

					q.time = q.time + q.ofDelay;
					q.ofDelay.wait;
				}
		)};

	}

}, 50);

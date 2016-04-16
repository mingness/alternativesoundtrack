~lerp = { |a,b,t| b-a*t+a };

s.waitForBoot({
	"".postln;
	"".postln;
	"START".postln;

	fork {

		1.wait; // ------------------------- ---------------------------



		// just a container for things
		q = ();

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
			var file = q.files[q.filesNext].fullPath;
			("  Loading " ++ file).postln;
			// Only wav and aiff can be loaded
			q.buffers[q.buffersNext] = Buffer.read(s, file, action: { ("  Loaded " ++ file).postln; });
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
			amp = 0.1;
			var grain, grainCurr, grainNext, env, snd,
			grainCurrTop, grainCurrBot, grainNextTop, grainNextBot;

			x = (x + 1) / 2;

			env = EnvGen.kr(Env.new([0, 1, 0], [0.01, dur]), doneAction: 2);
			grainCurrTop = PlayBuf.ar(2, bufNumCurrTop, BufRateScale.kr(bufNumCurrTop), 1, BufFrames.ir(bufNumCurrTop) * x, 0);
			grainCurrBot = PlayBuf.ar(2, bufNumCurrBottom, BufRateScale.kr(bufNumCurrBottom), 1, BufFrames.ir(bufNumCurrBottom) * x, 0);
			grainNextTop = PlayBuf.ar(2, bufNumNextTop, BufRateScale.kr(bufNumNextTop), 1, BufFrames.ir(bufNumNextTop) * x, 0);
			grainNextBot = PlayBuf.ar(2, bufNumNextBottom, BufRateScale.kr(bufNumNextBottom), 1, BufFrames.ir(bufNumNextBottom) * x, 0);

			grainCurr = XFade2.ar(grainCurrTop, grainCurrBot, y);
			grainNext = XFade2.ar(grainNextTop, grainNextBot, y);

			grain = XFade2.ar(grainCurr, grainNext, time * 2 - 1);

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

		"Synth and effects created".postln;

		1.wait; // ------------------------- ---------------------------



		// Set up effects bus

		q.fxBus = Bus.audio(s, 2);
		q.fxSynth = Synth.new("effect", [
			\inBus, q.fxBus,
			\cutoff, 100
		]);

		"Effects bus set up".postln;

		//q.fxSynth.set(\cutoff, 800)

		1.wait; // ------------------------- ---------------------------



		// OSC listener. Event modifies existing sequencer.

		q.ofDelay = 0.1;
		q.ofAmp = 0;
		q.ofX = 0;
		q.ofY = 0;

		// From Processing

		OSCdef('ofListener', {
			arg msg;
			var foo, len, angle, x, y;
			# foo, len, angle, x, y = msg;
			q.ofDelay = (0.1 / (1 + len)).max(0.01);
			q.ofAmp = (len * 0.01).min(0.9);
			// TODO: maybe use angle for panning
			q.ofX = ~lerp.value(q.ofX, x, 0.1);
			q.ofY = ~lerp.value(q.ofY, y, 0.1);
		}, "/of", recvPort: q.listenToP5OnPort);

		// From control panel (Rhizome)

		OSCdef('testA', { |msg| q.testA = msg[1]; }, "/sc/testA", recvPort: q.listenToRzOnPort);
		OSCdef('testB', { |msg| q.testB = msg[1]; }, "/sc/testB", recvPort: q.listenToRzOnPort);

		"Listening to Processing and Rhizome via OSC".postln;

		1.wait; // ------------------------- ---------------------------

		fork {
			while(
				{
					true
				},
				{
					if(q.time < 10.0, {
						if(s.numSynths < 100, {
							Synth.grain(\grain, [
								\bufNumCurrTop,    q.playingBuf[0],
								\bufNumCurrBottom, q.playingBuf[1],
								\bufNumNextTop,    q.playingBuf[2],
								\bufNumNextBottom, q.playingBuf[3],
								\x, q.ofX * 2 - 1 + 0.01.bilinrand,
								\y, q.ofY * 2 - 1 + 0.01.bilinrand,
								\time, q.time / 10,
								\dur, q.ofDelay,
								\amp, q.ofAmp,
								\bus, q.fxBus
							]);
						});
						}, {
							q.time = 0.0;

							// start using next two buffers
							q.playNext.value;

							// load files into the two buffers which just became available
							q.loadNext.value;
							q.loadNext.value;

					});

					q.time = q.time + q.ofDelay;
					q.ofDelay.wait;
				}
		)};

	}

}, 50);

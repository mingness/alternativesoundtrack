(
~bands = 4;
~rhizomeOSCPort = 9000;
~listenOnPort = 9002;
~rhizomeIP = "127.0.0.1";
~rhizome = NetAddr(~rhizomeIP, ~rhizomeOSCPort);
~rhizome.sendMsg("/sys/subscribe", ~listenOnPort, "/slider");
"Subscribe to Rhizome. Ask it to send us /slider messages on port 9002"
)

(
q = Array.fill(~bands, { |i| {
	arg amp = 0.0;
	var frq = 100 * i + 600;
	SinOsc.ar([frq, frq * 1.01], 0, amp)
	}.play;
});
"Create an array of 4 test synthesizers to be controlled via OSC"
)

(
OSCdef('oscListener', {
    arg msg;
	var band = msg[1], amp = msg[2];
	q[band].set("amp", amp);
}, '/slider', recvPort: ~listenOnPort);
"Start listening to OSC /slider messages on port 9002"
)

(
~bands.do({|i| ~rhizome.sendMsg("/slider", i, 0.2.rand)});
"Send random /slider values to Rhizome"
)


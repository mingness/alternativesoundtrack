
thisProcess.openUDPPort(9002);
(
f = { |msg, time, replyAddr, recvPort|
    if(msg[0] != '/status.reply') {
        "At time %s received message % from % on port%\n".postf(
			time, msg, replyAddr, recvPort )
    }
};
)
thisProcess.addOSCRecvFunc(f);
thisProcess.removeOSCRecvFunc(f);

b = NetAddr.new("localhost", 9000);
b.sendMsg("/sys/subscribe", 9002, "/");

// Now sending stuff
b.sendMsg("/hello", "there");

n = NetAddr("127.0.0.1", 57120); // rhizome

OSCdef('ofListener', {
    arg msg;
	var foo, len, angle, x, y;
	# foo, len, angle, x, y = msg;
	q.ofDelay = (0.1 / (1 + len)).max(0.01);
	q.ofAmp = (len * 0.01).min(0.9);
	q.ofX = x;
	q.ofY = y;
	// TODO: maybe use angle for panning
}, "/of");
"Listening to Processing via OSC"
)

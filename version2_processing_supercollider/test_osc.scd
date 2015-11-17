s.boot

////////////////////////////////
// OSC
NetAddr.localAddr
// 127.0.0.1, 57120

(
OSCdef('starlistener', {
    // msg is an array that contains all osc
    // arguments
    arg msg;

    // this is useful for debugging
	msg.postln;

}, "/starhit");
)

OSCdef('starlistener').free


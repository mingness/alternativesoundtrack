(
SynthDef('help-dynKlang', {
    arg ch = 0,
   freqs = #[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],
    amps = #[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],
    phases = #[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0];

    Out.ar(ch, DynKlang.ar(`[ freqs, amps, phases ]));
}).add;
)

(
l = Synth('help-dynKlang');
r = Synth('help-dynKlang');
l.set(\ch, 0);
r.set(\ch, 1);
)

(
l.setn(\freqs, Array.fill(15, { arg i; (1.5**i)*40; }));
r.setn(\freqs, Array.fill(15, { arg i; (1.501**i)*40; }));
)
(
l.setn(\amps, Array.fill(15, { 0.001.exprand(0.06); }));
r.setn(\amps, Array.fill(15, { 0.001.exprand(0.06); }));
)

NetAddr.localAddr

(
OSCdef('starlistener', {
    arg msg;
    l.setn(\amps, msg[1..16]);
    r.setn(\amps, msg[17..32]);
    //msg[1..16].postln;
}, "/starhit");
)

# Strampáil #

Strampáil is a Java-based [STOMP](http://stomp.github.io/) client/server library.

### May I use this? ###

Be my guest, though it is just a proof-of-concept project for me at the moment.

### Can I use this? ###

That depends – this is very much ‘pre-α’ at the moment. It is very unlikely to become any more developed than my personal needs/intrigue requires. Chances are it will not implement much of the STOMP protocol beyond what I need it for. It may not even be useable for that. I make no guarantees regarding security or stability of the code.

**Do not use in production code!**

### Strampáil? ###

_Strampáil_ – Irish for the verb _to stomp_ – as in, to squash under your feet. Seemed like a fitting name to me.

### How do I say it? ###

stɹɒmpˈɔɪl (stromp–OIL)

### What? No TDD‽ ###

No…
I am currently playing around with concepts. Also, I have no experience with Java-based TDD technologies/concepts, and would rather not learn a whole framework for the sake of a few minutes of playing-around.

### Why STOMP? ###

Believe it or not, this started from experimenting with STOMP feeds that a signalling simulator that I play with ([SimSig](www.simsig.co.uk)) issues when running. I realised that there isn’t much in the way of Java-based STOMP clients, especially those aimed at version 1.1 of the protocol, so I thought I would investigate making my own.

### Current Status ###

Only the client is implemented at the moment.
Strampáil covers basic parts of STOMP version 1.1. Specifically, it can send:

- CONNECT/STOMP;
- DISCONNECT;
- SUBSCRIBE (autoack only, preset ID only);
- UNSUBSCRIBE (preset ID only);

It can deal with direct responses (such as CONNECTED, ERROR and RECEIPT) appropriately. It does not support custom headers, nor heart-beating. Note also that Strampáil does not do protocol negotiation – it is locked to version 1.1 as of present.

Other than direct replies to frames as mentioned above, Strampáil will, at present, merely print out any frame it receives from the server to the CLI if the debug messaging level is set to DEBUG. It does not currently do any (useful) processing on them. In addition, the current algorithm for extracting frame body data will fail if there are NUL bytes in the body.

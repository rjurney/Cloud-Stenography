Cloud Stenography was to be a graphical interface to Hadoop, on a dataflow and SQL abstraction. The idea was to compose ad-hoc datasets for analysis and visualization in Pig SQL using a graphical dataflow interface leveragingILLUSTRATE, then easily export them out to Excel. If I learned anything with Lucision it is that data isn’t real until users can touch it in Excel. I early aborted this project and company. A few people have asked me about it, and I’m hoping that this will be useful to someone.

You can see this project in action in a video [here](http://vimeo.com/6032078).

It really generates the code in that video, which I believe was a LOAD/FILTER/GROUP, etc. WireIt transmits the graph, the Perl does a topological sort to get an execution order, and then very hackishly generates a subset of Pig. I had not yet handled referencing datatypes two steps back in the interface when I stopped working on it.

It is a huge hack - my specialty. If I were to do it again, I would use Ruby, I would modify Pig to return data in JSON from a restlet, etc.

This project was made possible by:

Catalyst WireIT  WireIT Editor http://javascript.neyric.com/wireit/examples/WiringEditor/  PigPen

With assistance from Alan Gates and Eric Abouaf

This was a prototype. It is no longer under active development. Anyone wishing to continue development should pull the most recent WireIT release.


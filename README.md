Cloud Stenography was to be a graphical interface to [Hadoop](http://hadoop.apache.org/), on a dataflow and SQL abstraction. The idea was to compose ad-hoc datasets for analysis and visualization in Pig SQL using a graphical dataflow interface leveraging [ILLUSTRATE](http://hadoop.apache.org/pig/docs/r0.6.0/piglatin_ref2.html#ILLUSTRATE) to preview data, run jobs and then easily export them out to Excel. If I learned anything with [Lucision](http://www.lucision.com) it is that data isn’t real until users can touch it in Excel. I early aborted this project and company. A few people have asked me about it, and I’m hoping that this will be useful to someone.

You can see this project in action in a video [here](http://vimeo.com/6032078).

It really generates the code in that video, which I believe was a LOAD/FILTER/GROUP, etc. WireIt transmits the graph, the Perl does a topological sort to get an execution order, and then very hackishly generates a subset of Pig. I had not yet handled referencing datatypes two steps back in the interface when I stopped working on it, so you had to fill those in.

It is a huge hack - my specialty. If I were to do it again, I would use Ruby, I would modify Pig to return data in JSON from a restlet, etc.

This project was made possible by:

[Catalyst](http://catalystframework.org)
[WireIT](http://javascript.neyric.com/wireit/)
[The WireIT Wiring Editor](http://javascript.neyric.com/wireit/examples/WiringEditor/)
[PigPen](http://wiki.apache.org/pig/PigPen)

With assistance from [Alan Gates](http://www.linkedin.com/pub/alan-gates/2/45a/181) and [Eric Abouaf](http://fr.linkedin.com/in/ericabouaf)

This was a prototype. It is no longer under active development. Anyone wishing to continue development should pull the most recent [WireIT release](http://github.com/neyric/wireit), look at the code as an example and start over :)


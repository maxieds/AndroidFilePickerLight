## Some small bits of clarity on why the new storage access changes are, well, so unavailable at the moment:

Basically Google has invoked its power and privilege to impose a new file system access scheme from 
Mars on developers. And that new Martian access mechanism is only loosely coupled with 
more standardized historical ways of getting at files on local disk native to Java and its I/O functionality 
that used to be so lovely and implicitly understandable to us native Unix folks. 
As the development of this library 
gets under way and Android 11+ sets into more new devices on the market, we hope to figure out how to keep up 
with this new alien technolgy and will continue to expect small operations like reading files on 
one's hardware to happen in finite time (on this earth).  

In the event that anyone has made it to these lines at the bottom of this lengthy unfinished documentation 
prospectus, I have a few words to empathize with others on the net that have expressed confusion 
working with these new Android 10-11+ APIs and their pathological 
file system access restrictions. I do not believe that 
this offering from the Android API is a refined, well thought out masterpiece that 
requires careful attention to appreciate and understand. Rather, it is apparent (to me) that the file 
provide frameworks and the SAF requirements form an unholy and massively *crappy* specification to deal with. 
It is difficult and cumbersome even to perform the most routine file operations now. 
The documentation from Google on their GitHub sources profile is most underwhelming and not 
demonstrative of how to actually use these changes to their API. Granted, I am not in a lighthearted moode having 
slogged over rudimentary concepts like this for hours today. But this is a poor offering for a platform that is 
so easy to get off the ground and working lierally in hours! WTF, Google, et tu with my standard advanced 
integrated Java support for Unix and System V fare? This blows. :scream: :scream: :scream:

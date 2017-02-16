Shimeji for Mac
===============
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/3b592fdfb0c446bab59303390e51b3d8)](https://www.codacy.com/app/873863981/shimeji4mac?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=AlanJager/shimeji4mac&amp;utm_campaign=Badge_Grade)

This is a repository for Shimeji for Mac, An anti-productivity application which is notorious around [pixiv](http://pixiv.net).

When it launched, a cluel mascot fall out to your window. It increases by schizogenesis or pulling up its companions from ground of window. Then they walk around in front of windows, hung on the focused window, even throw it out of monitor.

Download
-------
Download link: https://github.com/AlanJager/shimeji4mac/releases

Building
--------

* Use ide to compile the project can easily get a working jar.(if any question, just mail to msjdxhc@gmail.com)

* Type `ant app` in the project root. (have error in macOS for the 3rd part package may not work)

TODO
----

* Optimize performance.
* Make Manager daemon thread sleep time more suitable.
* Frindly message when launched Shimeji.app is located outside of project directory
* Proper dock frame detection
* Use AXMakeProcessTrusted() instead of activation of assistive devices (deprecated in OS 10.9)

LICENSE
-------

Licensed under zlib/libpng license inherited from [the original project for windows](http://www.group-finity.com/Shimeji/).

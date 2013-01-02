# Scrolling Text with the Novation Launchpad

[Demo on Youtube](http://youtu.be/Zwbro6do84U)

<img src="https://raw.github.com/sordina/launchtext/master/images/launchtext.png" alt="Scrolling Text on the Launchpad" />

A stand-alone Jar package is available [here.](http://s3.amazonaws.com/sordina.binaries/launchtext-0.0.3-standalone.jar)

The Launchpad must be plugged in before starting the app.

## Web-Interface

An ADVANCED web-interface is started on http://localhost:9876. This allows
the currently displayed message to be updated. Updates can be nicely automated with
`curl`, or something similar:

    curl -s -X POST --data "message=Hello World!   " http://localhost:9876

## Controls (Side-Buttons)

    'stop'   - Pauses and unpauses the text
    'trk on' - Speeds up the scrolling
    'solo'   - Slows down the scrolling
    'vol'    - Exit the program

## TODO

* Clean this crap up! :-)
* Seems to allocate ~300M of memories to this application... I'm not good at computer...
* Add colors!!!!

# How to add a screencap

1. Make sure the options parameter of your drawing has `{ :quil { :size [800 500] :features []}}`
 - eg `(def drawing (Drawing. "Drag Glitch" setup update-state draw-state cli-options { :quil { :size [800 500] :features []}}))`
2. Get a screencapture program that supports gifs. A good one is [GifGrabber](http://www.gifgrabber.com/).
3. Record the animation for about 15 seconds.
4. Upload the gif to imgur.
5. Add an image link to it in the main readme. Be sure to get the direct link, with a `.gif` extension.

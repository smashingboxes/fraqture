# Storefront

An interactive art framework that takes in inputs and events, maps them to the
interactive parts of a drawing, and renders the art.

## Usage

### Bob Ross Drag Glitch

`lein run ross`

### Crazy Acid Spiral Glitch

`lein run spiro`

### Sliding grid

`lein run sliding-grid`

## Requirements

- Install Java JRE or JDK from [Oracle's website](http://www.oracle.com/technetwork/java/javase/downloads/index.html).
- `brew install leiningen` (or install it for your OS using [these instructions](http://leiningen.org/))

# How to make it your screensaver

1. Create a `screensaver.sh` with the following:
````
#!/bin/zsh

cd <path_to_storefront>
lein run drag # Or whichever animation command you want to run
````
2. Download and install the latest version of [AppStartSaver](http://www.themcdonalds.net/richard/index.php?title=AppStartSaver)
3. Open the settings for the AppStartSaver (System Preferences->Desktop & Screensaver->Screensaver->AppStartSaver->Screen Saver Options)
4. Click set path, and select the `screensaver.sh` file you just created
5. Check "Stop launched program..." and whatever other settings you want
6. Hit okay, and you're good to go!

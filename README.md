# Serpentine

Serpentine is a lightweight configuration library mod that can easily be used in other projects.

Serpentine provides an easy way to setup configuration for any mod, all you need is a class that extends the Config class provided by Serpentine and to then register it in your modInitializer.

The config screen is probably the only bloat in this mod but even then its just two classes that automate making a config screen for you.

The config screen parses your config's expects and detects their default value's type. If it is an integer it makes a SliderWidget that goes from 0-100 (I'll add more customisation for this in later versions), 

if it is a boolean it'll make two buttons for True and False, the currently selected one (not saved one) will have a checkmark next to it. If it is a float or string then a textField will be made where the user can type in their desired value.

# Current limitations

### ~~one config at a time~~

~~Only one mod can use Serpentine at a time (1.7.0)~~

As of 2.0.0, multiple configs are supported

### Integer slider limited boundaries in Config screen

Integer expects in the Config Screen is only set up to be a slider from 0 - 100, this is quite limiting

but I am rn working on it


# Installation Guide

## Normal users

click on the github releases and download the version that you need!

## Developers

1. download the most recent jar file in the release page
2. in your build.gradle file, add this line to your dependencies block:

    ``modImplementation files('libs/serpentine.jar')``

3. drag the jar file into a folder called libs in your project root
4. you may need to run ./gradlew --refresh-dependencies for your IDE to update to the change but after that, it's installed!

* only reason it is this way is because I can't be bothered to upload to maven, soon I'll add it to modrinth where this guide will be updated

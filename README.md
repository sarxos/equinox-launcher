equinox-launcher
============

This is simple POC of how to embed Equinox OSGi runtime in POJ application. The same 
approach as presented in ```Launcher``` class can be used within any other Java code.
Depending on your need you can choose which bundles you would like to start while 
initialization. By default I added few which I'm personally using in various 
applications.

## How To Run

1. [Equinox SDK](http://download.eclipse.org/equinox/) has to be downloaded first to 
run Launcher. 
2. Extract downloaded archive and copy all files from ```plugins``` 
directory into ```plugins``` in this project. 
3. Include your custom JARs in ```libs``` directory or add them as ```dependency```
in ```pom.xml```.
4. Execute ```mvn clean install```
5. Run ```Launcher``` class.

All bundles you put in ```dropins``` directory will be automatically deployed.

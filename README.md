# KSP Craft Stats

KSP Craft Stats displays useful statistics and data about your vehicles, and allows basic ascent simulations, so you can get a rough idea of how your rocket
will perform in seconds, rather than minutes.

KSP Craft Stats is a collection of command-line tools. There is currently no GUI, though anyone is welcome to write one.  KSP Craft Stats also endeavours to be
a library jar that can be used by other tools to abstract away the complexities of loading KSP data files, calculating statistics, and merging crafts.

Many thanks go to these other projects, for paving the way in figuring out the internals of KSP:

* [Payloader](http://forum.kerbalspaceprogram.com/showthread.php/29950-0-18-Payloader-1-2-A-quick-easy-payload-launcher-utility!-WIN-LIN-MAC)
* [KSP scripts](https://github.com/numerobis/KSP-scripts)

Thanks also to @ojacobson/derspiny for his invaluable help with all the math and equations.

## Tools

There are a number of different command-line tools included in Craft Stats. 

* PartHierarchy - Shows the part tree so you can see how things are connected
* StageParts - Shows what parts will still be on the ship in each stage, as well as what parts get dropped.
* VehicleStats - Shows mass, thrust, acceleration, and much more for the ship, as well as each stage.
* AscentSim - INCOMPLETE: Attempts to simulate an ascent from the surface of any body in KSP.
* MergeCraft - NOT STARTED: Will allow merging any number of vehicles together to easily build multi-stage rockets.

All tools need to be told where to find the KSP data files.  This can be done in one of two ways:
* Set a KSP_HOME environment variable that points to your KSP directory.
* Pass the -k parameter to the tools.  eg. -k c:\KSP_win 

### PartHierarchy

Shows the part tree so you can see how things are connected.

Required params:
-c or --craft  The path to the .craft file to load.

Optional params:
-v or --verbose More verbose output.

Example:
parthierarchy -c testrocket.craft -v

### StageParts

Shows what parts will still be on the ship in each stage, as well as what parts get dropped.

Required params:
-c or --craft  The path to the .craft file to load.

Optional params:
-a or --all     List all parts, not just major ones.
-v or --verbose More verbose output.

Example:
stageparts -c testrocket.craft -v -a


### VehicleStats

Shows mass, thrust, acceleration, and much more for the ship, as well as each stage.

Required params:
-c or --craft  The path to the .craft file to load.

Optional params:
-s or --stages  Show information about individual stages.
-v or --verbose More verbose output.

Example:
vehiclestats -c testrocket.craft -v -s

### AscentSim

INCOMPLETE: Attempts to simulate an ascent from the surface of any body in KSP.

### MergeCraft

NOT STARTED: Will allow merging any number of vehicles together to easily build multi-stage rockets.
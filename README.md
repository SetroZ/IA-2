# IA-2 Java Project

This project is written in pure Java and does not require Gradle, Maven, or any IDE to run.
You can compile and execute it using the provided build scripts.
The example data from the task description is included and stored in csv files as:
- employeeData.csv
- taskData.csv

---

```
## 📁 Project Structure
├──out/ # All classes from java compiling with build
├── src/
│ └── main/
│ ├── java/ # Java source code
│ │ └── Main.java
│ └── resources/ # CSV and other resource files
├── results/
|   └── /performance # Performance Log CSV's
├── lib/ #External Libraries
├── build.sh # Build script for macOS/Linux
└── README.md # You're reading it!
```

## Prerequisites

- JDK 23 or later

## Dependencies
- This project uses jcommon-1.0.24.jar and jfreechart-1.5.3.jar to generate charts
- These have been included in the repo under root/lib
- They can be linked and managed without the need of gradle or maven via the usage of the build script.

### Running build on macOS/Linux
This script will compile all classes into an "out" directory, link all dependencies from lib and run the main class
1. Open a terminal
2. Navigate to your project directory root directory (the folder where this file is located)
3. Make the script executable (if it isn't already):
   chmod +x build.sh
4. Run the script:
   ./build.sh
   
### Running clean on macOS/Linux
This script will remove the result and out folders (including randomly generated data)
2. Open a terminal
2. Navigate to your project directory root directory (the folder where this file is located)
3. Make the script executable (if it isn't already):
   chmod +x clean.sh
4. Run the script:
   ./clean.sh


#### Troubleshooting: "Permission denied" when running build or clean

If you see this error:
./build.sh: Permission denied
or
./clean.sh: Permission denied

This means the script isn't executable. Fix it with:
chmod +x build.sh

## Finding results files

All results files will be located in root/results/
- Results exist as individual runs.
  A run can be one single trial of a single algorithm, multiple trials of the same algorithm, a single trial of all algorithms or multiple trials of all algorithms
  They are incremented in order of creation, starting at 1.
```
## 📁 Result Structure
├── results
│ └── run(x) # Where x is the run ID
│ │ └── Performance # Where the log files used to generate the charts are stored
│ │ └── Charts # Where the generated visualisations are stored for each run
│   └── AlgorithmName.txt # Where runs that used "File Output = true" Store report their progress and final solutions
```

## Finding Generated Data
\[IMPORTANT] GENERATED DATA WILL BE DESTROYED WHEN ./clean.sh IS RUN

All generated data is stored under root/out/resources
- This is where the build file compiles the class resources to reside
- The data stored in root/src/main/resources is copied over to this directory at build.
- Generated Data in this directory WILL be destroyed when ./clean.sh is run. To save it, you can simply copy it to src/main/resources

```
## 📁 Out Structure
├── out
│ └──  # All packages copied from src/main/java
│ │ └── # All Class files compiles from src/main/java
│ └── resources
│   └── copiedData.csv # Data copied from src/main/resources
│ │ └── RandomData(x) # Where x is a number incremented for each set of randomly generated data
│ │ │ └── Employees.csv  # Randomly generated employee data
│ │ │ └── Tasks.csv   # Randomly generated Task data
```


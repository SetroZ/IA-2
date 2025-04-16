# IA-2 Java Project

This project is written in pure Java and does not require Gradle, Maven, or any IDE to run. 
You can compile and execute it using the provided build scripts.

---

## 📁 Project Structure
├── src/
│ └── main/
│ ├── java/ # Java source code
│ │ └── Main.java
│ └── resources/ # CSV and other resource files
├── build.sh # Build script for macOS/Linux
├── build.bat # Build script for Windows
└── README.md # You're reading it!

## Prerequisites 
- JDK 23 or later

### Running build on macOS/Linux
1. Open a terminal
2. Navigate to your project directory root directory (the folder where this file is located)
3. Make the script executable (if it isn't already): 
     chmod +x build.sh
4. Run the script:
     ./build.sh

#### Troubleshooting: "Permission denied"
If you see this error:
    ./build.sh: Permission denied
This means the script isn't executable. Fix it with:
    chmod +x build.sh


### Running build on Windows

1. Open Command Prompt or PowerShell
2. Navigate to your project directory (where) this file is located.
3. Run the batch file:
     build.bat

* Alternatively, you can double-click the build.bat file in Windows Explorer.

## Finding results files
- All results files will be located in /out/results/
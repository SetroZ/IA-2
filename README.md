# IA-2 Java Project

This project is written in pure Java and does not require Gradle, Maven, or any IDE to run.
You can compile and execute it using the provided build scripts.

---

```
## 📁 Project Structure
├── src/
│ └── main/
│ ├── java/ # Java source code
│ │ └── Main.java
│ └── resources/ # CSV and other resource files
├── build.sh # Build script for macOS/Linux
├── build.bat # Build script for Windows
└── README.md # You're reading it!
```

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

- Alternatively, you can double-click the build.bat file in Windows Explorer.

## Finding results files

- All results files will be located in /out/results/

# Algorithms

## Particle Swarm Optimisation

### Varaibles

- A particle $P_i$ is a solution. _A mapping of tasks to employees_.
- A position $p_i$ is a task. whose value is an employee id.
- $v_i$ is velocity for $i$ th position/task.
- $p_i$ is the $i$ th task/position. population[i][taskid] = $Employee_i$.
- $P_i$ is $i$ th particle/solution.
- $Population[i]$ is a
  - Solution.
  - Particle.
  - Vector of positions with corresponding velocities found in another array.
  - All tasks mapped to employees.
- $Population[i][task_{j}]$ value is a
  - Employee id
  - position

### Terminology:

1. Each particle is represented by a vector of positions and velocities.
2. Velocity decides how much a particle should change its position.
3. A particle is the mapping of multiple tasks to employees e.g **solution**.
4. A position is a mapping of a single task $p_i$ to employee.
5. positions are updated using (prevPosition + newVelocity).
6. velocity is updated using an equation found in the lecture slide.
7. The personal best $Pbest$ of each particle represents the best solution (task-to-employee mapping) that the particle has found so far.
8. The global best $Gbest$ is the best solution found by any particle in the swarm.

#### Velocity updates:

- Velocity is a value between 0-1 represnting the chance of switching.
- Greedly select the employee with the lowest cost. or randomly

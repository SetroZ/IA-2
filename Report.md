# 1. Introduction
## 1.1 Employee Task Assignment Optimisation Problem

**Problem summary:**
The Employee Task Assignment Optimisation problem addresses the challenge of optimally allocating a set of tasks to a group of employees while respecting a variety of real-world constraints. Specifically, the company in question must assign a number of distinct tasks of size $i$ ($T = \{T_1, T_2, ..., T_i\}$), each with a
- defined workload (in hours)
- a difficulty level
- a deadline
- a required skill type

to a pool of $j$ available employees ($E = \{E_1, E_2, ..., E_j\}$), each characterised by
- limited working hours
- an individual skill level
- a set of specific skill types they possess.

The problem therefore is to assign these tasks to the company’s employees satisfying 5 constraints:
- **Unique Assignment:** Each task is to be assigned to one and only one employee.
- **Capacity Constraint:** The total amount of task hours assigned to an employee shouldn’t exceed an employee’s available hours.
- **Skill Level Constraint:** A task should only be assigned to an employee if and only if the employee’s skill level is at least equal to or more than the task’s difficulty.
- **Specialised Skill Matching:** A task can only be assigned to an employee if and only if the employee has that specific skill that the task requires.
- **Deadline Consideration:** if an employee finishes the task before its deadline, a penalty is added.

### Cost Function
The cost function quantifies how "bad" or inefficient a particular task assignment solution is by summing penalties for violating key constraints. The objective is to minimise this cost, ensuring a more optimal and feasible task-to-employee allocation.

It is defined as:

$$\text{Cost} = \beta \cdot \text{Skill Mismatch Penalty} + \alpha \cdot \text{Overload Penalty} + \delta \cdot \text{Difficulty Violation Penalty} + \gamma \cdot \text{Deadline Violation Penalty} + \sigma \cdot \text{Unique Assignment Violation Penalty}$$
Each term represents a different type of constraint violation or inefficiency:
- **Overload Penalty ($\alpha$):** Penalises solutions where an employee is assigned more task hours than their available working hours.
- **Skill Mismatch Penalty ($\beta$):** Applies when a task is assigned to an employee who lacks the specific skill required for the task.
- **Difficulty Violation Penalty ($\delta$):** Incurred when a task is assigned to an employee whose skill level is below the task’s difficulty.
- **Deadline Violation Penalty ($\gamma$):** Applies if a task is completed too early (before its deadline).
- **Unique Assignment Violation Penalty ($\sigma$):** Penalises cases where tasks are either not assigned at all or assigned to more than one employee.

## 1.2 Aim and Scope

The purpose of this report is to analyse the performance and effectiveness of three widely used optimisation algorithms: Genetic Algorithm (GA), Particle Swarm Optimization (PSO) and Ant Colony Optimization (ACO) against randomly generated synthetic data.

The scope of the report includes the implementation of each algorithm on synthetically generated datasets that model real-world task and employee attributes such as workloads, deadlines, skill requirements, and working hour limits. The evaluation focuses on three performance metrics:
- **Solution Quality (Convergence):** How optimal and effective the task assignments are in minimizing the overall cost function.
- **Computational Efficiency:** The time and resources consumed by each algorithm during the runtime.
- **Constraint Satisfaction:** The degree to which each solution adheres to assignment rules and avoids constraint violations.

By analysing and comparing these algorithms, the report aims to offer valuable insights into the relative strengths, weaknesses for real-world task allocation problems.

# 2. Methodology

## 2.1 Algorithm Overview

### 2.1.1 Genetic Algorithm (GA)

<!-- Placeholder for user to complete -->

### 2.1.2 Particle Swarm Optimization (PSO)

**Introduction**

The Particle Swarm Optimization (PSO) algorithm is a metaheuristic that iteratively improves a candidate solution by reducing the cost function, without making assumptions about the underlying problem. It uses a population (swarm) of candidate solutions, known as particles. Each particle explores the defined search space while tracking two key metrics: its personal best ($pBest$), which is the best solution it has found so far, and the global best ($gBest$), which is the best solution found by any particle.

The particle's position and velocity are updated using mathematical formulas influenced by both $pBest$ (cognitive), $gBest$ (social). These updates guide the particle toward better solutions over time. While PSO does not guarantee finding a globally optimal solution, it aims to minimise the cost function and converge toward optimal or near-optimal solutions through collective swarm intelligence with introduced randomness.

#### Terminology
In our PSO implementation for the Employee-Task Assignment problem, we have chosen to represent our data using 2D and 1D arrays.
#### Swarm Representation
- A swarm is represented using a two-dimensional array, where `swarm[i]` refers to the $i$-th particle in the population.
- `swarm[i][j] = employeeID` means task $j$ is assigned to a specific employee.
- `v = new double[populationSize][tasks.size()]` stores the velocity associated with each position.
- A 2D `pBest` array maps the personal best for each particle.
- A 1D `gBest` contains the global best solution out of all particles.

#### Particle Updating Mechanism
Each particle updates its task-to-employee mapping based on velocities computed using the function:

```java
private double calculateVelocity(double gBest, int pBest, double v, int currP) {
    final int maxV = employees.size();
    Random rd = new Random();
    double r1 = rd.nextDouble(0.1, 1.0);
    double r2 = rd.nextDouble(0.1, 1.0);
    double cognitive = c1 * r1 * (pBest - currP);
    double social = c2 * r2 * (gBest - currP);
    double stag = (STAG_LIMIT < lastgBestUpdate) ? rd.nextDouble(1.0, 1.5) : 1;
    double newV = w * v + cognitive + social * stag;
    return Math.max(-maxV, Math.min(maxV, newV));
}
```

The velocity is calculated for each task using the function above which is based on:
- **Cognitive Component:** Pull toward the particle’s personal best.
- **Social Component:** Pull toward the global best solution.
- **Stagnation limit:** Accelerates randomness if the global best hasn't improved recently.
- **Velocity Clamping:** Ensures the updated velocity stays within valid bounds.

#### Position Update and Feasibility Handling

Each particle's position _(task assignment)_ is updated by applying the newly computed velocity to `swarm[i][j]`. The updated position is passed to a validation function that ensures the following constraints:
- Checks skill matching and skill level compatibility.
- Generates a list of eligible employees and verifies if the new assignment lies within this list.

The **fitness evaluation** for each particle uses the defined cost function, which aggregates penalties for violating task assignment constraints.

### 2.1.3 Ant Colony Optimisation (ACO)

## 2.2 Implementation Details

### 2.2.1 Solution Encoding Strategy

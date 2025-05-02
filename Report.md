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


## 1.2 Aim and Scope

The purpose of this report is to analyse the performance and effectiveness of three widely used optimisation algorithms: Genetic Algorithm (GA), Particle Swarm Optimisation (PSO) and Ant Colony Optimisation (ACO) against randomly generated synthetic data.

The scope of the report includes the implementation of each algorithm on synthetically generated datasets that model real-world task and employee attributes such as workloads, deadlines, skill requirements, and working hour limits. The evaluation focuses on three performance metrics:
- **Solution Quality (Convergence):** How optimal and effective the task assignments are in minimising the overall cost function.
- **Computational Efficiency:** The time and resources consumed by each algorithm during the runtime.
- **Constraint Satisfaction:** The degree to which each solution adheres to assignment rules and avoids constraint violations.

By analysing and comparing these algorithms, this report aims to offer valuable insights into the relative strengths, weaknesses for real-world task allocation problems.

# 2. Methodology

## 2.1 Algorithm Overview

All Algorithms extend an abstract class called `AbstractOptimationAlgorithm`
- This serves as the superclass for all algorithm implementations to use standardised parameters, reporting functions and interactions with the observer classes.

The abstract optimisation class

### 2.1.1 Genetic Algorithm (GA)

**Overview**

The Genetic Algorithm (GA) is a metaheuristic optimisation approach inspired by the processes of natural selection and evolution. In our implementation, GA iteratively improves a population of candidate solutions through selection, crossover, mutation, and elitism operations to find an optimal assignment of tasks to employees.

#### Solution Representation

- Each solution (chromosome) is represented as an integer array where the index corresponds to a task ID and the value represents the employee ID assigned to that task.
- For example, `solution[3] = 2` means task 3 is assigned to employee 2.
- A population consists of multiple such solutions, forming a 2D array where each row is a distinct solution.

#### Evolutionary Operators

**Selection**: Tournament selection is used to choose parent solutions for breeding:

- For each parent selection, a tournament of size 3 is formed by randomly selecting solutions from the population.
- The solution with the lowest cost (best fitness) within the tournament is chosen as a parent.
- This process is repeated to select two parents for breeding.

**Crossover**: Uniform crossover is implemented to combine genetic material from two parents:

- For each task position, the offspring randomly inherits the employee assignment from either parent 1 or parent 2 with equal probability.
- This creates diversity in solutions while preserving valuable task-employee pairings from both parents.

**Mutation**: Task-specific mutation is applied to introduce further diversity:

- Each task in a solution has a chance (defined by the mutation rate) of being reassigned.
- When mutation occurs, the task is reassigned to a randomly selected employee from the pool of compatible employees (those with matching skills and adequate skill level).
- This targeted mutation ensures that new assignments are likely to be feasible.

**Elitism**: To preserve the best solutions across generations:

- A specified number (elitism count) of the best solutions from the current population are directly copied to the next generation.
- This ensures that high-quality solutions are not lost during the evolutionary process.

#### Fitness Evaluation

The fitness of each solution is evaluated using the project wide cost function that aggregates penalties for constraint violations:

- Skill mismatch penalty: When employees lack required skills for assigned tasks.
- Overload penalty: When employees are assigned more work hours than available.
- Difficulty violation penalty: When tasks are assigned to employees with insufficient skill levels.
- Deadline violation penalty: When task completions exceed specified deadlines.
- Unique assignment violation penalty: When tasks are not assigned to exactly one employee.

The algorithm aims to minimise this cost, with a perfect solution having a cost of zero (no constraint violations).

#### Termination Criteria

The algorithm terminates when either:

- A predefined maximum number of generations is reached, or
- A perfect solution (cost = 0) is found.

Upon termination, the best solution found across all generations is reported as the final result.


### 2.1.2 Particle Swarm Optimisation (PSO)

**Overview**

The Particle Swarm Optimisation (PSO) algorithm is a meta heuristic that iteratively improves a candidate solution by reducing the cost function, without making assumptions about the underlying problem. It uses a population (swarm) of candidate solutions, known as particles. Each particle explores the defined search space while tracking two key metrics: its personal best ($pBest$), which is the best solution it has found so far, and the global best ($gBest$), which is the best solution found by any particle.

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

**Overview** 

The Ant Colony Optimisation Algorithm is a metaheuristic algorithm inspired by the real life behaviour of ant colonies optimising the path between the colony and a food source through pheromone communication. As an ant returns to its colony from the food source it deposits pheromone along its path, this serves as a signal to other ants who are more likely to follow paths to the food source with a higher pheromone concentration. Pheromone evaporates over time, this results in paths that are longer (less optimized) having a weaker pheromone signal by the time the ant returns to the colony compared to more optimal paths. Over time, these optimal paths generate higher concentrations of pheromone due to repeated deposition, encouraging more ants to take the same path who each deposit pheromone, further reinforcing the efficiency of the route. This positive feedback mechanic results in the convergence of all ants to a near optimal route. 

**Application to Employee Task Assignment Problem**
The concept of ACO can be applied to combinatorial problems such as the Employee Task assignment optimisation problem by considering a whole solution as the path with a length equal to a cost function that describes how far away the solution is from meeting the given constraints. Each individual decision, employee-task assignment, that composes the solution is then deposited with pheromone influencing the probability of that employee-task pairing being chosen in subsequent solutions. Over time the pairings that compose solutions with lower cost are favoured as pheromone is continuosly deposited, and those composing higher cost solutions are disfavoured as pheromone evaporation outweighs the minimal pheromone deposited.

#### Ant Colony Represenation
- The ant colony is represented by a 2D-array where `antMatrix[i]` represents the solution or path taken by the i'th ant.
- `antMatrix[i][t] = e` means that in the i'th ant's solution, task t has been assigned to employee e.
- Every iteration the 2D-array is updated with new solutions that are generated probabilistically based on the values stored in the pheromone matrix.
- A 1D-array stores the global best solution; the solution with the lowest cost found over all iterations run so far.

#### Pheromone Matrix Representation
- The pheromone matrix is represented by a 2D-array where `pherMatrix[t][e] = p` means that the assignment of employee e to task t has a pheromone value of p.

**Initialisation Mechanism** 
The pheromone matrix is initialised with a constrained solution space. All employee task pairings `[t][e]` are evaluated according to if the pairing violates the skill level constraint and the specialized skill matching constraint. If the pairing of employee e with task t violates either constraint then `[t][e] = 0`, otherwise `[t][e] = initial pheromone value parameter`. This process effectively reduces the solution space, as pairings with a pheromone value of 0 can never be chosen by the ant, and so can never have a pheromone deposited upon it. In the edge case that their exists a task t where no employee can satisfy both the skill level and specialized skill matching constraints, all `[t][e]` are assigned a value equal to the initial pheromone parameter for that specific task t.

**Updating Mechanism**
- After each iteration all elements in the pheromone matrix are multiplied by `1 - (pherDecayRate)`, where pherDecayRate is the decimal representation of the percentage decrease in pheromone strength; this process implements the concept of pheromone evaporation. 
- The cost for each ant's solution is then evaluated for the current iteration.
- The pheromone value for that solution is then evaluated according to the equation: `pheromone = 1/(5 * cost + 1) `.
- This pheromone value is then incremented into the elements for all employee task pairs that compose the solution using the for loop:
``` java 
for(int j = 0; j < numTasks; j++) 
{
    int empIdx = ant[j];
    this.pherMatrix[j][empIdx] += pheromone;
    
}
```
where ant is a 1D array for a specific solution from the antMatrix.

#### Solution Generation Mechanism
- In each iteration the number of independent solutions generated is equal to the `numAnts` parameter.
- Each solution is generated sequentially from Task 1 to Task n, and assigning an employee at each different task. 
- For each task in a solution, the pheromone value of assigning that task to each employee is summed and stored as `totalPheromone`.
- A random number is then generated between 0 and `total pheromone` and assigned to `choice`. 
- We then iterate through each element representing the pheromone value for assigning each employee to that task, and on each iteration the respective pheromone value is incremented to the variable `cumulative` and `choice < cumulative` is checked. When this condition is true the respective employee is the one that is chosen for the solution. This ensures probabilistic assignment of employees for each task based on their relative pheromone strengths.
- This process is repeated for all ants in the antMatrix.

## 2.2 Implementation Details

### 2.2.1 Solution Encoding Strategy

Our solutions are encoded in a 1D array `Solution`, which represents the most optimal solution—*the one with the lowest cost*. The array has a size of $n$, where $n$ is the number of tasks in the original problem.

Each element in the array corresponds to a task whose value represents the employee to whom the task is assigned. For example, if `Solution[1] = 3`, it means that $task_1$  is assigned to $employee_3$. i.e a task with an id of 1 is assigned to an employee with an id of 3.

This array representation is simple yet efficient and ensures the most optimal use of memory and allows for fast access and manipulation in $O(1)$ constant time.

### 2.2.2 Constraint Handling Mechanisms

Constraints are managed in a multitude of ways. 
The most prominent contributor is the `Alogirithms.CostCalulator.java` class which implements the cost function.
Additionally, employee task pairs that do not meet both the skill level and specialized skill matching constraint are not considered except for the edge case where no employee meets both constraints for a given task.

### Cost Function
The cost function quantifies how "bad" or inefficient a particular task assignment solution is by summing penalties for violating key constraints. The objective is to minimise this cost, ensuring a more optimal and feasible task-to-employee allocation.

It is defined as per the assignment spec:
$$\text{Cost} = \beta \cdot \text{Skill Mismatch Penalty} + \alpha \cdot \text{Overload Penalty} + \delta \cdot \text{Difficulty Violation Penalty} + \gamma \cdot \text{Deadline Violation Penalty} + \sigma \cdot \text{Unique Assignment Violation Penalty}$$

Each term represents a different type of constraint violation or inefficiency:
- **Overload Penalty ($\alpha$):** Penalises solutions where an employee is assigned more task hours than their available working hours.
- **Skill Mismatch Penalty ($\beta$):** Applies when a task is assigned to an employee who lacks the specific skill required for the task.
- **Difficulty Violation Penalty ($\delta$):** Incurred when a task is assigned to an employee whose skill level is below the task’s difficulty.
- **Deadline Violation Penalty ($\gamma$):** Applies if a task is completed too early (before its deadline).
- **Unique Assignment Violation Penalty ($\sigma$):** Penalises cases where tasks are either not assigned at all or assigned to more than one employee.


### 2.2.3  Algorithm Parameters





## 2.3 Experimental Setup

### 2.3.1 Data Set Description

For our experimental evaluation, we generated synthetic datasets to test the scalability and effectiveness of our task allocation algorithms across various scenarios. The data generation process was designed to create realistic task-employee matching scenarios with controlled parameters.

### Data Generation Methodology

Our synthetic data generator creates two primary entities:
1. **Tasks**: Each task is characterized by:
    - A unique identifier
    - A skill requirement (randomly selected from a predefined set)
    - Estimated completion time (1-9 hours)
    - Difficulty level (1-9)
    - Deadline (1-29 days)
2. **Employees**: Each employee is defined by:
    - A unique identifier
    - Available working hours (7-19 hours)
    - Skill proficiency level (1-9)
    - A random subset of skills from the predefined skill set

The generator ensures variability in the dataset by utilizing pseudorandom distribution of attributes while maintaining realistic constraints on parameters such as working hours and skill distributions.
### 2.3.2 Experiment Setup and Configuration

To comprehensively evaluate our algorithm under different operational conditions, we generated eight distinct test scenarios varying in scale and resource balance:

| Test ID | Tasks | Employees | Resource Ratio | Scenario Description                         |
| ------- | ----- | --------- | -------------- | -------------------------------------------- |
| Test 1  | 10    | 10        | 1:1            | Baseline balanced scenario                   |
| Test 2  | 10    | 100       | 1:10           | Resource abundance scenario                  |
| Test 3  | 100   | 10        | 10:1           | Resource scarcity scenario                   |
| Test 4  | 100   | 100       | 1:1            | Medium-scale balanced scenario               |
| Test 5  | 100   | 500       | 1:5            | Medium tasks with high resource availability |
| Test 6  | 500   | 10        | 50:1           | Extreme resource constraint scenario         |
| Test 7  | 500   | 100       | 5:1            | Large-scale constrained resources            |
| Test 8  | 500   | 500       | 1:1            | Large-scale balanced scenario                |

This systematic variation in problem size and resource balance allows us to evaluate both the computational efficiency and solution quality of our proposed algorithm across a spectrum of operational conditions, from small teams with few tasks to enterprise-scale resource allocation problems. All generated datasets were exported to CSV format and stored in individually labelled directories.


## 2.3.3 Hardware and Software Specifications

The tests were run on a 2020 MacBook Pro with 8GB RAM featuring an Apple M1 processor. The software environment utilized JDK 23 for all experimental evaluations.



# 3. Performance Evaluation

## 3.1 Solution Quality and Optimality

### 3.1.1 Graphical Analysis
### 3.1.2 Interpretation of Results

## 3.2 Computational Efficiency

### 3.2.1 Graphical Representation of Efficiency
### 3.2.2 Analysis of Resource Usage







Datasets:
10 trials
Test1: 10 tasks -> 10 employees
test2: 10 tasks -> 100 employees
test3: 100 tasks -> 10 employees

test4 : 100 tasks -> 100 employees


test5: 100 tasks->500 employees


test6:  500 tasks -> 10 employees


tsest7: 500 tasks-> 100 employees


test 8 : 500 tasks ->500 employees

**Parameter tests

Test1-Mutation-C1-initPherm UP

Test2- CrossOver-c2- pher decay UP


test3-elitismRate- w UP by 0.3 
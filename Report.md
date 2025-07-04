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

```java
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

### Common Parameters

**Population Size**
`populationSize`
This parameter defines the number of independent solutions generated in each iteration of an algorithm.

**Number of Iterations** 
`maxIterations`
This parameter defines the maximum number of iterations that will be completed before an algorithm stops running and returns the best solution generated.

#### GA Specific

**Crossover Rate**
`crossoverRate`
This parameter defines the probability (0.0-1.0) that two parent solutions will undergo crossover to create offspring.

**Mutation Rate**
`mutationRate`
This parameter determines the probability (0.0-1.0) that a task assignment in a solution will be randomly changed to a different employee.

**Elitism Count**
`elitismCount`
This parameter specifies the number of best solutions that are preserved unchanged between generations.

#### ACO Specific

**Pheromone Decay Rate**
`pherDecayRate` class field in `AntColAlg` class.
This parameter defines the percentage decrease of all pheromone values after each iteration represented as a decimal value.

**Initial Pheromone Value**
`initPheromone` class field in `AntColAlg` class.
This parameter defines the pheromone strength assigned to every employee task pairing before any ants have constructed a solution. 

#### PSO Specific

**Personal Best Weight**
`C1`
This parameter controls the influence of a particle's own best-found position on its movement.

**Global Best Weight**
`C2`
This parameter determines the influence of the swarm's best-found position on each particle's movement.

**Inertia Weight**
`W`
This parameter controls how much a particle maintains its current velocity when updating its position.

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

Our experiments evaluated two distinct segments of factors affecting performance: 
- DataSet Ratio
- Parameter Configuration

**DataSet Ratio**
To comprehensively evaluate our algorithm under different operational conditions, we generated eight distinct dataset scenarios varying in scale and resource balance:

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
All of these tests ran with the same default algorithm parameters that are stored in `root/testData/test1-DEFAULT.csv`
```
maxIterations,reportingFrequency,fileOutput,populationSize,mutationRate,crossoverRate,elitismCount,c1,c2,w,initPheromone,pherDecayRate
100,10,1,100,0.1,0.1,2,1.5,1.5,0.5,0.1,0.1
```

This systematic variation in problem size and resource balance allows us to evaluate both the computational efficiency and solution quality of our proposed algorithm across a spectrum of operational conditions, from small teams with few tasks to enterprise-scale resource allocation problems. All generated datasets were exported to CSV format and stored in individually labelled directories.

**Parameter Configuration**
Keeping the dataset at a standard of 200 employees and 50 tasks, the parameters were tweaked as such for 4 distinct sets.


| Test ID | Increase/Decrease | Parameters                       | Scenario description                         |
| ------- | ----------------- | -------------------------------- | -------------------------------------------- |
| Test 1  | Baseline          | All as seen above                | The lowest, baseline                         |
| Test 2  | Increase          | mutation rate, c1, initPheromone | Individual parameter increases per algorithm |
| Test 3  | Increase          | crossoverRate, c2, pherDecayRate | Individual parameter increases per algorithm |
| Test 4  | increase          | elitismCount, w                  | Individual parameter increases per algorithm |

By only changing one parameter per algorithm per run, we were able to observe the affect each parameter had on the overall outcome. Every other possible factor was kept consistent for each algorithm.

All these tests were run multiple times and their results were averaged to produce the graphs attached.


## 2.3.3 Hardware and Software Specifications

The tests were run on a 2020 MacBook Pro with 8GB RAM featuring an Apple M1 processor. The software environment utilised JDK 23 for all experimental evaluations.

# 3. Performance Evaluation

## 3.1 Solution Quality and Optimality

#### 1:1 Ratio (Task 1) - Equal Tasks and Employees

![[Pasted image 20250502225910.png]]

In the 1:1 ratio scenario, all three algorithms show distinctly different performance levels:

- **Genetic Algorithm (GA)**: Shows the best performance, starting at cost ~1.8 and rapidly improving to converge at ~1.4
- **Particle Swarm Optimization (PSO)**: Maintains a consistent cost of 2.0 throughout all iterations
- **Ant Colony Optimization (ACO)**: Performs significantly worse with a stable cost around 3.6

**Key insights**:

- GA demonstrates its adaptive capability even in simple problem spaces
- PSO shows no improvement over iterations, suggesting it quickly reaches its local optimum
- ACO performs poorly relative to other approaches in this balanced configuration
- The performance gap between algorithms is very pronounced in this simple case

#### 1:10 Ratio (Task 2) - More Employees than Tasks

![[Pasted image 20250502230100.png]]

When there are significantly more employees than tasks (1:10 ratio):

- **Genetic Algorithm (GA)**: Starts with a poor solution (~1.6) but quickly improves and reaches ~1.05
- **Particle Swarm Optimization (PSO)**: Performs better than in the 1:1 case, with cost around 1.1-1.2
- **Ant Colony Optimization (ACO)**: Shows dramatic improvement compared to the 1:1 case, achieving the best final cost of ~1.0

**Key insights**:

- ACO performs exceptionally well with employee surplus, suggesting it excels at employee selection when options are plentiful
- The performance gap between algorithms narrows significantly
- All algorithms achieve good solutions, indicating this is an easier configuration to optimize
- GA shows the most improvement over iterations despite not achieving the best final solution

#### 5:1 Ratio (Task 7) - More Tasks than Employees

![[Pasted image 20250502230318.png]]

In the task-heavy scenario with a 5:1 ratio:

- **Genetic Algorithm (GA)**: Achieves the best performance (~825), showing consistent improvement
- **Particle Swarm Optimization (PSO)**: Performs slightly worse (~875) with minimal improvement over iterations
- **Ant Colony Optimization (ACO)**: Shows the poorest performance (~950) but still improves gradually

**Key insights**:

- GA maintains its advantage in complex scenarios with constrained resources
- The cost values are much higher for all algorithms, indicating this is a more challenging optimization problem
- ACO struggles the most when employees are the constraining factor
- All algorithms show some improvement, suggesting none reach their true optimal solution within 100 iterations

### 3.1.2 Interpretation of Results

1. **Algorithm Sensitivity to Problem Structure**:
   - GA shows consistent strong performance across all ratios, particularly excelling in resource-constrained scenarios
   - PSO performs reliably but with limited improvement capacity
   - ACO shows dramatic performance variation based on ratio - performing poorly with balanced or constrained employees but excelling when many employees are available
2. **Impact of Resource Constraints**:
   - Employee surplus (1:10) leads to better solutions across all algorithms
   - Task surplus (5:1) creates a much more challenging optimisation problem with higher costs
3. **Convergence Patterns**:
   - GA consistently shows the most improvement over iterations regardless of ratio
   - ACO's improvement pattern varies significantly based on problem structure
   - PSO tends to find reasonably good initial solutions but shows limited further improvement

## 3.2 Computational Efficiency

### 3.2.1 Graphical Representation of Efficiency

The computational efficiency of each algorithm was evaluated across different problem scales using both runtime (in milliseconds) and memory usage (in MB) metrics. The graphs presented in Images 1-12 illustrate these performance characteristics.

### Runtime Performance Analysis

Runtime measurements reveal significant differences in computational efficiency among the three algorithms:

![[SavedRunData/DataSetTests/run(1)/charts/computational_efficiency_avg_runtime_run1.png]]
Dataset-Run1-AvgRuntime/Iteration

![[computational_efficiency_avg_runtime_run7.png]]
Dataset-Run7

![[SavedRunData/DataSetTests/run(7)/charts/computational_efficiency_avg_runtime.png]]
Dataset-Run8

![[SavedRunData/DataSetTests/run(6)/charts/computational_efficiency_avg_runtime.png]]
Dataset-Run-6

![[SavedRunData/ParameterTests/run(1)/charts/computational_efficiency_avg_memory_run1.png]]
Dataset-Run1

![[computational_efficiency_avg_memory_run8.png]]
Dataset-Run8

![[computational_efficiency_total_memory_run7.png]]
Dataset-Run7


**Small-Scale Problems (Test 1 - 10 tasks, 10 employees):**

- Dataset-Run1 shows that for small instances, ACO achieves the best performance with approximately 16ms average runtime per iteration
- PSO follows with around 27ms per iteration
- GA requires considerably more processing time at roughly 55ms per iteration

**Medium-Scale Problems (Test 3 - 100 tasks, 10 employees):**

- Dataset-Run7 demonstrates that as the problem size increases, the runtime differences become more pronounced
- ACO maintains the best performance at around 32ms per iteration
- PSO requires approximately 52ms per iteration
- GA's runtime increases significantly to about 115ms per iteration

**Large-Scale Problems (Test 7 - 500 tasks, 100 employees):**

- Dataset-Run8 shows the most dramatic differences in computational requirements
- ACO remains the most efficient at approximately 140ms per iteration
- PSO requires about 270ms per iteration
- GA's computational cost increases dramatically to around 500ms per iteration

**Extreme-Scale Problems (Test 6 - 500 tasks, 10 employees):**

- Dataset-Run6 reveals that in extreme task-to-employee ratio scenarios, all algorithms require substantially more processing time
- ACO still performs best at around 14,000ms per iteration
- PSO follows at approximately 27,000ms per iteration
- GA requires nearly 50,000ms per iteration

### Memory Usage Patterns

Memory consumption patterns show different characteristics compared to runtime:

**Per-Iteration Memory Usage:**

- All three algorithms exhibit remarkably similar memory efficiency on a per-iteration basis
- For small problems (Dataset-Run1), all algorithms use approximately 34MB per iteration
- For medium problems (Dataset-Run8), memory usage increases to around 55MB per iteration
- The differences between algorithms remain minimal across all problem sizes

**Total Memory Usage (Images Dataset-Run7, Dataset-Run8, Dataset-Run1):**

- Images mentioned before show the total memory consumption across all iterations
- All algorithms demonstrate similar total memory usage patterns
- ACO typically uses slightly less total memory than PSO and GA
- For medium problems (Dataset-Run8) , ACO consumes about 5,400MB total, compared to PSO's 5,600MB
- The differences are not as substantial as those observed in runtime metrics

## 3.2.2 Analysis of Resource Usage

The analysis of computational efficiency reveals several key insights about how these algorithms utilise computational resources:

### Runtime Scaling Behaviour

1. **ACO's Superior Efficiency:**
    - ACO consistently demonstrates the lowest runtime across all problem sizes
    - This efficiency stems from its simplified decision-making process, where each ant makes probabilistic choices based on a pheromone matrix without complex calculations
    - The runtime advantage increases with problem size, making ACO particularly valuable for large-scale task assignment scenarios
2. **PSO's Moderate Performance:**
    - PSO maintains an intermediate position in terms of runtime efficiency
    - Its performance degradation as problem size increases is more pronounced than ACO but less severe than GA
    - The algorithm's need to calculate both cognitive and social components in velocity updates contributes to its higher computational requirements compared to ACO
3. **GA's High Computational Cost:**
    - GA consistently exhibits the highest runtime requirements across all problem scales
    - This performance characteristic is attributable to its more complex evolutionary operations, particularly the selection, crossover, and mutation processes
    - The algorithm's computational cost grows dramatically with problem size, making it less suitable for time-sensitive applications with large datasets

### Problem Size Impact

1. **Task-to-Employee Ratio Effect:**
    - The computational efficiency of all algorithms is significantly affected by the task-to-employee ratio
    - Test 6 (500 tasks, 10 employees) shows dramatically higher runtimes than Test 7 (500 tasks, 100 employees), despite having fewer total assignments to manage
    - This suggests that highly constrained resource scenarios require more extensive exploration of the solution space, increasing computational demands
2. **Scaling Patterns:**
    - Runtime increases non-linearly with problem size for all algorithms
    - When comparing Test 1 (10 tasks, 10 employees) to Test 7 (500 tasks, 100 employees), we observe runtime increases of:
        - ACO: ~9× increase (16ms to 140ms)
        - PSO: ~10× increase (27ms to 270ms)
        - GA: ~9× increase (55ms to 500ms)
    - This suggests similar scaling characteristics across algorithms, despite their different absolute performance levels

### Memory Usage Insights

1. **Algorithm Implementation Efficiency:**
    - The similar memory consumption patterns across all three algorithms suggest that their memory usage is primarily determined by the problem representation rather than algorithm-specific data structures
    - The core data structure (task-employee assignment array) is shared across all implementations, contributing to this uniformity
2. **Memory-Runtime Tradeoffs:**
    - While ACO achieves superior runtime performance, it does not come at the cost of increased memory consumption
    - This indicates that ACO's efficiency stems from algorithmic simplicity rather than memory-intensive optimisations
3. **Practical Implications:**
    - For applications where memory is constrained but processing power is available, all three algorithms remain viable options
    - In scenarios with limited processing power but sufficient memory, ACO would be the preferred choice
    - For time-critical applications dealing with large datasets, the substantially lower runtime of ACO provides a clear advantage

These findings demonstrate that while all three algorithms can effectively solve the Employee Task Assignment problem, ACO offers the best computational efficiency across all tested scenarios, making it particularly valuable for large-scale or real-time applications where processing time is a critical factor.

# 4.Results and Discussion
### 4.1 Analysis Comparison: GA vs. PSO vs. ACO

### Computational Efficiency
- **ACO**: Fastest algorithm (6ms/iteration for small problems, 140ms for large)
- **PSO**: Moderate efficiency (12ms/iteration for small problems, 270ms for large)
- **GA**: Highest computational cost (20ms/iteration for small problems, 500ms for large)

#### Solution Quality
- **GA**: Best overall solutions, particularly in resource-constrained scenarios
- **PSO**: Reliable performance with good initial solutions but limited improvement
- **ACO**: Performance varies significantly by problem structure; excels with employee surplus

#### Employee-Task Ratio Impact
- **1:1 ratio**: GA dominates (cost ~1.4 vs PSO ~2.0, ACO ~3.6)
- **1:10 ratio** (employee surplus): ACO performs best (~1.0), followed by GA (~1.05) and PSO (~1.1)
- **5:1 ratio** (task surplus): GA maintains advantage (~825 vs PSO ~875, ACO ~950)

#### Practical Selection Guidance
- **Choose GA**: When solution quality is paramount and computational resources are ample
- **Choose PSO**: For balanced performance between quality and efficiency
- **Choose ACO**: For time-critical applications, especially with employee surplus

All algorithms generally stabilize after 20-40 iterations, with memory usage patterns remaining similar across implementations.
## 4.2 Insights 

### 4.2.1 Genetic Algorithm 
GA demonstrates consistent and reliable performance across all employee-to-task ratio scenarios. Its effectiveness arises from its ability to balance exploitation through the elitism mechanism and exploration through controlled mutation. Solutions evolve over generations as parent assignments are recombined with a probability defined by the crossoverRate, and occasionally mutated to introduce diversity. The algorithm performs especially well in resource-constrained problems, such as when there are significantly more tasks than employees, where the elitismCount preserves high-quality solutions and the mutationRate enables escape from local minima. While the computational cost of managing a population is higher compared to swarm-based methods, the algorithm shows rapid convergence to low-cost solutions when its parameters are properly tuned.

### 4.2.2 Particle Swarm Optimisation
Particle Swarm Optimisation relies on swarm intelligence, where each particle represents a solution moving through the search space, influenced by both its own best position and the best-known position of the swarm. This dual attraction, governed by the parameters c1 and c2, allows the algorithm to converge quickly to reasonable solutions. However, its tendency to plateau after early iterations indicates a lack of deep exploration, particularly when the inertia weight w is not tuned to maintain exploration momentum. PSO is computationally efficient due to its simplicity and minimal memory requirements, making it suitable for large-scale problems with many employees, but its final solution quality often suffers in scenarios with tight constraints, where greater variability in search is required.

### 4.2.3 Ant Colony Optimisation
Ant Colony Optimisation applies a pheromone-based reinforcement mechanism where employee-task pairings that are part of low-cost solutions are incrementally strengthened over time. Its performance varies significantly with the structure of the problem. In balanced or employee-constrained configurations, such as 1:1 or 5:1 ratios, the algorithm struggles as early deposits of pheromone on suboptimal pairings can lead to premature convergence. However, in cases with employee surplus, such as the 1:10 ratio, ACO benefits from the wider selection space, allowing its adaptive learning process to reinforce efficient pairings over time. The initPheromone and pherDecayRate parameters are particularly influential, as they determine the relative importance of new versus historical information. When tuned correctly, ACO demonstrates strong improvement patterns in scenarios where flexibility and redundancy in assignments allow the pheromone-driven mechanism to shine.


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

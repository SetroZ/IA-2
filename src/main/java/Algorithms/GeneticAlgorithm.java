package Algorithms;

import Model.Employee;
import Model.Task;
import Utilities.Initialise;
import Utilities.Observer;
import Utilities.ObserverException;
import Utilities.Subject;

import java.io.Console;
import java.util.*;

/**
 * Genetic Algorithm implementation
 */
public class GeneticAlgorithm implements Algorithm, Subject {

    // Algorithm parameters
    private final int populationSize;
    private final double crossoverRate;
    private final double mutationRate;
    private final int maxGenerations;
    private final int elitismCount;
    private final int REPORTING_FREQUENCY;
    private final boolean fileOutput;

    // Problem data
    private final List<Task> tasks;
    private final List<Employee> employees;

    // Tracking and reporting
    private List<Observer> observers = new ArrayList<>();
    private String output = "";
    private int[] globalBestSolution;
    private double globalBestCost = Double.MAX_VALUE;
    private List<Double> bestCostHistory = new ArrayList<>();
    private List<Double> avgCostHistory = new ArrayList<>();
    private List<Integer> feasibleSolutionsHistory = new ArrayList<>();

    /**
     * Constructor for the Genetic Algorithm.
     *
     * @param tasks              The list of tasks to be assigned
     * @param employees          The list of employees available for solution
     * @param populationSize     Size of the population (number of solutions)
     * @param crossoverRate      Probability of crossover (0.0-1.0)
     * @param mutationRate       Probability of mutation (0.0-1.0)
     * @param maxGenerations     Maximum number of generations to run
     * @param reportingFrequency The frequency of progress reports printed to the
     *                           console.
     * @param fileOutput         Whether to output results to a file
     */
    public GeneticAlgorithm(List<Task> tasks, List<Employee> employees,
            int populationSize, double crossoverRate, double mutationRate,
            int maxGenerations, int reportingFrequency, boolean fileOutput) {
        this.tasks = tasks;
        this.employees = employees;
        this.populationSize = populationSize;
        this.crossoverRate = crossoverRate;
        this.mutationRate = mutationRate;
        REPORTING_FREQUENCY = reportingFrequency;
        if (maxGenerations == -1) {
            this.maxGenerations = Integer.MAX_VALUE;
        } else {
            this.maxGenerations = maxGenerations;
        }
        this.elitismCount = 2; // Keep the best 2 solutions
        this.fileOutput = fileOutput;
    }

    /**
     * Runs the genetic algorithm to find an optimal solution.
     *
     * @return The best Solution found
     */

    @Override
    public void run() {

        // Initialize population
        int[][] population = Initialise.getInitialPopulation(employees, tasks, populationSize);

        int generation = 0;

        globalBestSolution = findBestSolution(population);
        globalBestCost = CostCalculator.calculateTotalCost(globalBestSolution, tasks, employees);

        // recordStatistics(population, generation);

        // Main loop
        while (generation < maxGenerations && !terminationCondition(population)) {
            int[][] newPopulation = new int[populationSize][tasks.size()];

            // Initialise counter for populated solutions
            int counter = 0;

            // Add elite solutions to new population
            int[][] eliteSolutions = findBestSolution(population, elitismCount);

            for (counter = 0; counter < elitismCount && counter < population.length; counter++) {
                newPopulation[counter] = eliteSolutions[counter].clone();
            }

            // Fill the rest of the population with offspring
            while (counter < populationSize) {
                // Selection
                int[][] parents = selectParents(population);

                // Crossover
                int[] offspring1 = null;
                int[] offspring2 = null;

                if (Math.random() < crossoverRate) {
                    offspring1 = crossover(parents[0], parents[1]);
                    offspring2 = crossover(parents[1], parents[0]);
                } else {
                    offspring1 = parents[0].clone();
                    offspring2 = parents[1].clone();
                }

                // Mutation
                mutate(offspring1);
                mutate(offspring2);

                // Add to new population
                newPopulation[counter] = offspring1.clone();
                counter++;

                if (counter < populationSize) {
                    newPopulation[counter] = offspring2.clone();
                    counter++;
                }
            }

            // Replace old population with new population
            population = newPopulation;

            // Update global best
            int[] currentBest = findBestSolution(population);
            double currentBestCost = CostCalculator.calculateTotalCost(currentBest, tasks, employees);

            if (currentBestCost < globalBestCost) {
                globalBestSolution = currentBest;
                globalBestCost = currentBestCost;
            }

            // Record statistics
            recordStatistics(population);

            // Print progress
            if (generation % REPORTING_FREQUENCY == 0 || generation == maxGenerations - 1) {
                printProgress(currentBest, generation);
            }

            generation++;
        }
        System.out.println(globalBestSolution);
        // Print final result
        printFinalResult(globalBestSolution, generation);
    }

    /**
     * Records statistics for the current generation.
     *
     * @param population The current population
     */
    private void recordStatistics(int[][] population) {
        // Record best cost
        double bestCost = CostCalculator.calculateTotalCost(findBestSolution(population), tasks, employees);
        bestCostHistory.add(bestCost);

        // Record average cost
        double totalCost = 0;
        for (int[] solution : population) {
            totalCost += CostCalculator.calculateTotalCost(solution, tasks, employees);
        }
        double avgCost = totalCost / populationSize;
        avgCostHistory.add(avgCost);

        // Record feasible solutions count
        int feasibleCount = 0;
        for (int[] solution : population) {
            if (ConstraintValidator.isSolutionFeasible(solution, tasks, employees)) {
                feasibleCount++;
            }
        }
        feasibleSolutionsHistory.add(feasibleCount);
    }

    /**
     * Finds the best solution in the population.
     *
     * @param population The population to search
     * @return The best Solution
     */
    private int[][] findBestSolution(int[][] population, int numSolutions) {
        // Create an array to store individuals and their costs
        List<int[]> individuals = new ArrayList<>();
        List<Double> costs = new ArrayList<>();

        // Calculate the cost of each individual in the population
        for (int[] individual : population) {
            double cost = CostCalculator.calculateTotalCost(individual, tasks, employees);
            individuals.add(individual);
            costs.add(cost);
        }

        // Sort individuals by cost (ascending)
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < costs.size(); i++) {
            indices.add(i);
        }
        indices.sort(Comparator.comparingDouble(costs::get));

        // Select the top numSolutions individuals
        int[][] best = new int[numSolutions][tasks.size()];
        for (int i = 0; i < numSolutions; i++) {
            best[i] = individuals.get(indices.get(i));
        }

        return best;
    }

    private int[] findBestSolution(int[][] population) {
        // Create an array to store individuals and their costs
        int[] best = population[0];
        double bestCost = CostCalculator.calculateTotalCost(population[0], tasks, employees);

        for (int i = 1; i < population.length; i++) {
            double cost = CostCalculator.calculateTotalCost(population[i], tasks, employees);
            if (cost < bestCost) {
                best = population[i];
            }
        }
        return best;
    }

    /**
     * Selects two parent solutions using tournament selection.
     *
     * @param population The population to select from
     * @return Array of two selected parent Solutions
     */
    private int[][] selectParents(int[][] population) {
        int[][] parents = new int[2][tasks.size()];

        // Tournament selection
        for (int i = 0; i < 2; i++) {
            int tournamentSize = 3;
            int[][] tournament = new int[tournamentSize][tasks.size()];

            for (int j = 0; j < tournamentSize; j++) {
                int randomIndex = new Random().nextInt(populationSize);
                tournament[j] = population[randomIndex];
            }

            parents[i] = findBestSolution(tournament);
        }

        return parents;
    }

    /**
     * Performs crossover between two parent solutions.
     *
     * @param parent1 The first parent Solution
     * @param parent2 The second parent Solution
     * @return A new Solution created by crossover
     */
    private int[] crossover(int[] parent1, int[] parent2) {
        int[] offspring = new int[tasks.size()];

        // Uniform crossover - for each task, choose solution from either parent1 or
        // parent2
        for (Task task : tasks) {
            int taskIdx = task.getIdx();

            if (Math.random() < 0.5) {
                offspring[taskIdx] = parent1[taskIdx];
            } else {
                offspring[taskIdx] = parent2[taskIdx];
            }
        }

        return offspring;
    }

    /**
     * Applies mutation to a solution.
     *
     * @param solution The Solution to mutate
     */
    private void mutate(int[] solution) {
        for (Task task : tasks) {
            if (Math.random() < mutationRate) {
                // Get the current assigned employee
                int currentEmployeeIdx = solution[task.getIdx()];

                // Create a list of employees who can perform this task
                List<Employee> compatibleEmployees = new ArrayList<>();
                for (Employee employee : employees) {
                    if (employee.hasSkill(task.getRequiredSkill()) &&
                            employee.getSkillLevel() >= task.getDifficulty()) {
                        compatibleEmployees.add(employee);
                    }
                }

                // If we have compatible employees, choose one randomly
                if (!compatibleEmployees.isEmpty()) {
                    int randomIndex = new Random().nextInt(compatibleEmployees.size());
                    int newEmployeeIdx = compatibleEmployees.get(randomIndex).getIdx();
                    solution[task.getIdx()] = newEmployeeIdx;
                }
            }
        }
    }

    /**
     * Checks if termination condition is met.
     *
     * @param population The current population
     * @return true if termination condition is met, false otherwise
     */
    private boolean terminationCondition(int[][] population) {
        // Check if we have a perfect solution (zero cost)
        return globalBestCost == 0;
    }

    /**
     * Prints the progress of the current generation.
     *
     * @param bestSolution The best Solution in the current generation
     * @param generation   The current generation number
     */
    private void printProgress(int[] bestSolution, int generation) {
        StringBuilder sb = new StringBuilder();
        double cost = CostCalculator.calculateTotalCost(bestSolution, tasks, employees);

        sb.append("Generation ").append(generation)
                .append(": Best Cost = ").append(String.format("%.2f", cost))
                .append(", Feasible: ").append(ConstraintValidator.isSolutionFeasible(bestSolution, tasks, employees))
                .append("\n");

        output += sb.toString();

        if (!fileOutput) {
            System.out.print(sb.toString());
        }
    }

    /**
     * Prints the final result of the algorithm.
     *
     * @param bestSolution The best Solution found
     * @param generation   The final generation number
     */
    private void printFinalResult(int[] bestSolution, int generation) {

        double cost = CostCalculator.calculateTotalCost(bestSolution, tasks, employees);
        boolean isFeasilble = ConstraintValidator.isSolutionFeasible(bestSolution, tasks, employees);

        String finalResult = observers.getFirst().getFinalSolution(bestSolution, cost, generation, isFeasilble);

        if (fileOutput) {
            output += finalResult;
            try {
                notifyObservers("FILE", "geneticAlg", output);
            } catch (ObserverException e) {
                notifyObservers("ERROR", "Writing To File", e.getMessage());
            }
        } else {
            notifyObservers("INFO", "GENETIC ALGORITHM RESULT", finalResult);
        }
    }

    /**
     * Helper method to find an employee by ID.
     *
     * @param employeeId The ID of the employee to find
     * @return The Employee with the specified ID, or null if not found
     */
    private Employee findEmployeeById(String employeeId) {
        for (Employee employee : employees) {
            if (employee.getId().equals(employeeId)) {
                return employee;
            }
        }
        return null;
    }

    /**
     * Gets the best cost history for reporting.
     *
     * @return List of best cost values per generation
     */
    @Override
    public List<Double> getBestCostHistory() {
        return bestCostHistory;
    }

    /**
     * Gets the average cost history for reporting.
     *
     * @return List of average cost values per generation
     */
    @Override
    public List<Double> getAvgCostHistory() {
        return avgCostHistory;
    }

    /**
     * Gets the feasible solutions history for reporting.
     *
     * @return List of feasible solution counts per generation
     */
    @Override
    public List<Integer> getFeasibleSolutionsHistory() {
        return feasibleSolutionsHistory;
    }

    @Override
    public void registerObserver(Observer observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(String messageType, String title, String content) {
        for (Observer observer : observers) {
            observer.update(messageType, title, content);
        }
    }
}
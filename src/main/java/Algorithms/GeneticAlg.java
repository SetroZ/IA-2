package Algorithms;

import Model.Employee;
import Model.Task;
import Utilities.Initialise;
import Utilities.Observer;
import Utilities.PerformanceLogger;

import java.util.*;

/**
 * Genetic Algorithm implementation
 */
public class GeneticAlg extends AbstractOptimisationAlgorithm {

    // Algorithm parameters
    private final double crossoverRate;
    private final double mutationRate;
    private final int elitismCount;

    /**
     * Constructor for the Genetic Algorithm.
     *
     * @param tasks              The list of tasks to be assigned
     * @param employees          The list of employees available for solution
     * @param populationSize     Size of the population (number of solutions)
     * @param crossoverRate      Probability of crossover (0.0-1.0)
     * @param mutationRate       Probability of mutation (0.0-1.0)
     * @param maxIterations      Maximum number of generations to run
     * @param reportingFrequency The frequency of progress reports printed to the
     *                           console.
     * @param fileOutput         Whether to output results to a file
     */

    public GeneticAlg(List<Task> tasks, List<Employee> employees,
            int populationSize, double crossoverRate, double mutationRate,
            int elitismCount, int maxIterations, int reportingFrequency,
            boolean fileOutput) {
        super(tasks, employees, reportingFrequency, fileOutput, maxIterations, populationSize);
        this.crossoverRate = crossoverRate;
        this.mutationRate = mutationRate;
        this.elitismCount = elitismCount;

    }

    /**
     * Runs the genetic algorithm to find an optimal solution.
     */

    @Override
    public void run() {
        // Start timing performance
        performanceLogger.startTimer();

        // Initialize population
        int[][] population = Initialise.getInitialPopulation(employees, tasks, populationSize);

        int generation = 0;

        int[] globalBestSolution = findBestSolution(population);
        double globalBestCost = CostCalculator.calculateTotalCost(globalBestSolution, tasks, employees);

        // Log initial state
        performanceLogger.logIteration(
                generation,
                globalBestSolution,
                globalBestCost,
                PerformanceLogger.getCurrentMemoryUsageMB());
//        // Log initial state
//        performanceLogger.logIteration(
//                generation,
//                globalBestSolution,
//                globalBestCost,
//                PerformanceLogger.getCurrentMemoryUsageMB()
//        );

        // Main loop
        while (generation < maxIterations && !(globalBestCost == 0)) {
            int[][] newPopulation = new int[populationSize][tasks.size()];

            // Initialise counter for populated solutions
            int counter;

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
                int[] offspring1;
                int[] offspring2;

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

            // Print progress
            if (generation % reportinFrequency == 0 || generation == maxIterations - 1) {
                reportProgress(globalBestSolution, generation);
            }

            // Log metrics for this generation
            performanceLogger.logIteration(
                    generation,
                    globalBestSolution,
                    globalBestCost,
                    PerformanceLogger.getCurrentMemoryUsageMB());
                    PerformanceLogger.getCurrentMemoryUsageMB()
            );

            generation++;


        }

        // Stop timer and save all metrics to CSV files
        performanceLogger.stopTimer();
        performanceLogger.saveMetricsToCSV();

        // Print final result
        reportFinalResult(globalBestSolution, generation);
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

    public void notifyObservers(String messageType, String title, String content) {
        for (Observer observer : observers) {
            observer.update(messageType, title, content);
        }
    }

    @Override
    protected String getAlgorithmName() {

    public String getAlgorithmName() {
        return "GeneticAlg";
    }

    @Override
    protected int getMaxIterations() {

    public int getMaxIterations() {
        return maxIterations;
    }
}
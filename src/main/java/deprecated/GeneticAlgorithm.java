package deprecated;
import Algorithms.Algorithm;
import Model.Assignment;
import Model.Employee;
import Model.Task;
import Utilities.ConstraintValidator;
import Utilities.Initialise;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Genetic Algorithm implementation for employee-task assignment optimization.
 * Adapted from a solution originally designed for the 8-Queens problem.
 */
public class GeneticAlgorithm implements Algorithm
{
    // Algorithm parameters
    private final int populationSize;
    private final double crossoverRate;
    private final double mutationRate;
    private final int maxGenerations;
    private final int elitismCount;
    private final boolean fileOutput;

    // Problem data
    private final List<Task> tasks;
    private final List<Employee> employees;

    // Tracking and reporting
    private String output = "";
    private Assignment globalBestAssignment;
    private double globalBestCost = Double.MAX_VALUE;
    private List<Double> bestCostHistory = new ArrayList<>();
    private List<Double> avgCostHistory = new ArrayList<>();
    private List<Integer> feasibleSolutionsHistory = new ArrayList<>();

    /**
     * Constructor for the Genetic Algorithm.
     *
     * @param tasks          The list of tasks to be assigned
     * @param employees      The list of employees available for assignment
     * @param populationSize Size of the population (number of solutions)
     * @param crossoverRate  Probability of crossover (0.0-1.0)
     * @param mutationRate   Probability of mutation (0.0-1.0)
     * @param maxGenerations Maximum number of generations to run
     * @param fileOutput     Whether to output results to a file
     */
    public GeneticAlgorithm(List<Task> tasks, List<Employee> employees,
                            int populationSize, double crossoverRate, double mutationRate,
                            int maxGenerations, boolean fileOutput)
    {
        this.tasks = tasks;
        this.employees = employees;
        this.populationSize = populationSize;
        this.crossoverRate = crossoverRate;
        this.mutationRate = mutationRate;
        if(maxGenerations == -1)
        {
            this.maxGenerations = Integer.MAX_VALUE;
        }
        else{
            this.maxGenerations = maxGenerations;
        }
        this.elitismCount = 2; // Keep the best 2 solutions
        this.fileOutput = fileOutput;
    }

    /**
     * Generates a random employee index within the valid range.
     *
     * @return A random employee index
     */
    private int getRandomEmployeeIndex()
    {
        return new Random().nextInt(employees.size());
    }



    /**
     * Runs the genetic algorithm to find an optimal assignment.
     *
     * @return The best Assignment found
     */
    @Override
    public Assignment run()
    {
        // Initialize population
        List<Assignment> population = Initialise.getInitialPopulation(employees, tasks, populationSize);
        // Evaluate initial population
        evaluatePopulation(population);

        int generation = 0;
        globalBestAssignment = findBestAssignment(population);
        globalBestCost = globalBestAssignment.calculateCost(employees, tasks);

        recordStatistics(population, generation);

        // Main loop
        while (generation < maxGenerations && !terminationCondition(population))
        {
            List<Assignment> newPopulation = new ArrayList<>();

            // Sort population by fitness (lower cost is better)
            sortPopulationByFitness(population);

            // Add elite solutions to new population
            for (int i = 0; i < elitismCount && i < population.size(); i++)
            {
                newPopulation.add(new Assignment(population.get(i)));
            }

            // Fill the rest of the population with offspring
            while (newPopulation.size() < populationSize)
            {
                // Selection
                Assignment[] parents = selectParents(population);
                Assignment parent1 = parents[0];
                Assignment parent2 = parents[1];

                // Crossover
                Assignment offspring1 = null;
                Assignment offspring2 = null;

                if (Math.random() < crossoverRate)
                {
                    offspring1 = crossover(parent1, parent2);
                    offspring2 = crossover(parent2, parent1);
                }
                else
                {
                    offspring1 = new Assignment(parent1);
                    offspring2 = new Assignment(parent2);
                }

                // Mutation
                mutate(offspring1);
                mutate(offspring2);

                // Add to new population
                if (newPopulation.size() < populationSize)
                {
                    newPopulation.add(offspring1);
                }
                if (newPopulation.size() < populationSize)
                {
                    newPopulation.add(offspring2);
                }
            }

            // Replace old population with new population
            population = newPopulation;

            // Evaluate new population
            evaluatePopulation(population);

            // Update global best
            Assignment currentBest = findBestAssignment(population);
            double currentBestCost = currentBest.calculateCost(employees, tasks);

            if (currentBestCost < globalBestCost)
            {
                globalBestAssignment = new Assignment(currentBest);
                globalBestCost = currentBestCost;
            }

            // Record statistics
            recordStatistics(population, generation);

            // Print progress
            if (generation % 10 == 0 || generation == maxGenerations - 1)
            {
                printProgress(currentBest, generation);
            }

            generation++;
        }

        // Print final result
        printFinalResult(globalBestAssignment, generation);

        return globalBestAssignment;
    }

    /**
     * Records statistics for the current generation.
     *
     * @param population The current population
     * @param generation The current generation number
     */
    private void recordStatistics(List<Assignment> population, int generation)
    {
        // Record best cost
        double bestCost = findBestAssignment(population).calculateCost(employees, tasks);
        bestCostHistory.add(bestCost);

        // Record average cost
        double totalCost = 0;
        for (Assignment assignment : population)
        {
            totalCost += assignment.calculateCost(employees, tasks);
        }
        double avgCost = totalCost / population.size();
        avgCostHistory.add(avgCost);

        // Record feasible solutions count
        int feasibleCount = 0;
        for (Assignment assignment : population)
        {
            if (ConstraintValidator.isAssignmentFeasible(assignment, tasks, employees))
            {
                feasibleCount++;
            }
        }
        feasibleSolutionsHistory.add(feasibleCount);
    }

    /**
     * Evaluates the fitness of all assignments in the population.
     *
     * @param population The population to evaluate
     */
    private void evaluatePopulation(List<Assignment> population)
    {
        for (Assignment assignment : population)
        {

            // Fitness is already calculated when needed by the Assignment class
            // This is just a placeholder in case preprocessing is needed
        }
    }

    /**
     * Sorts the population by fitness (lower cost is better).
     *
     * @param population The population to sort
     */
    private void sortPopulationByFitness(List<Assignment> population)
    {
        population.sort(Comparator.comparingDouble(a -> a.calculateCost(employees, tasks)));
    }

    /**
     * Finds the best assignment in the population.
     *
     * @param population The population to search
     * @return The best Assignment
     */
    private Assignment findBestAssignment(List<Assignment> population)
    {
        Assignment best = population.get(0);
        double bestCost = best.calculateCost(employees, tasks);

        for (int i = 1; i < population.size(); i++)
        {
            Assignment current = population.get(i);
            double currentCost = current.calculateCost(employees, tasks);

            if (currentCost < bestCost)
            {
                best = current;
                bestCost = currentCost;
            }
        }

        return best;
    }

    /**
     * Selects two parent assignments using tournament selection.
     *
     * @param population The population to select from
     * @return Array of two selected parent Assignments
     */
    private Assignment[] selectParents(List<Assignment> population)
    {
        Assignment[] parents = new Assignment[2];

        // Tournament selection
        for (int i = 0; i < 2; i++)
        {
            int tournamentSize = 3;
            List<Assignment> tournament = new ArrayList<>();

            for (int j = 0; j < tournamentSize; j++)
            {
                int randomIndex = new Random().nextInt(population.size());
                tournament.add(population.get(randomIndex));
            }

            parents[i] = findBestAssignment(tournament);
        }
        return parents;
    }

    /**
     * Performs crossover between two parent assignments.
     *
     * @param parent1 The first parent Assignment
     * @param parent2 The second parent Assignment
     * @return A new Assignment created by crossover
     */
    private Assignment crossover(Assignment parent1, Assignment parent2)
    {
        Assignment offspring = new Assignment();

        // Uniform crossover - for each task, choose assignment from either parent1 or parent2
        for (Task task : tasks)
        {
            String taskId = task.getId();

            if (Math.random() < 0.5)
            {
                offspring.assign(taskId, parent1.getAssignedEmployee(taskId));
            }
            else
            {
                offspring.assign(taskId, parent2.getAssignedEmployee(taskId));
            }
        }

        return offspring;
    }

    /**
     * Applies mutation to an assignment.
     *
     * @param assignment The Assignment to mutate
     */
    private void mutate(Assignment assignment)
    {
        for (Task task : tasks)
        {
            if (Math.random() < mutationRate)
            {
                // Mutate this task by assigning it to a random employee
                String employeeId = employees.get(Initialise.getRandomEmployeeIndex(employees.size())).getId();
                assignment.assign(task.getId(), employeeId);
            }
        }

        // Additional mutation: swap tasks between employees
        if (Math.random() < mutationRate / 2 && tasks.size() >= 2)
        {
            int taskIndex1 = new Random().nextInt(tasks.size());
            int taskIndex2 = new Random().nextInt(tasks.size());

            if (taskIndex1 != taskIndex2)
            {
                String taskId1 = tasks.get(taskIndex1).getId();
                String taskId2 = tasks.get(taskIndex2).getId();

                String employeeId1 = assignment.getAssignedEmployee(taskId1);
                String employeeId2 = assignment.getAssignedEmployee(taskId2);

                assignment.assign(taskId1, employeeId2);
                assignment.assign(taskId2, employeeId1);
            }
        }
    }

    /**
     * Checks if termination condition is met.
     *
     * @param population The current population
     * @return true if termination condition is met, false otherwise
     */
    private boolean terminationCondition(List<Assignment> population)
    {
        // Check if we have a perfect solution (zero cost)
        for (Assignment assignment : population)
        {
            if (assignment.calculateCost(employees, tasks) == 0)
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Prints the progress of the current generation.
     *
     * @param bestAssignment The best Assignment in the current generation
     * @param generation     The current generation number
     */
    private void printProgress(Assignment bestAssignment, int generation)
    {
        StringBuilder sb = new StringBuilder();
        double cost = bestAssignment.calculateCost(employees, tasks);

        sb.append("Generation ").append(generation)
                .append(": Best Cost = ").append(String.format("%.2f", cost))
                .append(", Feasible: ").append(ConstraintValidator.isAssignmentFeasible(bestAssignment, tasks, employees))
                .append("\n");

        output += sb.toString();

        if (!fileOutput)
        {
            System.out.print(sb.toString());
        }
    }

    /**
     * Prints the final result of the algorithm.
     *
     * @param bestAssignment The best Assignment found
     * @param generation     The final generation number
     */
    private void printFinalResult(Assignment bestAssignment, int generation)
    {
        StringBuilder sb = new StringBuilder();
        double cost = bestAssignment.calculateCost(employees, tasks);

        sb.append("\n=================================\n");
        sb.append("Final Result after ").append(generation).append(" generations:\n");
        sb.append("=================================\n");
        sb.append("Total Cost: ").append(String.format("%.2f", cost)).append("\n");
        sb.append("Feasible Solution: ").append(ConstraintValidator.isAssignmentFeasible(bestAssignment, tasks, employees)).append("\n\n");

        // Print assignments
        sb.append("Task Assignments:\n");
        for (Task task : tasks)
        {
            String taskId = task.getId();
            String employeeId = bestAssignment.getAssignedEmployee(taskId);

            Employee employee = findEmployeeById(employeeId);

            sb.append("  Task ").append(taskId)
                    .append(" (Time: ").append(task.getEstimatedTime())
                    .append(", Difficulty: ").append(task.getDifficulty())
                    .append(", Skill: ").append(task.getRequiredSkill())
                    .append(") -> Employee ").append(employeeId)
                    .append(" (Skill Level: ").append(employee.getSkillLevel())
                    .append(", Has Required Skill: ").append(employee.hasSkill(task.getRequiredSkill()))
                    .append(")\n");
        }

        // Print workload distribution
        sb.append("\nWorkload Distribution:\n");
        Map<String, Integer> employeeWorkload = new HashMap<>();

        for (Employee employee : employees)
        {
            String employeeId = employee.getId();
            int totalTime = bestAssignment.calculateTotalWorkingTime(employeeId, tasks);
            employeeWorkload.put(employeeId, totalTime);

            sb.append("  Employee ").append(employeeId)
                    .append(": ").append(totalTime)
                    .append(" / ").append(employee.getAvailableHours())
                    .append(" hours (")
                    .append(String.format("%.1f", (double) totalTime / employee.getAvailableHours() * 100))
                    .append("%)");

            if (totalTime > employee.getAvailableHours())
            {
                sb.append(" - OVERLOADED");
            }

            sb.append("\n");
        }

        // Print penalty breakdown
        sb.append("\nPenalty Breakdown:\n");
        double overloadPenalty = 0;
        for (Employee employee : employees)
        {
            overloadPenalty += bestAssignment.calculateOverloadPenalty(employee, tasks);
        }

        double skillMismatchPenalty = bestAssignment.calculateSkillMismatchPenalty(employees, tasks);
        double deadlineViolationPenalty = bestAssignment.calculateDeadlineViolationPenalty(employees, tasks);

        sb.append("  Overload Penalty: ").append(String.format("%.2f", overloadPenalty)).append("\n");
        sb.append("  Skill Mismatch Penalty: ").append(String.format("%.2f", skillMismatchPenalty)).append("\n");
        sb.append("  Deadline Violation Penalty: ").append(String.format("%.2f", deadlineViolationPenalty)).append("\n");

        output += sb.toString();

        if (fileOutput)
        {
            System.out.println(sb.toString());
            writeToFile();
        }
        else
        {
            System.out.println(sb.toString());
        }
    }

    /**
     * Helper method to find an employee by ID.
     *
     * @param employeeId The ID of the employee to find
     * @return The Employee with the specified ID, or null if not found
     */
    private Employee findEmployeeById(String employeeId)
    {
        for (Employee employee : employees)
        {
            if (employee.getId().equals(employeeId))
            {
                return employee;
            }
        }
        return null;
    }

    /**
     * Writes output to a file.
     */
    private void writeToFile()
    {
        String fileName = "results/ga_results.txt";

        String extension = "";
        String name = "";

        int idxOfDot = fileName.lastIndexOf('.');
        extension = fileName.substring(idxOfDot + 1);
        name = fileName.substring(0, idxOfDot);

        Path path = Paths.get(fileName);
        int counter = 1;
        File f = null;

        // Create directories if they don't exist
        File dir = new File("results");
        if (!dir.exists())
        {
            dir.mkdirs();
        }

        while (Files.exists(path))
        {
            fileName = name + "(" + counter + ")." + extension;
            path = Paths.get(fileName);
            counter++;
        }
        f = new File(fileName);

        try (FileWriter fw = new FileWriter(f))
        {
            fw.write(output);
        }
        catch (IOException e)
        {
            System.out.println("Error writing to file: " + e.getMessage());
        }
    }

    /**
     * Gets the best cost history for reporting.
     *
     * @return List of best cost values per generation
     */
    @Override
    public List<Double> getBestCostHistory()
    {
        return bestCostHistory;
    }

    /**
     * Gets the average cost history for reporting.
     *
     * @return List of average cost values per generation
     */
    @Override
    public List<Double> getAvgCostHistory()
    {
        return avgCostHistory;
    }

    /**
     * Gets the feasible solutions history for reporting.
     *
     * @return List of feasible solution counts per generation
     */
    @Override
    public List<Integer> getFeasibleSolutionsHistory()
    {
        return feasibleSolutionsHistory;
    }
}
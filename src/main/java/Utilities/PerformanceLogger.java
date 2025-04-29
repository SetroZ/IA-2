package Utilities;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Model.Employee;
import Model.Task;

/**
 * A utility class for logging performance metrics during algorithm execution
 * and generating CSV files for visualization and analysis.
 */
public class PerformanceLogger {
    // Directory for storing CSV results
    private static final String RESULTS_DIR = "results/performance";

    // File names for different metrics
    private static final String SOLUTION_QUALITY_FILE = "solution_quality.csv";
    private static final String COMPUTATIONAL_EFFICIENCY_FILE = "computational_efficiency.csv";
    private static final String CONSTRAINT_SATISFACTION_FILE = "constraint_satisfaction.csv";

    // Metrics tracking
    private final List<IterationData> iterationDataList = new ArrayList<>();
    private final String algorithmName;
    private final List<Task> tasks;
    private final List<Employee> employees;

    // Time tracking
    private long startTime;
    private long totalExecutionTime;

    /**
     * Construct a PerformanceLogger for a specific algorithm run.
     *
     * @param algorithmName The name of the algorithm being logged
     * @param tasks The list of tasks in the problem instance
     * @param employees The list of employees in the problem instance
     */
    public PerformanceLogger(String algorithmName, List<Task> tasks, List<Employee> employees) {
        this.algorithmName = algorithmName;
        this.tasks = tasks;
        this.employees = employees;
        createResultsDirectory();
    }

    /**
     * Start timing the algorithm execution.
     */
    public void startTimer() {
        this.startTime = System.currentTimeMillis();
    }

    /**
     * Stop timing the algorithm execution.
     */
    public void stopTimer() {
        this.totalExecutionTime = System.currentTimeMillis() - startTime;
    }

    /**
     * Record metrics for the current iteration.
     *
     * @param iteration The current iteration/generation number
     * @param solution The current best solution
     * @param cost The cost of the current best solution
     * @param memoryUsed The memory used (in MB) during this iteration
     */
    public void logIteration(int iteration, int[] solution, double cost, double memoryUsed) {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - startTime;

        // Calculate constraint violations
        int skillMismatchCount = countSkillMismatches(solution);
        int overloadCount = countOverloads(solution);
        int difficultyViolationCount = countDifficultyViolations(solution);
        int deadlineViolationCount = countDeadlineViolations(solution);

        // Total constraint violations (ignoring deadline violations which are soft constraints)
        int hardConstraintViolations = skillMismatchCount + overloadCount + difficultyViolationCount;

        // Create iteration data entry
        IterationData data = new IterationData(
                iteration,
                elapsedTime,
                cost,
                memoryUsed,
                hardConstraintViolations,
                skillMismatchCount,
                overloadCount,
                difficultyViolationCount,
                deadlineViolationCount
        );

        iterationDataList.add(data);
    }

    /**
     * Count the number of skill mismatches in the solution.
     */
    private int countSkillMismatches(int[] solution) {
        int count = 0;
        for (Task task : tasks) {
            int employeeIdx = solution[task.getIdx()];
            Employee employee = employees.get(employeeIdx);
            if (!employee.hasSkill(task.getRequiredSkill())) {
                count++;
            }
        }
        return count;
    }

    /**
     * Count the number of employees with overloaded work hours.
     */
    private int countOverloads(int[] solution) {
        Map<String, Integer> employeeWorkload = new HashMap<>();

        // Calculate workload for each employee
        for (Task task : tasks) {
            int employeeIdx = solution[task.getIdx()];
            Employee employee = employees.get(employeeIdx);
            String employeeId = employee.getId();

            employeeWorkload.put(
                    employeeId,
                    employeeWorkload.getOrDefault(employeeId, 0) + task.getEstimatedTime()
            );
        }

        // Count overloaded employees
        int count = 0;
        for (Employee employee : employees) {
            int workload = employeeWorkload.getOrDefault(employee.getId(), 0);
            if (workload > employee.getAvailableHours()) {
                count++;
            }
        }

        return count;
    }

    /**
     * Count the number of difficulty level violations.
     */
    private int countDifficultyViolations(int[] solution) {
        int count = 0;
        for (Task task : tasks) {
            int employeeIdx = solution[task.getIdx()];
            Employee employee = employees.get(employeeIdx);
            if (employee.getSkillLevel() < task.getDifficulty()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Count the number of deadline violations.
     */
    private int countDeadlineViolations(int[] solution) {
        // For each employee, track their current workload time
        Map<String, Integer> employeeWorkloadTimes = new HashMap<>();
        int count = 0;

        for (Task task : tasks) {
            int employeeIdx = solution[task.getIdx()];
            Employee employee = employees.get(employeeIdx);
            String employeeId = employee.getId();

            // Initialize workload time if not already present
            employeeWorkloadTimes.putIfAbsent(employeeId, 0);

            // Get current workload time for the employee
            int currentWorkloadTime = employeeWorkloadTimes.get(employeeId);

            // Add the task's estimated time to the workload
            currentWorkloadTime += task.getEstimatedTime();

            // Check if the deadline is violated
            if (currentWorkloadTime > task.getDeadline()) {
                count++;
            }

            // Update the workload time
            employeeWorkloadTimes.put(employeeId, currentWorkloadTime);
        }

        return count;
    }

    /**
     * Save all logged metrics to CSV files for analysis and visualization.
     */
    public void saveMetricsToCSV() {
        try {
            saveSolutionQualityData();
            saveConstraintSatisfactionData();
            appendComputationalEfficiencyData();

            System.out.println("Performance metrics saved successfully to the 'results' directory.");
        } catch (IOException e) {
            System.err.println("Error saving performance metrics: " + e.getMessage());
        }
    }

    /**
     * Save solution quality data (cost vs. iteration/time).
     */
    private void saveSolutionQualityData() throws IOException {
        String filename = RESULTS_DIR + "/" + algorithmName + "_" + SOLUTION_QUALITY_FILE;
        boolean fileExists = Files.exists(Paths.get(filename));

        try (FileWriter writer = new FileWriter(filename, true)) {
            // Write header if file doesn't exist
            if (!fileExists) {
                writer.write("Algorithm,Iteration,ElapsedTimeMs,CostValue\n");
            }

            // Write data rows
            for (IterationData data : iterationDataList) {
                writer.write(String.format("%s,%d,%d,%.4f\n",
                        algorithmName,
                        data.iteration,
                        data.elapsedTimeMs,
                        data.cost
                ));
            }
        }
    }

    /**
     * Save constraint satisfaction data (violations vs. iteration/time).
     */
    private void saveConstraintSatisfactionData() throws IOException {
        String filename = RESULTS_DIR + "/" + algorithmName + "_" + CONSTRAINT_SATISFACTION_FILE;
        boolean fileExists = Files.exists(Paths.get(filename));

        try (FileWriter writer = new FileWriter(filename, true)) {
            // Write header if file doesn't exist
            if (!fileExists) {
                writer.write("Algorithm,Iteration,ElapsedTimeMs,HardConstraintViolations," +
                        "SkillMismatches,Overloads,DifficultyViolations,DeadlineViolations\n");
            }

            // Write data rows
            for (IterationData data : iterationDataList) {
                writer.write(String.format("%s,%d,%d,%d,%d,%d,%d,%d\n",
                        algorithmName,
                        data.iteration,
                        data.elapsedTimeMs,
                        data.hardConstraintViolations,
                        data.skillMismatchCount,
                        data.overloadCount,
                        data.difficultyViolationCount,
                        data.deadlineViolationCount
                ));
            }
        }
    }

    /**
     * Append computational efficiency data (runtime/memory vs. algorithm).
     * This file contains one row per algorithm run.
     */
    private void appendComputationalEfficiencyData() throws IOException {
        String filename = RESULTS_DIR + "/" + COMPUTATIONAL_EFFICIENCY_FILE;
        boolean fileExists = Files.exists(Paths.get(filename));

        try (FileWriter writer = new FileWriter(filename, true)) { // Append mode
            // Write header if file doesn't exist
            if (!fileExists) {
                writer.write("Algorithm,Iterations,TotalTimeMs,AvgIterationTimeMs,PeakMemoryMB,FinalCost,FinalConstraintViolations\n");
            }

            // Get last iteration data for final metrics
            IterationData lastData = iterationDataList.get(iterationDataList.size() - 1);

            // Calculate average time per iteration
            double avgTimePerIteration = (double) totalExecutionTime / iterationDataList.size();

            // Get peak memory usage
            double peakMemory = 0;
            for (IterationData data : iterationDataList) {
                peakMemory = Math.max(peakMemory, data.memoryUsageMB);
            }

            // Write a single row with summary data
            writer.write(String.format("%s,%d,%d,%.2f,%.2f,%.4f,%d\n",
                    algorithmName,
                    lastData.iteration,
                    totalExecutionTime,
                    avgTimePerIteration,
                    peakMemory,
                    lastData.cost,
                    lastData.hardConstraintViolations
            ));
        }
    }

    /**
     * Create the results directory if it doesn't exist.
     */
    private void createResultsDirectory() {
        File dir = new File(RESULTS_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Inner class to store data for each iteration.
     */
    private static class IterationData {
        final int iteration;
        final long elapsedTimeMs;
        final double cost;
        final double memoryUsageMB;
        final int hardConstraintViolations;
        final int skillMismatchCount;
        final int overloadCount;
        final int difficultyViolationCount;
        final int deadlineViolationCount;

        IterationData(
                int iteration,
                long elapsedTimeMs,
                double cost,
                double memoryUsageMB,
                int hardConstraintViolations,
                int skillMismatchCount,
                int overloadCount,
                int difficultyViolationCount,
                int deadlineViolationCount
        ) {
            this.iteration = iteration;
            this.elapsedTimeMs = elapsedTimeMs;
            this.cost = cost;
            this.memoryUsageMB = memoryUsageMB;
            this.hardConstraintViolations = hardConstraintViolations;
            this.skillMismatchCount = skillMismatchCount;
            this.overloadCount = overloadCount;
            this.difficultyViolationCount = difficultyViolationCount;
            this.deadlineViolationCount = deadlineViolationCount;
        }
    }

    /**
     * Utility method to get current memory usage in MB.
     *
     * @return Current memory usage in MB
     */
    public static double getCurrentMemoryUsageMB() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        return usedMemory / (1024.0 * 1024.0); // Convert bytes to MB
    }
}
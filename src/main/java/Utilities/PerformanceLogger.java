package Utilities;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Exceptions.LoadDataException;
import Model.Employee;
import Model.Task;

/**
 * Class for logging performance metrics during algorithm execution
 * and generating CSV files for visualisations
 */
public class PerformanceLogger {

    // File names for different metrics
    private static final String SOLUTION_QUALITY_FILE = "solution_quality.csv";
    private static final String COMPUTATIONAL_EFFICIENCY_FILE = "computational_efficiency.csv";
    private static final String COMPUTATIONAL_EFFICIENCY_MEMORY_FILE = "computational_efficiency_memory.csv";
    private static final String CONSTRAINT_SATISFACTION_FILE = "constraint_satisfaction.csv";
    private static final String PARAMETERS_FILE = "parameters.csv";

    // Metrics tracking
    private AlgParameters parameters;
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
     * @param runId The ID of the current run for the algorithm (for averaging)
     */
    public PerformanceLogger(String algorithmName, List<Task> tasks, List<Employee> employees, int runId) {
        this.algorithmName = algorithmName;
        this.tasks = tasks;
        this.employees = employees;

        PathUtility.setRunId(runId);
        PathUtility.createDirectories();
    }

    public void setParameters(AlgParameters parameters)
    {
        this.parameters = parameters;
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

        // Total constraint violations
        int totalConstraintViolations = skillMismatchCount + overloadCount + difficultyViolationCount + deadlineViolationCount;

        // Create iteration data entry
        IterationData data = new IterationData(
                iteration,
                elapsedTime,
                cost,
                memoryUsed,
                totalConstraintViolations,
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
            saveComputationalEfficiencyData();
            saveRunParameters(parameters);
            System.out.println("Performance metrics saved successfully to the 'results' directory.");
        } catch (IOException e) {
            System.err.println("Error saving performance metrics: " + e.getMessage());
        }
    }

    /**
     * Save solution quality data (cost vs. iteration/time).
     */
    private void saveSolutionQualityData() throws LoadDataException
    {
        String filename = PathUtility.getPerformanceDir() + "/" + algorithmName + "_" + SOLUTION_QUALITY_FILE;
        boolean fileExists = Files.exists(Paths.get(filename));
        try (FileWriter writer = new FileWriter(filename, true)) {
            // Write header
            if(!fileExists)
            {
                writer.write("Algorithm,Iteration,costValue\n");
            }

            for (IterationData data : iterationDataList) {
                writer.write(String.format("%s,%d,%.2f\n",
                        algorithmName,
                        data.iteration,
                        data.cost
                ));
            }
        } catch (IOException e) {
            throw new LoadDataException(e.getMessage());
        }
    }

    /**
     * Save constraint satisfaction data (violations vs. iteration/time).
     */
    private void saveConstraintSatisfactionData() throws LoadDataException
    {
        String filename = PathUtility.getPerformanceDir() + "/" + algorithmName + "_" + CONSTRAINT_SATISFACTION_FILE;

        try (FileWriter writer = new FileWriter(filename, false))
        {
            // Write header
            writer.write("Algorithm,Iteration,TotalConstraintViolations\n");


            // Write data rows
            for (IterationData data : iterationDataList)
            {
                writer.write(String.format("%s,%d,%d\n",
                        algorithmName,
                        data.iteration,
                        data.totalConstraintViolations
                ));
            }
        }
        catch (IOException e) {
            throw new LoadDataException(e.getMessage());
        }
    }

    /**
     * Append computational efficiency data (runtime/memory vs. algorithm).
     * This file contains one row per algorithm run.
     */
    private void saveComputationalEfficiencyData() throws IOException {
        String filename = PathUtility.getPerformanceDir() + "/"+ COMPUTATIONAL_EFFICIENCY_FILE;
        boolean fileExists = Files.exists(Paths.get(filename));

        try (FileWriter writer = new FileWriter(filename, true)) {
            // Write header
            if(!fileExists)
            {
                writer.write("Algorithm,Iterations,TotalTimeMs,AvgIterationTimeMs,UsedMemoryMb,AvgUsedMemoryPerIteration\n");
            }


            // Calculate average time per iteration
            double avgTimePerIteration = (double) totalExecutionTime / iterationDataList.size();

            // Sum total memory usage
            double totalMemoryUsage = 0;
            for(IterationData data : iterationDataList){
                totalMemoryUsage += data.memoryUsageMB;
            }
            double avgMemoryUsagePerIteration = totalMemoryUsage / iterationDataList.size();

            // Write a single row with summary data
            writer.write(String.format("%s,%d,%.2f,%.2f,%.2f\n",
                    algorithmName,
                    totalExecutionTime,
                    avgTimePerIteration,
                    totalMemoryUsage,
                    avgMemoryUsagePerIteration

            ));
        }
        catch (IOException e) {
            throw new LoadDataException(e.getMessage());
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
        final int totalConstraintViolations;
        final int skillMismatchCount;
        final int overloadCount;
        final int difficultyViolationCount;
        final int deadlineViolationCount;

        IterationData(
                int iteration,
                long elapsedTimeMs,
                double cost,
                double memoryUsageMB,
                int totalConstraintViolations,
                int skillMismatchCount,
                int overloadCount,
                int difficultyViolationCount,
                int deadlineViolationCount
        ) {
            this.iteration = iteration;
            this.elapsedTimeMs = elapsedTimeMs;
            this.cost = cost;
            this.memoryUsageMB = memoryUsageMB;
            this.totalConstraintViolations = totalConstraintViolations;
            this.skillMismatchCount = skillMismatchCount;
            this.overloadCount = overloadCount;
            this.difficultyViolationCount = difficultyViolationCount;
            this.deadlineViolationCount = deadlineViolationCount;
        }
    }

    private void saveRunParameters(AlgParameters parameters)
    {
        String filename = PathUtility.getPerformanceDir() + "/"+ algorithmName + "_" + PARAMETERS_FILE;
        boolean fileExists = Files.exists(Paths.get(filename));
        if(!fileExists)
        {
            try (FileWriter writer = new FileWriter(filename, false))
            {
                writer.write(parameters.toString());
            }
            catch (IOException e)
            {
                throw new LoadDataException("Failed to save parameter file to " + filename + "\n" + e.getMessage());
            }
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
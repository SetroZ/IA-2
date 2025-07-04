package Controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import Exceptions.LoadDataException;
import Exceptions.ObserverException;
import Utilities.PathUtility;
import View.PerformanceVisualiser;

/**
 * Controller for generating performance visualizations from CSV data.
 * This class reads performance data collected during algorithm runs and
 * generates comparative charts for analysis.
 */
public class VisualisationController {

    // Run ID
    //Directories


    // File names for different metrics
    private static final String SOLUTION_QUALITY_SUFFIX = "_solution_quality.csv";
    private static final String COMPUTATIONAL_EFFICIENCY_FILE = "computational_efficiency.csv";
    private static final String CONSTRAINT_SATISFACTION_SUFFIX = "_constraint_satisfaction.csv";

    // Algorithm names
    private static final String[] ALGORITHM_NAMES = {"GeneticAlg", "ParticleSwarmAlg", "AntColonyAlg"};

    // Chart output filenames
    private static final String SOLUTION_QUALITY_CHART = "solution_quality.png";
    private static final String COMPUTATIONAL_EFFICIENCY_TOTAL_RUNTIME_CHART = "computational_efficiency_total_runtime.png";
    private static final String COMPUTATIONAL_EFFICIENCY_AVG_RUNTIME_CHART = "computational_efficiency_avg_runtime.png";
    private static final String COMPUTATIONAL_EFFICIENCY_TOTAL_MEMORY_CHART = "computational_efficiency_total_memory.png";
    private static final String COMPUTATIONAL_EFFICIENCY_AVG_MEMORY_CHART = "computational_efficiency_avg_memory.png";
    private static final String CONSTRAINT_SATISFACTION_CHART = "constraint_satisfaction_comparison.png";



    private final PerformanceVisualiser visualiser;

    /**
     * Constructor for VisualizationController
     */
    public VisualisationController(int runID) {
        this.visualiser = new PerformanceVisualiser();
        PathUtility.setRunId(runID);
        PathUtility.createDirectories();
    }

    public void setRUN_ID(int runID)
    {
        PathUtility.setRunId(runID);
    }

    /**
     * Generate all performance comparison charts
     *
     * @return String with results of the operation
     */
    public String generateAllCharts(boolean perIteration) throws ObserverException {
        StringBuilder result = new StringBuilder();

        try {
            result.append(generateSolutionQualityChart()).append("\n");
            result.append(generateComputationalEfficiencyChart(perIteration)).append("\n");
            result.append(generateConstraintSatisfactionChart()).append("\n");
        } catch (LoadDataException e) {
            throw new ObserverException("Error generating charts: " + e.getMessage());
        }

        return result.toString();
    }

    /**
     * Generate a chart comparing solution quality across algorithms
     *
     * @return Result message
     * @throws LoadDataException If reading CSV or generating chart fails
     */
    public String generateSolutionQualityChart() throws ObserverException
    {
        List<String> algorithmNames = new ArrayList<>();
        List<List<double[]>> dataPoints = new ArrayList<>();

        for (String algorithm : ALGORITHM_NAMES)
        {
            try
            {
                List<double[]> averagedData = readSolutionQualityData(algorithm);

                if (!averagedData.isEmpty())
                {
                    algorithmNames.add(algorithm);
                    dataPoints.add(averagedData);
                }
            }
            catch (LoadDataException e)
            {
                throw new ObserverException(e.getMessage());
            }
        }

        // Generate the comparison chart
        String outputPath = PathUtility.getChartsDir()+ "/"+ SOLUTION_QUALITY_CHART;
        visualiser.createComparisonChart(
                "Algorithm Solution Quality Comparison",
                "Iterations",
                "Average Cost Value (Lower is Better)",
                algorithmNames,
                dataPoints,
                outputPath
        );

        return "Solution quality comparison chart saved to " + outputPath;
    }

    /**
     * Generate a chart comparing computational efficiency across algorithms
     *
     * @return Result message
     * @throws LoadDataException If reading CSV or generating chart fails
     */
    public String generateComputationalEfficiencyChart(boolean perIteration) throws LoadDataException {
        String filePath = PathUtility.getPerformanceDir() + "/"+ COMPUTATIONAL_EFFICIENCY_FILE;
        File file = new File(filePath);

        if (!file.exists()) {
            return "No computational efficiency data found. Run algorithms first.";
        }

        // Read the computational efficiency data
        Map<String, Double[]> efficiencyData = readComputationalEfficiencyData(filePath);

        if (efficiencyData.isEmpty()) {
            return "Computational efficiency data is empty.";
        }

        // Lists for bar chart data
        List<String> algorithmNames = new ArrayList<>(efficiencyData.keySet());
        List<Double> tRuntimeValues = new ArrayList<>();
        List<Double> avgRuntimeValues = new ArrayList<>();
        List<Double> tMemoryValues = new ArrayList<>();
        List<Double> avgMemoryValues = new ArrayList<>();

        String tRuntimeTitle = "Average TotalRuntime (ms)";

        String avgRuntimeTitle = "Average Runtime/Iteration (ms)";
        String tMemoryTitle = "Average Total Memory Usage(MB)";
        String avgMemoryTitle = "Average Memory/Iteration Usage (MB)";
        for (String algorithm : algorithmNames)
        {
            // TotalRuntime,AvgRuntime,TotalMemory,AvgMemory
            tRuntimeValues.add(efficiencyData.get(algorithm)[0]);
            avgRuntimeValues.add(efficiencyData.get(algorithm)[1]);
            tMemoryValues.add(efficiencyData.get(algorithm)[2]);
            avgMemoryValues.add(efficiencyData.get(algorithm)[3]);
        }

        // Generate the comparison charts
        String outputPath = PathUtility.getChartsDir() + "/" + COMPUTATIONAL_EFFICIENCY_TOTAL_RUNTIME_CHART;
        visualiser.createEfficiencyBarChart(
                "Computational Efficiency Comparison",
                "Algorithm",
                tRuntimeTitle,
                algorithmNames,
                tRuntimeValues,
                outputPath
        );

        outputPath = PathUtility.getChartsDir() + "/" + COMPUTATIONAL_EFFICIENCY_AVG_RUNTIME_CHART;
        visualiser.createEfficiencyBarChart(
                "Computational Efficiency Comparison",
                "Algorithm",
                avgRuntimeTitle,
                algorithmNames,
                avgRuntimeValues,
                outputPath
        );

        outputPath = PathUtility.getChartsDir() + "/"+ COMPUTATIONAL_EFFICIENCY_TOTAL_MEMORY_CHART;
        visualiser.createEfficiencyBarChart(
                "Computational Efficiency Comparison",
                "Algorithm",
                tMemoryTitle,
                algorithmNames,
                tMemoryValues,
                outputPath
        );

        outputPath = PathUtility.getChartsDir() + "/"+ COMPUTATIONAL_EFFICIENCY_AVG_MEMORY_CHART;
        visualiser.createEfficiencyBarChart(
                "Computational Efficiency Comparison",
                "Algorithm",
                avgMemoryTitle,
                algorithmNames,
                avgMemoryValues,
                outputPath
        );

        return "Computational efficiency comparison chart saved to " + outputPath;
    }

    /**
     * Generate a chart comparing constraint satisfaction across algorithms
     *
     * @return Result message
     * @throws LoadDataException If reading CSV or generating chart fails
     */
    public String generateConstraintSatisfactionChart() throws LoadDataException
    {
        // Lists to hold data for each algorithm
        List<String> algorithmNames = new ArrayList<>();
        List<List<double[]>> avgDataPoints = new ArrayList<>();

        // Read data for each algorithm
        for (String algorithm : ALGORITHM_NAMES)
        {
            try
            {
                List<double[]> avgData = readConstraintSatisfactionData(algorithm);

                if (!avgData.isEmpty())
                {
                    algorithmNames.add(algorithm);
                    avgDataPoints.add(avgData);
                }
            }
            catch (LoadDataException e)
            {
                throw new LoadDataException(e.getMessage());
            }
        }

        if (algorithmNames.isEmpty())
        {
            return "No constraint satisfaction data found. Run algorithms first.";
        }

        // Generate the comparison chart
        String outputPath = PathUtility.getChartsDir() + "/"+ CONSTRAINT_SATISFACTION_CHART;
        visualiser.createComparisonChart(
                "Constraint Satisfaction Comparison",
                "Iterations",
                "Average Constraint Violations (Lower is Better)",
                algorithmNames,
                avgDataPoints,
                outputPath
        );

        return "Constraint satisfaction comparison chart saved to " + outputPath;
    }

    /**
     * Read and average the cost of the solution quality data from CSV file
     *
     * @param algorithmName Name of the algorithm
     * @return List of data points [iteration, cost]
     * @throws LoadDataException If reading fails
     */
    private List<double[]> readSolutionQualityData(String algorithmName) throws LoadDataException {
        Map<Integer, List<Double>> iterationToCostMap = new HashMap<>();
        int runCount = 0;

        // Debug: Print directory contents
        System.out.println("Looking for solution quality files for " + algorithmName);
        File dir = new File(PathUtility.getPerformanceDir());
        System.out.println("Directory exists: " + dir.exists());
        if (dir.exists()) {
            System.out.println("Files in directory:");
            File[] allFiles = dir.listFiles();
            if (allFiles != null) {
                for (File f : allFiles) {
                    System.out.println(" - " + f.getName());
                }
            }
        }
        System.out.println(PathUtility.getRunId());

        // Find all files for this algorithm using the correct pattern that includes the runID
        File[] files = dir.listFiles((d, name) ->
                name.startsWith(algorithmName) && name.contains(SOLUTION_QUALITY_SUFFIX));

        System.out.println("Found " + (files != null ? files.length : 0) + " solution quality files for " + algorithmName);

        if (files != null && files.length > 0) {
            for (File file : files) {
                System.out.println("Processing file: " + file.getName());
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    // Skip header
                    reader.readLine();

                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] parts = line.split(",");
                        if (parts.length >= 3) {
                            int iteration = Integer.parseInt(parts[1]);
                            double costValue = Double.parseDouble(parts[2]);
                            //map.put(iteration, costValue);

                            System.out.println("  Found data point: iteration=" + iteration + ", cost=" + costValue);

                            // Store cost for this iteration
                            iterationToCostMap.computeIfAbsent(iteration, k -> new ArrayList<>())
                                    .add(costValue);
                        }
                    }
                } catch (IOException e) {
                    throw new LoadDataException("Could not read solution quality data for " + file.getName()
                            + ": " + e.getMessage());
                }
                runCount++;
            }
        }

        // If no data was found, return empty list
        if (runCount == 0) {
            System.out.println("No solution quality data found for " + algorithmName);
            return new ArrayList<>();
        }

        // Calculate averages and print them for debugging
        List<double[]> averagedData = new ArrayList<>();
        for (Map.Entry<Integer, List<Double>> entry : iterationToCostMap.entrySet()) {
            int iteration = entry.getKey();
            List<Double> costs = entry.getValue();

            // Calculate average cost for this iteration
            double avgCost = costs.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

            System.out.println("Average for iteration " + iteration + ": " + avgCost +
                    " (from " + costs.size() + " runs: " + costs + ")");

            // Add data point [iteration, avgCost]
            averagedData.add(new double[]{iteration, avgCost});
        }

        // Sort by iteration
        averagedData.sort(Comparator.comparingDouble(point -> point[0]));

        return averagedData;
    }

    /**
     * Read computational efficiency data from CSV file
     *
     * @param filePath Path to the CSV file
     * @return Map of algorithm name to metrics array [runtime, memory, iterations]
     * @throws LoadDataException If reading fails
     */
    private Map<String, Double[]> readComputationalEfficiencyData(String filePath) throws LoadDataException {
        Map<String, List<Double[]>> dataMap = new HashMap<>();
        Map<String, Double[]> averagedData = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            // Skip header
            reader.readLine();

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    String algorithm = parts[0];
                    Double[] metrics = new Double[4];

                    metrics[0] = (Double.parseDouble(parts[1])); // Total Time
                    metrics[1] = (Double.parseDouble(parts[2])); // time/iteration
                    metrics[2] = (Double.parseDouble(parts[3])); // Total memoryUsage
                    metrics[3] = (Double.parseDouble(parts[4])); // memoryusage per iteration
                    dataMap.computeIfAbsent(algorithm, k -> new ArrayList<>()).add(metrics);
                }
            }


            for (Map.Entry<String, List<Double[]>> entry : dataMap.entrySet()) {
                String algorithm = entry.getKey();
                List<Double[]> metricList = entry.getValue();
                // Calculate averages of metrics for every trial in run per algorithm
                double avgTotalRunTime = 0;
                double avgRunTimePerIteration = 0;
                double avgTotalMemoryUsage = 0;
                double avgMemoryUsagePerIteration = 0;
                for(Double[] metrics : metricList) {
                    avgTotalRunTime += metrics[0];
                    avgRunTimePerIteration += metrics[1];
                    avgTotalMemoryUsage += metrics[2];
                    avgMemoryUsagePerIteration += metrics[3];
                }
                avgRunTimePerIteration = avgRunTimePerIteration/metricList.size();
                avgTotalRunTime = avgTotalRunTime / metricList.size();
                avgTotalMemoryUsage = avgTotalMemoryUsage / metricList.size();
                avgMemoryUsagePerIteration = avgMemoryUsagePerIteration / metricList.size();
//
//                System.out.println("Average runtime/itereration for algorithm " + algorithm + ": " + avgRunTimePerIteration +
//                        " (from " + metricList.size() + " runs: " + metricList + ")");
//                System.out.println("Average total runtime for algorithm " + algorithm + ": " + avgTotalRunTime +
//                        " (from " + metricList.size() + " runs: " + metricList + ")");


                averagedData.put(algorithm, new Double[]{avgTotalRunTime, avgRunTimePerIteration, avgTotalMemoryUsage, avgMemoryUsagePerIteration});
            }
        }
        catch (IOException e) {
            throw new LoadDataException("Could not read computational efficiency data for " + filePath);
        }

        return averagedData;
    }

    /**
     * Read constraint satisfaction data from CSV file
     *
     * @param algorithmName name of the CSV file
     * @return List of data points [iteration, violation count]
     * @throws LoadDataException If reading fails
     */
    private List<double[]> readConstraintSatisfactionData(String algorithmName) throws LoadDataException {
        Map<Integer, List<Double>> iterationToViolationsMap = new HashMap<>();
        int runCount = 0;

        // Find all files for this algorithm with constraint satisfaction data using the correct pattern
        File dir = new File(PathUtility.getPerformanceDir());
        File[] files = dir.listFiles((d, name) ->
                name.startsWith(algorithmName) && name.contains(CONSTRAINT_SATISFACTION_SUFFIX));

        if (files != null) {
            for (File file : files) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    // Skip header
                    reader.readLine();

                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] parts = line.split(",");
                        if (parts.length >= 3) {
                            int iteration = Integer.parseInt(parts[1]);
                            double violations = Double.parseDouble(parts[2]); // TotalConstraintViolations column

                            // Store constraint violations for this iteration
                            iterationToViolationsMap.computeIfAbsent(iteration, k -> new ArrayList<>())
                                    .add(violations);
                        }
                    }
                } catch (IOException e) {
                    throw new LoadDataException("Could not read constraint satisfaction data for " + file.getName());
                }
                runCount++;
            }
        }

        // If no data was found, return empty list
        if (runCount == 0) {
            return new ArrayList<>();
        }

        // Calculate averages for each iteration
        List<double[]> averagedData = new ArrayList<>();
        for (Map.Entry<Integer, List<Double>> entry : iterationToViolationsMap.entrySet()) {
            int iteration = entry.getKey();
            List<Double> violations = entry.getValue();

            // Calculate average violations for this iteration
            double avgViolations = violations.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

            System.out.println("Constraint violations average for iteration " + iteration + ": " + avgViolations +
                    " (from " + violations.size() + " runs)");

            // Add data point [iteration, avgViolations]
            averagedData.add(new double[]{iteration, avgViolations});
        }

        // Sort by iteration
        averagedData.sort(Comparator.comparingDouble(point -> point[0]));

        return averagedData;
    }

}
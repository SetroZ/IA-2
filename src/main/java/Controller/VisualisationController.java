package Controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import Exceptions.LoadDataException;
import Exceptions.ObserverException;
import View.PerformanceVisualiser;

/**
 * Controller for generating performance visualizations from CSV data.
 * This class reads performance data collected during algorithm runs and
 * generates comparative charts for analysis.
 */
public class VisualisationController {
    private static final String RESULTS_DIR = "results/performance";
    private static final String CHARTS_DIR = "results/charts";

    // File names for different metrics
    private static final String SOLUTION_QUALITY_SUFFIX = "_solution_quality.csv";
    private static final String COMPUTATIONAL_EFFICIENCY_FILE = "computational_efficiency.csv";
    private static final String CONSTRAINT_SATISFACTION_SUFFIX = "_constraint_satisfaction.csv";

    // Algorithm names
    private static final String[] ALGORITHM_NAMES = {"GeneticAlg", "ParticleSwarmAlg", "AntColonyAlg"};

    // Chart output filenames
    private static final String SOLUTION_QUALITY_CHART = "solution_quality_comparison.png";
    private static final String COMPUTATIONAL_EFFICIENCY_CHART = "computational_efficiency_comparison.png";
    private static final String CONSTRAINT_SATISFACTION_CHART = "constraint_satisfaction_comparison.png";

    // Run id
    private int RUN_ID;

    private final PerformanceVisualiser visualiser;

    /**
     * Constructor for VisualizationController
     */
    public VisualisationController(int runID) {
        this.visualiser = new PerformanceVisualiser();
        this.RUN_ID = runID;
        createDirectories();
    }

    public void setRUN_ID(int runID)
    {
        this.RUN_ID = runID;
    }

    /**
     * Create necessary directories for output files
     */
    private void createDirectories() {
        new File(CHARTS_DIR).mkdirs();
    }

    /**
     * Generate all performance comparison charts
     *
     * @return String with results of the operation
     */
    public String generateAllCharts() throws ObserverException {
        StringBuilder result = new StringBuilder();

        try {
            result.append(generateSolutionQualityChart()).append("\n");
            result.append(generateComputationalEfficiencyChart()).append("\n");
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
        String outputPath = CHARTS_DIR + "/run"+RUN_ID +"_"+ SOLUTION_QUALITY_CHART;
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
    public String generateComputationalEfficiencyChart() throws LoadDataException {
        String filePath = RESULTS_DIR + "/" +"run"+RUN_ID+"_"+ COMPUTATIONAL_EFFICIENCY_FILE;
        File file = new File(filePath);

        if (!file.exists()) {
            return "No computational efficiency data found. Run algorithms first.";
        }

        // Read the computational efficiency data
        Map<String, double[]> efficiencyData = readComputationalEfficiencyData(filePath);

        if (efficiencyData.isEmpty()) {
            return "Computational efficiency data is empty.";
        }

        // Lists for bar chart data
        List<String> algorithmNames = new ArrayList<>(efficiencyData.keySet());
        List<Double> runtimeValues = new ArrayList<>();

        // Extract runtime values for each algorithm
        for (String algorithm : algorithmNames) {
            double[] metrics = efficiencyData.get(algorithm);
            runtimeValues.add(metrics[0]); // Runtime is at index 0
        }

        // Generate the comparison chart
        String outputPath = CHARTS_DIR + "/" + COMPUTATIONAL_EFFICIENCY_CHART;
        visualiser.createEfficiencyBarChart(
                "Computational Efficiency Comparison",
                "Algorithm",
                "Runtime (ms)",
                algorithmNames,
                runtimeValues,
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
        List<List<double[]>> stdDevDataPoints = new ArrayList<>();

        // Read data for each algorithm
        for (String algorithm : ALGORITHM_NAMES)
        {
            try
            {
                List<double[]> avgData = readConstraintSatisfactionData(algorithm);
                List<double[]> stdDevData = calculateConstraintSatisfactionStdDev(algorithm);

                if (!avgData.isEmpty())
                {
                    algorithmNames.add(algorithm);
                    avgDataPoints.add(avgData);
                    stdDevDataPoints.add(stdDevData);
                }
            }
            catch (IOException e)
            {
                throw new LoadDataException(e.getMessage());
            }
        }

        if (algorithmNames.isEmpty())
        {
            return "No constraint satisfaction data found. Run algorithms first.";
        }

        // Generate the comparison chart
        String outputPath = CHARTS_DIR + "run"+RUN_ID +"_"+ CONSTRAINT_SATISFACTION_CHART;
        visualiser.createComparisonChartWithVariability(
                "Constraint Satisfaction Comparison",
                "Iterations",
                "Average Constraint Violations (Lower is Better)",
                algorithmNames,
                avgDataPoints,
                stdDevDataPoints,
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
        File dir = new File(RESULTS_DIR);
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
        System.out.println(RUN_ID);

        // Find all files for this algorithm using the correct pattern that includes the runID
        File[] files = dir.listFiles((d, name) ->
                name.startsWith(algorithmName) && name.contains("_run"+RUN_ID) && name.contains(SOLUTION_QUALITY_SUFFIX));

        System.out.println("Found " + (files != null ? files.length : 0) + " solution quality files for " + algorithmName);

        if (files != null && files.length > 0) {
            for (File file : files) {
                System.out.println("Processing file: " + file.getName());
                Map<Integer, Double> map = new HashMap<Integer, Double>();
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    // Skip header
                    reader.readLine();

                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] parts = line.split(",");
                        if (parts.length >= 4) {
                            int iteration = Integer.parseInt(parts[1]);
                            double costValue = Double.parseDouble(parts[3]);
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
    private Map<String, double[]> readComputationalEfficiencyData(String filePath) throws LoadDataException {
        Map<String, double[]> dataMap = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            // Skip header
            reader.readLine();

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 7) {
                    String algorithm = parts[0];
                    double[] metrics = new double[3];
                    metrics[0] = Double.parseDouble(parts[2]); // Total runtime
                    metrics[1] = Double.parseDouble(parts[4]); // Peak memory
                    metrics[2] = Double.parseDouble(parts[1]); // Iterations
                    dataMap.put(algorithm, metrics);
                }
            }
        }
        catch (IOException e) {
            throw new LoadDataException("Could not read computational efficiency data for " + filePath);
        }

        return dataMap;
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
        File dir = new File(RESULTS_DIR);
        File[] files = dir.listFiles((d, name) ->
                name.startsWith(algorithmName) && name.contains("_run"+RUN_ID) && name.contains(CONSTRAINT_SATISFACTION_SUFFIX));

        if (files != null) {
            for (File file : files) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    // Skip header
                    reader.readLine();

                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] parts = line.split(",");
                        if (parts.length >= 4) {
                            int iteration = Integer.parseInt(parts[1]);
                            double violations = Double.parseDouble(parts[3]); // HardConstraintViolations column

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

    private List<double[]> getAverageData(Map<Integer, List<Double>> iterationToX){
        // Calculate averages for each iteration
        List<double[]> averagedData = new ArrayList<>();
        for (Map.Entry<Integer, List<Double>> entry : iterationToX.entrySet()) {
            int iteration = entry.getKey();
            List<Double> violations = entry.getValue();

            // Calculate average X for this iteration
            double X = violations.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

            // Add data point [iteration, X]
            averagedData.add(new double[]{iteration, X});
        }

        // Sort by iteration
        averagedData.sort(Comparator.comparingDouble(point -> point[0]));

        return averagedData;
    }

    private List<double[]> calculateConstraintSatisfactionStdDev(String algorithmName) throws IOException {
        Map<Integer, List<Double>> iterationToViolationsMap = new HashMap<>();

        // Find all files for this algorithm
        File dir = new File(RESULTS_DIR);
        File[] files = dir.listFiles((d, name) ->
                name.startsWith(algorithmName) && name.contains("_constraint_satisfaction.csv"));

        if (files != null) {
            for (File file : files) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    // Skip header
                    reader.readLine();

                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] parts = line.split(",");
                        if (parts.length >= 4) {
                            int iteration = Integer.parseInt(parts[1]);
                            double violations = Double.parseDouble(parts[3]); // HardConstraintViolations

                            // Store constraint violations for this iteration
                            iterationToViolationsMap.computeIfAbsent(iteration, k -> new ArrayList<>())
                                    .add(violations);
                        }
                    }
                }
            }
        }

        // Calculate standard deviation for each iteration
        List<double[]> stdDevData = new ArrayList<>();
        for (Map.Entry<Integer, List<Double>> entry : iterationToViolationsMap.entrySet()) {
            int iteration = entry.getKey();
            List<Double> violations = entry.getValue();

            // Need at least 2 data points to calculate standard deviation
            if (violations.size() < 2) {
                stdDevData.add(new double[]{iteration, 0.0}); // No std dev with just one sample
                continue;
            }

            // Calculate mean
            double mean = violations.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

            // Calculate sum of squared differences
            double sumSquaredDiff = violations.stream()
                    .mapToDouble(v -> Math.pow(v - mean, 2))
                    .sum();

            // Calculate standard deviation
            double stdDev = Math.sqrt(sumSquaredDiff / (violations.size() - 1));

            // Add data point [iteration, stdDev]
            stdDevData.add(new double[]{iteration, stdDev});
        }

        // Sort by iteration
        stdDevData.sort(Comparator.comparingDouble(point -> point[0]));

        return stdDevData;
    }


}
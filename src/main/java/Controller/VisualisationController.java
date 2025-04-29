package Controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private final PerformanceVisualiser visualiser;

    /**
     * Constructor for VisualizationController
     */
    public VisualisationController() {
        this.visualiser = new PerformanceVisualiser();
        createDirectories();
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
        } catch (IOException e) {
            throw new ObserverException("Error generating charts: " + e.getMessage());
        }

        return result.toString();
    }

    /**
     * Generate a chart comparing solution quality across algorithms
     *
     * @return Result message
     * @throws IOException If reading CSV or generating chart fails
     */
    public String generateSolutionQualityChart() throws IOException {
        // Lists to hold data for each algorithm
        List<String> algorithmNames = new ArrayList<>();
        List<List<double[]>> dataPoints = new ArrayList<>();

        // Read data for each algorithm
        for (String algorithm : ALGORITHM_NAMES) {
            String filePath = RESULTS_DIR + "/" + algorithm + SOLUTION_QUALITY_SUFFIX;
            File file = new File(filePath);

            if (file.exists()) {
                List<double[]> algorithmData = readSolutionQualityData(filePath);

                if (!algorithmData.isEmpty()) {
                    algorithmNames.add(algorithm);
                    dataPoints.add(algorithmData);
                }
            }
        }

        if (algorithmNames.isEmpty()) {
            return "No solution quality data found. Run algorithms first.";
        }

        // Generate the comparison chart
        String outputPath = CHARTS_DIR + "/" + SOLUTION_QUALITY_CHART;
        visualiser.createComparisonChart(
                "Algorithm Solution Quality Comparison",
                "Iterations",
                "Cost Value (Lower is Better)",
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
     * @throws IOException If reading CSV or generating chart fails
     */
    public String generateComputationalEfficiencyChart() throws IOException {
        String filePath = RESULTS_DIR + "/" + COMPUTATIONAL_EFFICIENCY_FILE;
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
     * @throws IOException If reading CSV or generating chart fails
     */
    public String generateConstraintSatisfactionChart() throws IOException {
        // Lists to hold data for each algorithm
        List<String> algorithmNames = new ArrayList<>();
        List<List<double[]>> dataPoints = new ArrayList<>();

        // Read data for each algorithm
        for (String algorithm : ALGORITHM_NAMES) {
            String filePath = RESULTS_DIR + "/" + algorithm + CONSTRAINT_SATISFACTION_SUFFIX;
            File file = new File(filePath);

            if (file.exists()) {
                List<double[]> algorithmData = readConstraintSatisfactionData(filePath);

                if (!algorithmData.isEmpty()) {
                    algorithmNames.add(algorithm);
                    dataPoints.add(algorithmData);
                }
            }
        }

        if (algorithmNames.isEmpty()) {
            return "No constraint satisfaction data found. Run algorithms first.";
        }

        // Generate the comparison chart
        String outputPath = CHARTS_DIR + "/" + CONSTRAINT_SATISFACTION_CHART;
        visualiser.createComparisonChart(
                "Constraint Satisfaction Comparison",
                "Iterations",
                "Constraint Violations (Lower is Better)",
                algorithmNames,
                dataPoints,
                outputPath
        );

        return "Constraint satisfaction comparison chart saved to " + outputPath;
    }

    /**
     * Read solution quality data from CSV file
     *
     * @param filePath Path to the CSV file
     * @return List of data points [iteration, cost]
     * @throws IOException If reading fails
     */
    private List<double[]> readSolutionQualityData(String filePath) throws IOException {
        List<double[]> dataPoints = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            // Skip header
            reader.readLine();

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    try {
                        // [iteration, cost]
                        double[] point = new double[2];
                        point[0] = Double.parseDouble(parts[1]); // Iteration

                        double costValue = Double.parseDouble(parts[3]); // Cost value

                        // Filter out extreme values that might cause visualization issues
                        if (Double.isFinite(costValue) && costValue < 1.0E10) {
                            point[1] = costValue;
                            dataPoints.add(point);
                        } else {
                            System.out.println("Skipping extreme cost value: " + costValue);
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Warning: Could not parse value in line: " + line);
                    }
                }
            }
        }

        return dataPoints;
    }

    /**
     * Read computational efficiency data from CSV file
     *
     * @param filePath Path to the CSV file
     * @return Map of algorithm name to metrics array [runtime, memory, iterations]
     * @throws IOException If reading fails
     */
    private Map<String, double[]> readComputationalEfficiencyData(String filePath) throws IOException {
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

        return dataMap;
    }

    /**
     * Read constraint satisfaction data from CSV file
     *
     * @param filePath Path to the CSV file
     * @return List of data points [iteration, violation count]
     * @throws IOException If reading fails
     */
    private List<double[]> readConstraintSatisfactionData(String filePath) throws IOException {
        List<double[]> dataPoints = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            // Skip header
            reader.readLine();

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    // [iteration, violations]
                    double[] point = new double[2];
                    point[0] = Double.parseDouble(parts[1]); // Iteration
                    point[1] = Double.parseDouble(parts[3]); // Hard constraint violations
                    dataPoints.add(point);
                }
            }
        }

        return dataPoints;
    }
}
package Utilities;

import java.io.File;

/**
 * Utility class for managing paths and directories across the application.
 * Provides a central place for directory path definitions to ensure consistency.
 */
public class PathUtility {
    private static int runId = 1;

    // Base directory
    private static final String RESULTS_DIR = "results";

    /**
     * Set the current run ID
     * @param id The run ID to set
     */
    public static void setRunId(int id) {
        runId = id;
    }

    /**
     * Get the current run ID
     * @return The current run ID
     */
    public static int getRunId() {
        return runId;
    }


    /**
     * Get the run-specific directory path
     * @return The run-specific directory path
     */
    public static String getRunDir() {
        return RESULTS_DIR + "/run(" + runId + ")";
    }

    /**
     * Get the performance directory path for the current run
     * @return The performance directory path
     */
    public static String getPerformanceDir() {
        return getRunDir() + "/performance";
    }

    /**
     * Get the charts directory path for the current run
     * @return The charts directory path
     */
    public static String getChartsDir() {
        return getRunDir() + "/charts";
    }

    /**
     * Create all necessary directories for the current run
     */
    public static void createDirectories() {
        // Create all directories
        new File(getRunDir()).mkdirs();
        new File(getPerformanceDir()).mkdirs();
        new File(getChartsDir()).mkdirs();
    }

    /**
     * Determine the next available run ID by scanning the results directory
     * @return The next available run ID
     */
    public static int determineNextRunId() {
        File dir = new File(RESULTS_DIR);
        if (!dir.exists()) {
            return 1;
        }

        File[] files = dir.listFiles((d, name) -> name.startsWith("run(") && name.endsWith(")"));
        if (files == null || files.length == 0) {
            return 1;
        }

        int maxRunId = 0;
        for (File file : files) {
            String name = file.getName();
            try {
                // Extract number between "run(" and ")"
                int extractedId = Integer.parseInt(name.substring(4, name.length() - 1));
                maxRunId = Math.max(maxRunId, extractedId);
            } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
                // Skip files with invalid format
            }
        }

        return maxRunId + 1;
    }
}
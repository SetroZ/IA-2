package Utilities;

import Exceptions.LoadDataException;
import Model.Task;
import Model.Employee;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Arrays;

/**
 * Utility class for generating or loading task and employee data.
 */
public class DataGenerator {

    private static final String RESOURCES_DIR = "out/resources/";
    // Test Directory
    private static final String TEST_DIR = "testData";

    /**
     * Loads task data from a CSV file.
     *
     * @param filename Path to the CSV file (just the filename)
     * @return List of Task objects
     * @throws LoadDataException If an I/O error occurs
     */
    public static List<Task> loadTasks(String filename) throws LoadDataException {
        List<Task> tasks = new ArrayList<>();

        String dir;
        // Strip any leading slash for consistency
        String sanitizedFilename = filename.startsWith("/") ? filename.substring(1) : filename;

        // Just use the filename part if a full path was given
        String actualFilename = new File(sanitizedFilename).getName();

        File file = new File(filename);

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            // Skip header
            String line = reader.readLine();

            int idx = 0;

            // Read data lines
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 5) {
                    String id = data[0].trim();
                    int estimatedTime = Integer.parseInt(data[1].trim());
                    int difficulty = Integer.parseInt(data[2].trim());
                    int deadline = Integer.parseInt(data[3].trim());
                    String requiredSkill = data[4].trim();

                    Task task = new Task(id, estimatedTime, difficulty, deadline, requiredSkill, idx);
                    tasks.add(task);
                    idx++;
                } else {
                    throw new LoadDataException("Invalid task line: " + line);
                }
            }
        } catch (FileNotFoundException e) {
            throw new LoadDataException("Task file not found: " + file.getAbsolutePath());
        } catch (IOException e) {
            throw new LoadDataException("Error reading task data: " + e.getMessage());
        }

        return tasks;
    }

    /**
     * Loads employee data from a CSV file.
     *
     * @param filename Path to the CSV file (just the filename)
     * @return List of Employee objects
     * @throws LoadDataException If an I/O error occurs
     */
    public static List<Employee> loadEmployees(String filename) throws LoadDataException {
        List<Employee> employees = new ArrayList<>();

        // Strip any leading slash for consistency
        String sanitizedFilename = filename.startsWith("/") ? filename.substring(1) : filename;

        // Just use the filename part if a full path was given
        String actualFilename = new File(sanitizedFilename).getName();

        File file = new File(filename);

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            // Skip header line
            String line = reader.readLine();

            int idx = 0;
            // Read data lines
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 4) {
                    String id = data[0].trim();
                    int availableHours = Integer.parseInt(data[1].trim());
                    int skillLevel = Integer.parseInt(data[2].trim());

                    // Parse skills
                    String skillsStr = data[3].trim();
                    String[] skillsArray = skillsStr.split(" ");
                    Set<String> skills = new HashSet<>(Arrays.asList(skillsArray));

                    Employee employee = new Employee(id, availableHours, skillLevel, skills, idx);
                    employees.add(employee);
                    idx++;
                } else {
                    throw new LoadDataException("Invalid employee line: " + line);
                }
            }
        } catch (FileNotFoundException e) {
            throw new LoadDataException("Employee file not found: " + file.getAbsolutePath());
        } catch (IOException e) {
            throw new LoadDataException("Error reading employee data: " + e.getMessage());
        }

        return employees;
    }

    /**
     * Scan and return a list of all csv files in the resources folder.
     *
     * @return List of file paths found
     * @throws LoadDataException If no files found
     */
    public static List<String> getResourceFiles(boolean isTest) throws LoadDataException {
        List<String> fileNames = new ArrayList<>();

        String dir;
        if (isTest) {
            dir = TEST_DIR;
        } else {
            dir = RESOURCES_DIR;
        }
        File resourceDir = new File(dir);
        if (!resourceDir.exists()) {
            throw new LoadDataException("Resources directory not found: " + resourceDir.getAbsolutePath());
        }

        File[] files = resourceDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".csv")) {
                    System.out.println("Adding: " + file.getPath());
                    fileNames.add(file.getPath());
                }
                if (file.isDirectory()) {
                    File[] subDir = file.listFiles();
                    if (subDir != null) {
                        for (File subFile : subDir) {
                            fileNames.add(subFile.getPath());
                        }
                    }
                }
            }
        }

        if (fileNames.isEmpty()) {
            throw new LoadDataException("No CSV files found in resources directory.");
        }

        return fileNames;
    }

    public static AlgParameters loadTestFile(String filename) throws LoadDataException {
        AlgParameters parameters;
        // Strip any leading slash for consistency
        String sanitizedFilename = filename.startsWith("/") ? filename.substring(1) : filename;

        // Just use the filename part if a full path was given
        String actualFilename = new File(sanitizedFilename).getName();

        File file = new File(filename);

        // "maxIterations,reportingFrequency,fileOutput,populationSize,mutationRate,crossoverRate,
        // elitismCount,c1,c2,w,initpheromone,Pherdecayrate\n")

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            // Skip header line
            String line = reader.readLine();

            int idx = 0;
            // Read data lines
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 12) {
                    int maxIterations = Integer.parseInt(data[0].trim());
                    int reportingFrequency = Integer.parseInt(data[1].trim());
                    boolean fileOutput = Boolean.parseBoolean(data[2].trim());
                    int populationSize = Integer.parseInt(data[3].trim());
                    double mutationRate = Double.parseDouble(data[4].trim());
                    double crossoverRate = Double.parseDouble(data[5].trim());
                    int elitismCount = Integer.parseInt(data[6].trim());
                    double c1 = Double.parseDouble(data[7].trim());
                    double c2 = Double.parseDouble(data[8].trim());
                    double w = Double.parseDouble(data[9].trim());
                    double initPheromone = Double.parseDouble(data[10].trim());
                    double pherDecayRate = Double.parseDouble(data[11].trim());

                    parameters = new AlgParameters(maxIterations,
                            reportingFrequency, fileOutput, populationSize, mutationRate,
                            crossoverRate, elitismCount, c1, c2, w, initPheromone,
                            pherDecayRate);
                    return parameters;
                } else {
                    throw new LoadDataException("Invalid TestData line: " + line);
                }
            }
        } catch (FileNotFoundException e) {
            throw new LoadDataException("Employee file not found: " + file.getAbsolutePath());
        } catch (IOException e) {
            throw new LoadDataException("Error reading employee data: " + e.getMessage());
        }
        throw new LoadDataException("No CSV files found in resources directory.");
    }
}
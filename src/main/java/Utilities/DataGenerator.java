package Utilities;

import Exceptions.LoadDataException;
import Model.Task;
import Model.Employee;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Arrays;

/**
 * Utility class for generating or loading task and employee data.
 */
public class DataGenerator {

    /**
     * Loads task data from a specific CSV file.
     *
     * @param filePath Path to the CSV file (relative to resources)
     * @return List of Task objects
     * @throws LoadDataException If an I/O error occurs
     */
    public static List<Task> loadTasks(String filePath) throws LoadDataException {
        List<Task> tasks = new ArrayList<>();



        try (InputStream is = DataGenerator.class.getResourceAsStream(filePath))
        {
            assert is != null;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

                //Skip header
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
                    }
                    else
                    {
                        throw new LoadDataException("Invalid task line: " + line);
                    }
                }
            }
        }
        catch (IOException e) {
            throw new LoadDataException("ERROR while loading data to Tasks: " + e.getMessage());
        }

        return tasks;
    }


    /**
     * Loads employee data from a specific CSV file.
     *
     * @param filePath Path to the CSV file (relative to resources)
     * @return List of Employee objects
     * @throws LoadDataException If an I/O error occurs
     */
    public static List<Employee> loadEmployees(String filePath) throws LoadDataException {
        List<Employee> employees = new ArrayList<>();

        try (InputStream is = DataGenerator.class.getResourceAsStream(filePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
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

                    // Parse skills (comma-separated list, potentially in quotes with spaces)
                    String skillsStr = data[3].trim();
                    System.out.println(skillsStr);
                    String[] skillsArray = skillsStr.split(" ");
                    Set<String> skills = new HashSet<>(Arrays.asList(skillsArray));

                    Employee employee = new Employee(id, availableHours, skillLevel, skills, idx);
                    employees.add(employee);
                    idx++;
                }
                else
                {
                    throw new LoadDataException("Invalid employee line: " + line + "\nData of wrong type or format");
                }
            }
        }
        catch (IOException e) {
            throw new LoadDataException("ERROR while loading data to Employees: " + e.getMessage());
        }

        return employees;
    }

    /**
     * Scan and return a list of all csv files in resource folder
     *
     * @return String List of all file paths found
     */

    public static List<String> getResourceFiles() throws LoadDataException{
        List<String> fileNames = new ArrayList<>();
        try {
            // Root of classpath
            URL resourceURL = DataGenerator.class.getClassLoader().getResource("resources/");

            if (resourceURL == null) {
                throw new LoadDataException("Resource path not found.");
            }

            File directory = new File(resourceURL.toURI());
            File[] files = directory.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.getName().endsWith(".csv")) {
                        System.err.println("Adding: " + file.getPath());
                        fileNames.add(file.getPath());
                    }
                }
            }

        } catch (Exception e) {
            throw new LoadDataException("Error loading resource files." + e.getMessage());
        }
        return fileNames;
    }

}
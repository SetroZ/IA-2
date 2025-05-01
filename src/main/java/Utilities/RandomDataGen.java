package Utilities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import Model.Task;
import Model.Employee;

public class RandomDataGen {
    public static class DataSet {
        public DataSet() {
            tasks = new ArrayList<>();
            employees = new ArrayList<>();
        }

        public List<Task> tasks;
        public List<Employee> employees;

        @Override
        public String toString() {
            String res = "-----TASKS-------";
            for (Task task : tasks) {
                res.concat("\n" + task.toString());
            }
            res.concat("\n -----EMPLOYEES-------");
            for (Employee employee : employees) {
                res.concat("\n" + employee.toString());
            }
            return res;
        }
    }

    public static void exportTasksToCSV(List<Task> tasks, String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("ID,EstimatedTime,Difficulty,Deadline,RequiredSkill\n");
            for (Task task : tasks) {
                writer.write(String.format("%s,%d,%d,%d,%s\n",
                        task.getId(),
                        task.getEstimatedTime(),
                        task.getDifficulty(),
                        task.getDeadline(),
                        task.getRequiredSkill()));
            }
            System.out.println("Tasks exported to " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void exportEmployeesToCSV(List<Employee> employees, String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("ID,AvailableHours,SkillLevel,Skills\n");
            for (Employee emp : employees) {
                writer.write(String.format("%s,%d,%d,\"%s\"\n",
                        emp.getId(),
                        emp.getAvailableHours(),
                        emp.getSkillLevel(),
                        String.join(";", emp.getSkills())));
            }
            System.out.println("Employees exported to " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static String[] requiredSkills = new String[] { "A", "B", "C" };
    static Random rd = new Random();

    static private Set<String> generateRandomSkillSet() {
        Set<String> skillSet = new HashSet<>();
        int numSkills = rd.nextInt(requiredSkills.length) + 1;
        while (skillSet.size() < numSkills) {
            skillSet.add(requiredSkills[rd.nextInt(requiredSkills.length)]);
        }
        return skillSet;
    }

    public static DataSet generateDataSet(int numOfTasks, int numOfEmployees) {
        DataSet dataSet = new DataSet();

        for (int i = 0; i < numOfTasks; i++) {
            String skill = requiredSkills[rd.nextInt(0, requiredSkills.length)];
            int estimatedTime = rd.nextInt(1, 10);
            int difficulty = rd.nextInt(1, 10);
            int deadline = rd.nextInt(1, 30);
            String id = Integer.toString(i);

            Task newTask = new Task(id, estimatedTime, difficulty, deadline, skill,
                    i);
            dataSet.tasks.add(newTask);
        }
        for (int i = 0; i < numOfEmployees; i++) {
            String id = Integer.toString(i);
            int hours = rd.nextInt(7, 20);
            int skillLevel = rd.nextInt(1, 10);
            Set<String> skills = generateRandomSkillSet();
            Employee newEmployee = new Employee(id, hours, skillLevel, skills, i);

            dataSet.employees.add(newEmployee);

        }
        System.out.println("Generated");
        int dirNum = 0;
        String directory = "resources";
        File runDir;
        do {
            runDir = new File(directory, "RandomData(" + dirNum + ")");
            dirNum++;
        } while (runDir.exists());
        runDir.mkdirs();

        directory = runDir.getPath();

        exportEmployeesToCSV(dataSet.employees, directory + "/Employees.csv");
        exportTasksToCSV(dataSet.tasks, directory + "/Tasks.csv");
        return dataSet;
    }

}

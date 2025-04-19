package Utilities;

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
            tasks = new ArrayList<Task>();
            employees = new ArrayList<Employee>();
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

    static String[] requiredSkills = new String[] { "A", "B", "C", "D", "E" };
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
            int estimatedTime = rd.nextInt(1, 20);
            int difficulty = rd.nextInt(1, 20);
            int deadline = rd.nextInt(1, 20);
            String id = Integer.toString(i);

            Task newTask = new Task(id, estimatedTime, difficulty, deadline, skill,
                    i);
            dataSet.tasks.add(newTask);
        }
        for (int i = 0; i < numOfEmployees; i++) {
            String id = Integer.toString(i);
            int hours = rd.nextInt(1, 20);
            int skillLevel = rd.nextInt(1, 20);
            int idx = i;
            Set<String> skills = generateRandomSkillSet();
            Employee newEmployee = new Employee(id, hours, skillLevel, skills, idx);

            dataSet.employees.add(newEmployee);
        }
        return dataSet;
    }

    public static void Main(String[] args) {
        System.out.println();
    };
}

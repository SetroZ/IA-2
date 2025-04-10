import Algorithms.GeneticAlgorithm;
import Model.Assignment;
import Model.Employee;
import Model.Task;
import Utilities.DataGenerator;

import java.util.List;

public class Main
{
    public static void main(String[] args)
    {

        try
        {
            List<Task> tasks = DataGenerator.loadTasks();
            List<Employee> employees = DataGenerator.loadEmployees();

            // Print loaded data
            printLoadedData(tasks, employees);

            System.out.println("=====================\n Running Genetic Algorithm\n");
            // Use appropriate parameter values
            GeneticAlgorithm ga = new GeneticAlgorithm(tasks, employees, 100, 0.7, 0.1, -1, false);
            Assignment bestAssignment = ga.run();

            printLoadedData(tasks, employees);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }



    private static void printLoadedData(List<Task> tasks, List<Employee> employees)
    {
        System.out.println("Tasks:");
        for (Task task : tasks)
        {
            System.out.println("  " + task);
        }

        System.out.println("\nEmployees:");
        for (Employee employee : employees)
        {
            System.out.println("  " + employee);
        }
    }
}

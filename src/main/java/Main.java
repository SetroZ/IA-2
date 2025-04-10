import Model.Employee;
import Model.Task;
import Utilities.DataGenerator;
import Utilities.Initialise;
import View.ConsoleView;

import java.util.List;

public class Main
{
    public static void main(String[] args)
    {

        try
        {
            List<Task> tasks = DataGenerator.loadTasks();
            List<Employee> employees = DataGenerator.loadEmployees();
            ConsoleView cv = new ConsoleView(employees, tasks);


            // Print loaded data
            //printLoadedData(tasks, employees);

            //Initialise Population
            int[][] population = Initialise.getInitialPopulation(employees, tasks, 1);

            //printPopulation(population, employees, tasks);

            //cv.printPopulation(population);

            cv.printData();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private static void printPopulation(int[][] population, List<Employee> employees, List<Task> tasks)
    {
        for(int i = 0; i < population.length; i++)
        {
            System.out.println("Population: " + i);
            for(int j = 0; j < population[i].length; j++)
            {
                int taskID = j;
                int employeeID = population[i][j];
                System.out.println("Employee ID: " + employeeID + " Task ID: " + taskID);
                System.out.println(employees.get(employeeID));
                System.out.println(tasks.get(taskID));
            }
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

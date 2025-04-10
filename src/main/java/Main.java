import Algorithms.GeneticAlgorithm;
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


            cv.printData();

            GeneticAlgorithm ga = new GeneticAlgorithm(tasks, employees, 100, 0.4, 0.2, 80, 1,false);
            ga.run();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
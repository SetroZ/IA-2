package Utilities;

import Model.Assignment;
import Model.Employee;
import Model.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Initialise
{

    public static int getRandomEmployeeIndex(int size)
    {
        return new Random().nextInt(size);
    }

    public static List<Assignment> getInitialPopulation(List<Employee> employees, List<Task> tasks, int populationSize)
    {
        List<Assignment> population = new ArrayList<>();

        for (int i = 0; i < populationSize; i++)
        {
            Assignment assignment = new Assignment();
            // Randomly assign each task to an employee
            for (Task task : tasks)
            {
                String employeeId = employees.get(getRandomEmployeeIndex(employees.size())).getId();
                assignment.assign(task.getId(), employeeId);
            }
            population.add(assignment);
        }
        return population;
    }
}

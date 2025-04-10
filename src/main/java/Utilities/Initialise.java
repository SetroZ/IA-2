package Utilities;

import Model.Employee;
import Model.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Utility class for generating an initial population at random
 *
 */
public class Initialise
{

    /**
     * Gets a random index from the range of employee indexes
     *
     * @param size the size of employee list
     * @return random index from 0 to employee size
     */

    public static int getRandomEmployeeIndex(int size)
    {
        return new Random().nextInt(size);
    }

    /**
     * Generates population at random from data
     *
     * @param employees      the list of employees
     * @param tasks          the list of tasks
     * @param populationSize the total number of solutions in the population
     * @return random initial population as int[][] array
     */

    public static int[][] getInitialPopulation(List<Employee> employees, List<Task> tasks, int populationSize)
    {
        int[][] population = new int[populationSize][tasks.size()];
        for (int i = 0; i < populationSize; i++)
        {
            // Randomly assign each task to an employee
            for (Task task : tasks)
            {
                population[i][task.getIdx()] = getRandomEmployeeIndex(employees.size());
            }
        }
        return population;
    }
}
//
//    public static int[][] getTestPopulation(List<Employee> employees, List<Task> tasks)
//    {
//        int[][] population = new int[3][tasks.size()];
//
//        for(Task task : tasks)
//        {
//            population[0][task.getIdx()] = getRandomEmployeeIndex(employees.size());
//        }
//    }
//}

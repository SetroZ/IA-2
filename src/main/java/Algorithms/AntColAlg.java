package Algorithms;

import Model.Employee;
import Model.Task;
import Utilities.Observer;
import Utilities.PerformanceLogger;

import java.util.*;


public class AntColAlg extends AbstractOptimisationAlgorithm
{
    //ACO Parameters - (algorithm-specific settings)
   // private final int populationSize; //How many different entire solutions will be generated within each iteration
    private final double initPheromone; 
    private final double pherDecayRate; //decimal representation of the % decrease of all pheromone values after each decay

    // Internal State Variables
    private double[][] pherMatrix; //2D array storing the pheromone value for each Employee Task pairing; where [i][j] represents pheromone for assigning task i to employee j
    
    // Tracking and reporting
    private boolean foundPerfectSolution = false; //If solution with cost = 0 has been found.
    // private double[] globalBestPheromone;
    private int iterationCount = 0; 


    /**
     * Constructor for Ant Colony Optimisation Algorithm
     * @param populationSize number of solutions per iteration (same as class field)
     * @param pherDecayRate (see class field)
     * @param initPheromone initial pheromone value for all possible employee task pairings
     * @param maxIterations number of iterations before algorithm stops
     * @param REPORTING_FREQUENCY After how many generations the solutions are reported
     * @param fileOutput If it will be reported to file
     * @param tasks The list of tasks to be assigned
     * @param employees The list of employees available for solution
     */

    public AntColAlg(List<Task> tasks, List<Employee> employees, int populationSize, double pherDecayRate, double initPheromone, int maxIterations, int REPORTING_FREQUENCY,
                     boolean fileOutput, int runId)
    {
        super(tasks, employees, REPORTING_FREQUENCY, fileOutput, maxIterations, populationSize, runId);
        this.pherDecayRate = pherDecayRate;
        this.initPheromone = initPheromone;

        this.pherMatrix = new double[tasks.size()][employees.size()];
    }
    
    @Override
    public void run()
    {
        //Start timing performance
        performanceLogger.startTimer();
        //Initialising values stored in pheromone matrix
        initPherMatrix(); 
        //Creating Matrix to store each ant's solution; [i][j] = z means that ant i has assigned task j to employee z.
        int[][] antMatrix = new int[this.populationSize][this.tasks.size()];
        generateNextAntPaths(antMatrix, tasks.size(), employees.size(), this.populationSize);


        while(this.iterationCount < this.maxIterations && !foundPerfectSolution)
        {



            updatePheromones(antMatrix, this.populationSize, employees.size(), tasks.size());
            generateNextAntPaths(antMatrix, tasks.size(), employees.size(), this.populationSize);
            if(this.bestCost == 0.0)
            {
                this.foundPerfectSolution = true; //Flag to stop algorithm if a perfect solution has been found
            }


            // Log metrics for this generation
            performanceLogger.logIteration(
                    iterationCount,
                    bestSolution,
                    bestCost,
                    PerformanceLogger.getCurrentMemoryUsageMB()
            );


            if(iterationCount % reportinFrequency == 0)
            {
                reportProgress(bestSolution, iterationCount);
            }
            this.iterationCount++;

        }

        // Stop timer and save all metrics to CSV files
        performanceLogger.stopTimer();
        performanceLogger.saveMetricsToCSV();

        reportFinalResult(bestSolution, iterationCount);
    }

    /**
     * This is called once at the beginning of the run() method
     * The elements for all possible Employee Task pairs that do not violate the skill mismatch constraint
     * and the difficulty constraint are set to the initial pheromone value; the pairs that do violate these constraints
     * are assigned a value of 0 so that they are not considered.
     * If a task exists where no employee satisfies the skill and difficulty constraints, all employees will be given the initial
     * pheromone value for this task, to avoid a task not being assigned.
     * 
     * 
     */
    private void initPherMatrix()
    {
        //For every task
        for(int i = 0; i < this.tasks.size(); i++)
        {
            boolean capableEmployeeExists = false;
            Task currTask = this.tasks.get(i);
            //For every employee
            for(int j = 0; j < this.employees.size(); j++)
            {
                Employee currEmployee = this.employees.get(j);
                
                //Assign initial pheromone value if skill and difficulty constraints are met
                if(currEmployee.hasSkill(currTask.getRequiredSkill()) && currEmployee.getSkillLevel() >= currTask.getDifficulty())
                {
                    this.pherMatrix[i][j] = this.initPheromone;
                    capableEmployeeExists = true;
                }
                //If constraints violated assign pair a pheromone of 0 (removing it from solution space)
                else 
                {
                    this.pherMatrix[i][j] = 0.0;
                }
                System.out.print(this.pherMatrix[i][j] + " ");
            }
            //If a task has no feasible employee then all employees considered
            if(!capableEmployeeExists)
            {
                for(int j = 0; j < this.employees.size(); j++)
                {
                    this.pherMatrix[i][j] = this.initPheromone;
                }
            }
            System.out.println();
        }
    }

    private void updatePheromones(int[][] antMatrix, int populationSize, int numEmployees, int numTasks)
    {
        int[] ant;
        decayPheromones();
        for(int i = 0; i < populationSize; i++)
        {
            ant = antMatrix[i];
            double antCost = CostCalculator.calculateTotalCost(ant, this.tasks, this.employees);
            if(antCost < bestCost)
            {
                bestCost = antCost;
                bestSolution = ant.clone();
            }
            double pheromone = 1.0/(5.0 * antCost) + 1.0; // Multiplying by 5 ensures no dividing by a decimal, Adding 1 ensures no division by zero
            
            for(int j = 0; j < numTasks; j++) //for each task in ant's solution
            {
                int empIdx = ant[j];
                this.pherMatrix[j][empIdx] += pheromone;
                
            }
        }
    }

    private void generateNextAntPaths(int[][] antMatrix, int numTasks, int numEmployees, int populationSize)
    {
        double cumulative;
        double totalPheromone;
        double choice;
        
        for(int i = 0; i < populationSize; i++)
        {
            for(int j = 0; j < numTasks; j++)
            {
                cumulative = 0;
                totalPheromone = sumTaskPheromone(this.pherMatrix[j], numEmployees);
                choice = Math.random() * totalPheromone;
                
                for(int e = 0; e < numEmployees; e++)
                {
                    cumulative += this.pherMatrix[j][e];
                    if(choice < cumulative)
                    {
                        antMatrix[i][j] = e;
                        e = numEmployees;
                    }
                }
            }
        }
    }

    //This method returns the sum of the pheromones associated with assigning the task to each employee
    private double sumTaskPheromone(double[] task, int numEmployees)
    {
        double totalPheromone = 0;
        for(int i = 0; i < numEmployees; i++)
        {
            totalPheromone += task[i];
        }

        return totalPheromone;
    }



    private void decayPheromones()
    {
        for(int i = 0; i < this.tasks.size(); i++)
        {
            for(int j = 0; j < this.employees.size(); j++)
            {
                this.pherMatrix[i][j] *= (1 - this.pherDecayRate);
            }
        }
    }

    @Override
    public void notifyObservers(String messageType, String title, String content)
    {
        for(Observer observer : observers)
        {
            observer.update(messageType, title, content);
        }
    }

    @Override
    public String getAlgorithmName()
    {
        return "AntColonyAlg";
    }

    @Override
    public int getMaxIterations()
    {
        return maxIterations;
    }

}

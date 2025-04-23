package Algorithms;

import Model.Employee;
import Model.Task;
import Utilities.Initialise;
import Utilities.Observer;
import Utilities.ObserverException;
import Utilities.Subject;
import java.util.*;


public class AntColAlgorithm implements Algorithm, Subject
{
    //Algorithm Parameters
    private int numAnts;
    private int maxIterations;
    private final int REPORTING_FREQUENCY;
    private final boolean fileOutput;
    private double initPheromone;

    // Problem data
    private final List<Task> tasks;
    private final List<Employee> employees;
    private double[][] pherMatrix;
    private double pherDecayRate;

    // Tracking and reporting
    private List<Observer> observers = new ArrayList<>();
    private String output = "";
    private boolean foundPerfectSolution = false;
    private int[] globalBestSolution;
    private double globalBestCost = Double.MAX_VALUE;
    // private double[] globalBestPheromone;
    private int iterationCount = 0;


    /**
     * Constructor for Ant Colony Optimisation Algorithm
     * 
     */

    public AntColAlgorithm(int numAnts, double pherDecayRate, double initPheromone, int maxIterations, int REPORTING_FREQUENCY, 
                            boolean fileOutput, List<Task> tasks, List<Employee> employees)
    {
        this.tasks = tasks;
        this.employees = employees;
        this.pherDecayRate = pherDecayRate;
        this.maxIterations = maxIterations;
        this.REPORTING_FREQUENCY = REPORTING_FREQUENCY;
        this.numAnts = numAnts;
        this.fileOutput = fileOutput;
        this.initPheromone = initPheromone;

        this.pherMatrix = new double[tasks.size()][employees.size()];
    }
    
    @Override
    public void run()
    {
        int[][] antMatrix = Initialise.getInitialPopulation(employees, tasks, numAnts);
        initPherMatrix(this.initPheromone, employees.size(), tasks.size());
        while(this.iterationCount < this.maxIterations && !foundPerfectSolution)
        {
            this.iterationCount++;
            updatePheromones(antMatrix, this.numAnts, employees.size(), tasks.size());
            generateNextAntPaths(antMatrix, tasks.size(), employees.size(), this.numAnts);
            if(this.globalBestCost == 0.0)
            {
                this.foundPerfectSolution = true;
            }

            if(iterationCount % REPORTING_FREQUENCY == 0)
            {
                printProgress(globalBestSolution, iterationCount);
            }
        }

        printFinalResult(globalBestSolution, iterationCount);
    }

    /*
     * Initialises every element in the Pheromone Matrix to the initial Value parameter
     * This is only to be called in the Constructor
     */
    private void initPherMatrix(double initVal, int numEmployees, int numTasks)
    {
        for(int i = 0; i < numTasks; i++)
        {
            for(int j = 0; j < numEmployees; j++)
            {
                this.pherMatrix[i][j] = initVal;
            }
        }
    }

    private void updatePheromones(int[][] antMatrix, int numAnts, int numEmployees, int numTasks)
    {
        int[] ant;
        decayPheromones();
        for(int i = 0; i < numAnts; i++)
        {
            ant = antMatrix[i];
            double antCost = CostCalculator.calculateTotalCost(ant, this.tasks, this.employees);
            System.out.println(antCost);
            if(antCost < this.globalBestCost)
            {
                this.globalBestCost = antCost;
                this.globalBestSolution = ant;
            }
            /* 
            * CREATE A BEST SOLUTION SO FAR TRACKER
            if(antCost == 0.0)
            {
                this.foundPerfectSolution = true;
            }
            */
            double pheromone = 1.0/(5.0 * antCost) + 1.0; // Multiplying by 5 ensures no dividing by a decimal, Adding 1 ensures no division by zero
            
            for(int j = 0; j < numTasks; j++) //for each task in ant's solution
            {
                int empIdx = ant[j];
                this.pherMatrix[j][empIdx] += pheromone;
                
            }
            
        }
    }

    private void generateNextAntPaths(int[][] antMatrix, int numTasks, int numEmployees, int numAnts)
    {
        double cumulative;
        double totalPheromone;
        double choice;
        
        for(int i = 0; i < numAnts; i++)
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

    /**
     * Prints the progress of the current generation.
     *
     * @param bestSolution The best solution found so far
     * @param iteration   The current iteration number
     */
    private void printProgress(int[] bestSolution, int iteration)
    {
        StringBuilder sb = new StringBuilder();
        double cost = CostCalculator.calculateTotalCost(bestSolution, tasks, employees);

        sb.append("Iteration ").append(iteration)
                .append(": Best Cost = ").append(String.format("%.2f", cost))
                .append(", Feasible: ").append(CostCalculator.isFeasible(bestSolution, tasks, employees))
                .append("\n");

        output += sb.toString();

    }

    /**
     * Prints the final result of the algorithm.
     *
     * @param bestSolution The best Solution found
     * @param iteration   The final iteration number
     */
    private void printFinalResult(int[] bestSolution, int iteration)
    {

        double cost = CostCalculator.calculateTotalCost(bestSolution, tasks, employees);
        boolean isFeasible = CostCalculator.isFeasible(bestSolution, tasks, employees);

        String finalResult = observers.getFirst().getFinalSolution(bestSolution, cost, iteration, isFeasible);

        output += finalResult;

        if (fileOutput)
        {
            try
            {
                notifyObservers("FILE", "antColony", output);
            }
            catch (ObserverException e)
            {
                notifyObservers("ERROR", "Writing To File", e.getMessage());
            }
        }
        else
        {
            notifyObservers("INFO", "ANT COLONY RESULT", output);
        }
    }

    /**
     * Gets the best cost history for reporting.
     *
     * @return List of best cost values per generation
     */
    @Override
    public List<Double> getBestCostHistory()
    {
        return null;
    }

    /**
     * Gets the average cost history for reporting.
     *
     * @return List of average cost values per generation
     */
    @Override
    public List<Double> getAvgCostHistory()
    {
        return null;
    }

    /**
     * Gets the feasible solutions history for reporting.
     *
     * @return List of feasible solution counts per generation
     */
    @Override
    public List<Integer> getFeasibleSolutionsHistory()
    {
        return null;
    }

    @Override
    public void registerObserver(Observer observer)
    {
        observers.add(observer);
    }

    @Override
    public void removeObserver(Observer observer)
    {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(String messageType, String title, String content)
    {
        for(Observer observer : observers)
        {
            observer.update(messageType, title, content);
        }
    }

}

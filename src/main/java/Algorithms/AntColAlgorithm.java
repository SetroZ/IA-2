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
    private final int numAnts;
    private final int maxIterations;
    private final int REPORTING_FREQUENCY;
    private final boolean fileOutput;

    // Problem data
    private final List<Task> tasks;
    private final List<Employee> employees;
    private double[][] pherMatrix;
    private final double pherDecayRate;

    // Tracking and reporting
    private List<Observer> observers = new ArrayList<>();
    private String output = "";

    /**
     * Construction for Ant Colony Optimisation Algorithm
     * 
     */
    public AntColAlgorithm(int numAnts, double pherDecayRate, int maxIterations, int REPORTING_FREQUENCY, 
                            boolean fileOutput, List<Task> tasks, List<Employee> employees)
    {
        this.tasks = tasks;
        this.employees = employees;
        this.pherDecayRate = pherDecayRate;
        this.maxIterations = maxIterations;
        this.REPORTING_FREQUENCY = REPORTING_FREQUENCY;
        this.numAnts = numAnts;
        this.fileOutput = fileOutput;

        this.pherMatrix = new double[employees.size()][tasks.size()];
    }

    @Override

    public void run()
    {
        
    }






}
